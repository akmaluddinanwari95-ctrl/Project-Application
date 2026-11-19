package com.example.service

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.TranslationCacheEntity
import com.example.data.remote.TranslationApiClient
import com.example.domain.ConnectionBadgeType
import com.example.domain.ConnectionStatus
import com.example.domain.ErrorAlert
import com.example.domain.FileQueueStatus
import com.example.domain.LogEntry
import com.example.domain.LogLevel
import com.example.domain.PreviewRow
import com.example.domain.QueuedFileItem
import com.example.domain.TranslationEngine
import com.example.domain.TranslationProgressState
import com.example.domain.TranslatorSettings
import com.example.domain.WarbandTextProcessor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object TranslationCoordinator {
  private val apiClient = TranslationApiClient()

  private val _fileQueue = MutableStateFlow<List<QueuedFileItem>>(emptyList())
  val fileQueue: StateFlow<List<QueuedFileItem>> = _fileQueue.asStateFlow()

  private val _progressState = MutableStateFlow(TranslationProgressState())
  val progressState: StateFlow<TranslationProgressState> = _progressState.asStateFlow()

  private val _previewRows = MutableStateFlow<List<PreviewRow>>(emptyList())
  val previewRows: StateFlow<List<PreviewRow>> = _previewRows.asStateFlow()

  private val _activePreviewFileId = MutableStateFlow<String?>(null)
  val activePreviewFileId: StateFlow<String?> = _activePreviewFileId.asStateFlow()

  private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
  val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

  private val _failedRows = MutableStateFlow<List<String>>(emptyList())
  val failedRows: StateFlow<List<String>> = _failedRows.asStateFlow()

  private val _connectionStatus = MutableStateFlow(
      ConnectionStatus("Siap digunakan", ConnectionBadgeType.NEUTRAL)
  )
  val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

  private val _errorAlert = MutableStateFlow<ErrorAlert?>(null)
  val errorAlert: StateFlow<ErrorAlert?> = _errorAlert.asStateFlow()

  private val _completionToast = MutableStateFlow<String?>(null)
  val completionToast: StateFlow<String?> = _completionToast.asStateFlow()

  private val isPausedFlag = AtomicBoolean(false)
  private val shouldStopFlag = AtomicBoolean(false)
  private val logCounter = AtomicLong(0L)
  private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  private var lastErrorAlertTime = 0L

  fun enqueueFiles(newItems: List<QueuedFileItem>) {
    if (newItems.isEmpty()) return
    _fileQueue.update { it + newItems }
    newItems.forEach { item ->
      appendLog("Dimuat: ${item.name} (${item.lines.size} baris)", LogLevel.INFO)
    }
  }

  fun removeQueueItem(id: String) {
    if (_progressState.value.isRunning) return
    _fileQueue.update { list -> list.filterNot { it.id == id } }
  }

  fun clearQueue() {
    if (_progressState.value.isRunning) return
    _fileQueue.value = emptyList()
    _activePreviewFileId.value = null
    _previewRows.value = emptyList()
    _progressState.value = TranslationProgressState()
    _failedRows.value = emptyList()
  }

  fun togglePause() {
    if (!_progressState.value.isRunning) return
    val next = !isPausedFlag.get()
    isPausedFlag.set(next)
    _progressState.update { it.copy(isPaused = next) }
    appendLog(
        if (next) "Proses dijeda oleh pengguna." else "Proses dilanjutkan kembali...",
        LogLevel.WARN
    )
  }

  fun requestStop() {
    if (!_progressState.value.isRunning) return
    shouldStopFlag.set(true)
    isPausedFlag.set(false)
    _progressState.update { it.copy(isPaused = false) }
    appendLog("Menghentikan proses terjemahan...", LogLevel.ERROR)
  }

  fun clearLogs() {
    _logs.value = emptyList()
  }

  fun dismissErrorAlert() {
    _errorAlert.value = null
  }

  fun consumeCompletionToast() {
    _completionToast.value = null
  }

  fun setConnectionStatus(text: String, type: ConnectionBadgeType) {
    _connectionStatus.value = ConnectionStatus(text, type)
  }

  fun appendLog(message: String, level: LogLevel) {
    val entry = LogEntry(
        id = logCounter.incrementAndGet(),
        timestamp = timeFormat.format(Date()),
        message = message,
        level = level
    )
    _logs.update { current ->
      (current + entry).takeLast(250)
    }
  }

  suspend fun updateManualPreviewLine(
      context: Context,
      lineIndex: Int,
      newTranslatedLine: String,
      separator: String,
      targetLang: String
  ) {
    val fileId = _activePreviewFileId.value ?: return
    val currentQueue = _fileQueue.value
    val targetFile = currentQueue.firstOrNull { it.id == fileId } ?: return
    if (lineIndex !in targetFile.results.indices) return

    val updatedResults = targetFile.results.toMutableList()
    updatedResults[lineIndex] = newTranslatedLine
    val updatedFile = targetFile.copy(results = updatedResults)

    _fileQueue.value = currentQueue.map { if (it.id == fileId) updatedFile else it }
    refreshPreviewRows(updatedFile)

    val origLine = updatedFile.lines.getOrNull(lineIndex) ?: ""
    val sep = separator.ifEmpty { "|" }
    val origSepIdx = origLine.indexOf(sep)
    val newSepIdx = newTranslatedLine.indexOf(sep)
    if (origSepIdx != -1 && newSepIdx != -1) {
      val origText = origLine.substring(origSepIdx + sep.length).trim()
      val newText = newTranslatedLine.substring(newSepIdx + sep.length).trim()
      if (origText.isNotEmpty() && newText.isNotEmpty()) {
        val dao = AppDatabase.getInstance(context).translationCacheDao()
        dao.upsert(TranslationCacheEntity(targetLang = targetLang, sourceText = origText, translatedText = newText))
      }
    }
  }

  suspend fun runBatchTranslation(
      context: Context,
      settings: TranslatorSettings,
      onProgressUpdate: (TranslationProgressState) -> Unit
  ) {
    val pendingFiles = _fileQueue.value.filter { it.status == FileQueueStatus.WAIT }
    if (pendingFiles.isEmpty()) return

    val dao = AppDatabase.getInstance(context).translationCacheDao()
    isPausedFlag.set(false)
    shouldStopFlag.set(false)
    _failedRows.value = emptyList()

    val fallbackPool = TranslationEngine.entries.filter { it != settings.engine }
    val glossary = WarbandTextProcessor.parseGlossary(settings.glossaryText)
    val sep = settings.separator.ifEmpty { "|" }

    appendLog(
        "Memulai terjemahan ${pendingFiles.size} file menggunakan ${settings.engine.displayName}...",
        LogLevel.OK
    )

    val pendingBuffer = mutableListOf<TranslationCacheEntity>()

    for ((fIdx, pendingItem) in pendingFiles.withIndex()) {
      if (shouldStopFlag.get()) break

      updateQueueItem(pendingItem.id) { it.copy(status = FileQueueStatus.RUN, results = emptyList()) }
      _activePreviewFileId.value = pendingItem.id

      val lines = pendingItem.lines
      val total = lines.size
      val results = ArrayList<String>(total)
      var cacheHits = 0
      val startTimeMs = System.currentTimeMillis()

      val initialProgress = TranslationProgressState(
          isRunning = true,
          isPaused = false,
          currentFileIndex = fIdx + 1,
          totalPendingFiles = pendingFiles.size,
          currentFileName = pendingItem.name,
          processedLines = 0,
          totalLines = total,
          cacheHits = 0,
          etaText = ""
      )
      _progressState.value = initialProgress
      onProgressUpdate(initialProgress)

      for (i in lines.indices) {
        while (isPausedFlag.get() && !shouldStopFlag.get()) {
          delay(250L)
        }
        if (shouldStopFlag.get()) break

        val originalLine = lines[i]

        if (WarbandTextProcessor.matchesSkipRegex(originalLine, settings.skipRegex)) {
          results.add(originalLine)
          val p = buildProgress(
              fIdx + 1,
              pendingFiles.size,
              pendingItem.name,
              i + 1,
              total,
              cacheHits,
              startTimeMs
          )
          _progressState.value = p
          if ((i + 1) % 5 == 0 || i == total - 1) onProgressUpdate(p)
          continue
        }

        val split = WarbandTextProcessor.splitLine(originalLine, sep, settings.stripTag)
        if (split.textPart.isEmpty()) {
          val emptyOut = if (split.hasCode) "${split.codePart}$sep" else ""
          results.add(emptyOut)
          cacheHits++
          val p = buildProgress(
              fIdx + 1,
              pendingFiles.size,
              pendingItem.name,
              i + 1,
              total,
              cacheHits,
              startTimeMs
          )
          _progressState.value = p
          if ((i + 1) % 5 == 0 || i == total - 1) onProgressUpdate(p)
          continue
        }

        val cached = dao.getTranslation(settings.targetLang, split.textPart)
        if (cached != null) {
          val out = if (split.hasCode) "${split.codePart}$sep$cached" else cached
          results.add(out)
          cacheHits++
          val p = buildProgress(
              fIdx + 1,
              pendingFiles.size,
              pendingItem.name,
              i + 1,
              total,
              cacheHits,
              startTimeMs
          )
          _progressState.value = p
          if ((i + 1) % 5 == 0 || i == total - 1) {
            updateQueueItem(pendingItem.id) { it.copy(results = results.toList()) }
            refreshPreviewFromLists(lines, results)
            onProgressUpdate(p)
          }
          continue
        }

        val protected = WarbandTextProcessor.protectWarbandTokens(split.textPart, glossary)
        var translatedText: String? = null
        var lastErrorMsg = "Koneksi ke server penerjemah gagal"

        for (attempt in 1..3) {
          if (shouldStopFlag.get()) break
          try {
            val raw = apiClient.translate(
                engine = settings.engine,
                text = protected.protectedText,
                sourceLang = settings.sourceLang,
                targetLang = settings.targetLang
            )
            translatedText = WarbandTextProcessor.restoreWarbandTokens(
                raw,
                protected.tokens,
                glossary
            )
            _connectionStatus.value = ConnectionStatus(
                "Terhubung (${settings.engine.displayName})",
                ConnectionBadgeType.OK
            )
            break
          } catch (e: Exception) {
            lastErrorMsg = e.message ?: lastErrorMsg
            if (attempt < 3) delay(800L * attempt)
          }
        }

        if (translatedText == null && settings.autoFallback && !shouldStopFlag.get()) {
          for (altEngine in fallbackPool) {
            if (shouldStopFlag.get()) break
            try {
              val raw = apiClient.translate(
                  engine = altEngine,
                  text = protected.protectedText,
                  sourceLang = settings.sourceLang,
                  targetLang = settings.targetLang
              )
              translatedText = WarbandTextProcessor.restoreWarbandTokens(
                  raw,
                  protected.tokens,
                  glossary
              )
              _connectionStatus.value = ConnectionStatus(
                  "Fallback (${altEngine.displayName})",
                  ConnectionBadgeType.WARN
              )
              appendLog(
                  "Baris ${i + 1} dialihkan ke ${altEngine.displayName}",
                  LogLevel.WARN
              )
              break
            } catch (altErr: Exception) {
              lastErrorMsg = altErr.message ?: lastErrorMsg
            }
          }
        }

        if (shouldStopFlag.get()) break

        val finalOut: String
        if (translatedText != null) {
          finalOut = if (split.hasCode) "${split.codePart}$sep$translatedText" else translatedText
          pendingBuffer.add(
              TranslationCacheEntity(
                  targetLang = settings.targetLang,
                  sourceText = split.textPart,
                  translatedText = translatedText
              )
          )
        } else {
          finalOut = originalLine
          _connectionStatus.value = ConnectionStatus("Koneksi Terputus / Limit", ConnectionBadgeType.ERROR)
          _failedRows.update { it + "[${pendingItem.name} | Baris ${i + 1}] $originalLine" }
          appendLog("Gagal baris ${i + 1} di ${pendingItem.name} ($lastErrorMsg)", LogLevel.ERROR)
          emitThrottledErrorAlert(
              title = "Gagal Menerjemahkan Baris",
              detail = "Teks asli dipertahankan sementara. Penyebab: $lastErrorMsg",
              lineInfo = "${pendingItem.name} · Baris #${i + 1}"
          )
        }

        results.add(finalOut)

        if (pendingBuffer.size >= 25) {
          dao.upsertAll(pendingBuffer.toList())
          pendingBuffer.clear()
        }

        val p = buildProgress(
            fIdx + 1,
            pendingFiles.size,
            pendingItem.name,
            i + 1,
            total,
            cacheHits,
            startTimeMs
        )
        _progressState.value = p

        if ((i + 1) % 5 == 0 || i == total - 1) {
          updateQueueItem(pendingItem.id) { it.copy(results = results.toList()) }
          refreshPreviewFromLists(lines, results)
          onProgressUpdate(p)
        }

        delay(settings.delayMs)
      }

      if (pendingBuffer.isNotEmpty()) {
        dao.upsertAll(pendingBuffer.toList())
        pendingBuffer.clear()
      }
      dao.trimOldEntries(15000)

      if (!shouldStopFlag.get()) {
        updateQueueItem(pendingItem.id) {
          it.copy(status = FileQueueStatus.DONE, results = results.toList())
        }
        refreshPreviewFromLists(lines, results)
        appendLog("Selesai: ${pendingItem.name} ($total baris)", LogLevel.OK)
      } else {
        updateQueueItem(pendingItem.id) {
          it.copy(status = FileQueueStatus.WAIT, results = emptyList())
        }
        appendLog(
            "Dibatalkan pada ${pendingItem.name}. Progres tersimpan di cache!",
            LogLevel.WARN
        )
      }
    }

    val stoppedByUser = shouldStopFlag.get()
    isPausedFlag.set(false)
    shouldStopFlag.set(false)
    _progressState.update { it.copy(isRunning = false, isPaused = false) }
    if (!stoppedByUser) {
      _completionToast.value = "Semua file selesai diterjemahkan!"
    }
  }

  private fun buildProgress(
      fileIdx: Int,
      totalFiles: Int,
      fileName: String,
      done: Int,
      total: Int,
      cacheHits: Int,
      startTimeMs: Long
  ): TranslationProgressState {
    val elapsedSec = (System.currentTimeMillis() - startTimeMs) / 1000.0
    val etaStr = if (done > 5 && elapsedSec > 1.0 && total > done) {
      val rate = done / elapsedSec
      val remSec = ((total - done) / rate).toLong().coerceAtLeast(0L)
      val mins = remSec / 60
      val secs = remSec % 60
      "~${mins}m ${secs}d"
    } else {
      ""
    }
    return TranslationProgressState(
        isRunning = true,
        isPaused = isPausedFlag.get(),
        currentFileIndex = fileIdx,
        totalPendingFiles = totalFiles,
        currentFileName = fileName,
        processedLines = done,
        totalLines = total,
        cacheHits = cacheHits,
        etaText = etaStr
    )
  }

  private fun updateQueueItem(id: String, transform: (QueuedFileItem) -> QueuedFileItem) {
    _fileQueue.update { list ->
      list.map { if (it.id == id) transform(it) else it }
    }
  }

  private fun refreshPreviewRows(item: QueuedFileItem) {
    refreshPreviewFromLists(item.lines, item.results)
  }

  private fun refreshPreviewFromLists(origLines: List<String>, transLines: List<String>) {
    val count = transLines.size
    val start = (count - 5).coerceAtLeast(0)
    val preview = (start until count).map { idx ->
      PreviewRow(
          lineIndex = idx,
          originalLine = origLines.getOrNull(idx) ?: "",
          translatedLine = transLines[idx]
      )
    }
    _previewRows.value = preview
  }

  private fun emitThrottledErrorAlert(title: String, detail: String, lineInfo: String) {
    val now = System.currentTimeMillis()
    if (now - lastErrorAlertTime < 2200L) return
    lastErrorAlertTime = now
    _errorAlert.value = ErrorAlert(
        id = now,
        title = title,
        detail = detail,
        lineInfo = lineInfo
    )
  }
}
