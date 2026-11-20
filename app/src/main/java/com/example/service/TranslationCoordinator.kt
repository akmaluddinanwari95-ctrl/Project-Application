package com.example.service

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.TranslationCacheEntity
import com.example.data.local.TranslationHistoryEntity
import com.example.data.local.TranslationHistoryRepository
import com.example.data.remote.TranslationApiClient
import com.example.domain.ConnectionBadgeType
import com.example.domain.ConnectionStatus
import com.example.domain.ErrorAlert
import com.example.domain.FileQueueStatus
import com.example.domain.LogEntry
import com.example.domain.LogLevel
import com.example.domain.PreviewRow
import com.example.domain.QueuedFileItem
import com.example.domain.SessionSummaryReport
import com.example.domain.TelemetrySample
import com.example.domain.TranslationEngine
import com.example.domain.TranslationErrorItem
import com.example.domain.TranslationProgressState
import com.example.domain.TranslatorSettings
import com.example.domain.WarbandTextProcessor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

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

  private val _errorLogEntries = MutableStateFlow<List<TranslationErrorItem>>(emptyList())
  val errorLogEntries: StateFlow<List<TranslationErrorItem>> = _errorLogEntries.asStateFlow()

  private val _failedRows = MutableStateFlow<List<String>>(emptyList())
  val failedRows: StateFlow<List<String>> = _failedRows.asStateFlow()

  private val _sessionSummary = MutableStateFlow<SessionSummaryReport?>(null)
  val sessionSummary: StateFlow<SessionSummaryReport?> = _sessionSummary.asStateFlow()

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

  fun enqueueRestoredItem(restored: QueuedFileItem) {
    _fileQueue.update { it + restored }
    if (restored.results.isNotEmpty()) {
      _activePreviewFileId.value = restored.id
      refreshPreviewFromLists(restored.lines, restored.results)
    }
    appendLog("Dibuka kembali dari riwayat: ${restored.name} (${restored.lines.size} baris)", LogLevel.INFO)
  }

  suspend fun refreshAutoResumeCounts(context: Context, settings: TranslatorSettings) {
    val queueSnapshot = _fileQueue.value
    if (queueSnapshot.isEmpty()) return

    val cachedSet = withContext(Dispatchers.IO) {
      AppDatabase.getInstance(context)
          .translationCacheDao()
          .getCachedSourcesForLang(settings.targetLang)
          .toHashSet()
    }
    val sep = settings.separator.ifEmpty { "|" }

    val updated = withContext(Dispatchers.Default) {
      queueSnapshot.map { item ->
        if (item.status == FileQueueStatus.DONE) {
          item.copy(cachedLinesCount = item.lines.size, processedCount = item.lines.size)
        } else {
          var cachedMatches = 0
          item.lines.forEach { raw ->
            if (WarbandTextProcessor.matchesSkipRegex(raw, settings.skipRegex)) {
              cachedMatches++
            } else {
              val split = WarbandTextProcessor.splitLine(raw, sep, settings.stripTag)
              if (split.textPart.isEmpty() || cachedSet.contains(split.textPart)) {
                cachedMatches++
              }
            }
          }
          item.copy(cachedLinesCount = cachedMatches)
        }
      }
    }

    _fileQueue.update { current ->
      val byId = updated.associateBy { it.id }
      current.map { existing ->
        val calculated = byId[existing.id]
        if (calculated != null && existing.status != FileQueueStatus.RUN) {
          existing.copy(cachedLinesCount = calculated.cachedLinesCount)
        } else {
          existing
        }
      }
    }
  }

  fun removeQueueItem(id: String) {
    if (_progressState.value.isRunning) return
    _fileQueue.update { list -> list.filterNot { it.id == id } }
  }

  fun moveQueueItem(id: String, delta: Int) {
    if (_progressState.value.isRunning) return
    _fileQueue.update { list ->
      val idx = list.indexOfFirst { it.id == id }
      if (idx == -1) return@update list
      val target = (idx + delta).coerceIn(0, list.lastIndex)
      if (target == idx) return@update list
      val mutable = list.toMutableList()
      val item = mutable.removeAt(idx)
      mutable.add(target, item)
      mutable
    }
  }

  fun retryQueueItem(id: String) {
    if (_progressState.value.isRunning) return
    _fileQueue.update { list ->
      list.map { item ->
        if (item.id == id) {
          item.copy(status = FileQueueStatus.WAIT, errorMessage = null)
        } else {
          item
        }
      }
    }
  }

  fun clearQueue() {
    if (_progressState.value.isRunning) return
    _fileQueue.value = emptyList()
    _activePreviewFileId.value = null
    _previewRows.value = emptyList()
    _progressState.value = TranslationProgressState()
    _failedRows.value = emptyList()
    _errorLogEntries.value = emptyList()
    _sessionSummary.value = null
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

  fun dismissSessionSummary() {
    _sessionSummary.value = null
  }

  fun clearErrorLog() {
    _errorLogEntries.value = emptyList()
    _failedRows.value = emptyList()
  }

  fun dismissErrorEntry(errorId: String) {
    _errorLogEntries.update { list -> list.filterNot { it.id == errorId } }
    syncFailedRowsStrings()
  }

  fun recordFormatError(
      fileId: String? = null,
      fileName: String,
      errorMessage: String,
      sampleLine: String = ""
  ) {
    _errorLogEntries.update { current ->
      val duplicate = current.any {
        it.isFormatError && it.fileName == fileName && it.errorMessage == errorMessage
      }
      if (duplicate) {
        current
      } else {
        current + TranslationErrorItem(
            id = UUID.randomUUID().toString(),
            fileId = fileId,
            fileName = fileName,
            lineIndex = null,
            originalLine = sampleLine,
            errorMessage = errorMessage,
            isFormatError = true
        )
      }
    }
    syncFailedRowsStrings()
  }

  private fun syncFailedRowsStrings() {
    _failedRows.value = _errorLogEntries.value.map { entry ->
      if (entry.lineIndex != null) {
        "[${entry.fileName} | Baris ${entry.lineIndex + 1}] (${entry.errorMessage}) ${entry.originalLine}"
      } else {
        "[${entry.fileName} | FORMAT] ${entry.errorMessage}"
      }
    }
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

  suspend fun updateLineInQueueFile(
      context: Context,
      fileId: String,
      lineIndex: Int,
      newTranslatedLine: String,
      separator: String,
      targetLang: String
  ) {
    val currentQueue = _fileQueue.value
    val targetFile = currentQueue.firstOrNull { it.id == fileId } ?: return
    if (lineIndex !in targetFile.results.indices) return

    val updatedResults = targetFile.results.toMutableList()
    updatedResults[lineIndex] = newTranslatedLine
    val updatedFile = targetFile.copy(results = updatedResults)

    _fileQueue.value = currentQueue.map { if (it.id == fileId) updatedFile else it }
    if (_activePreviewFileId.value == fileId) {
      refreshPreviewRows(updatedFile)
    }

    val origLine = updatedFile.lines.getOrNull(lineIndex) ?: ""
    val sep = separator.ifEmpty { "|" }
    val origSepIdx = origLine.indexOf(sep)
    val newSepIdx = newTranslatedLine.indexOf(sep)
    if (origSepIdx != -1 && newSepIdx != -1) {
      val origText = origLine.substring(origSepIdx + sep.length).trim()
      val newText = newTranslatedLine.substring(newSepIdx + sep.length).trim()
      if (origText.isNotEmpty() && newText.isNotEmpty()) {
        AppDatabase.getInstance(context)
            .translationCacheDao()
            .upsert(TranslationCacheEntity(targetLang = targetLang, sourceText = origText, translatedText = newText))
      }
    }
  }

  suspend fun applyBatchReplaceToFile(
      context: Context,
      fileId: String,
      findQuery: String,
      replaceWith: String,
      caseSensitive: Boolean,
      separator: String,
      targetLang: String
  ): Int {
    val targetFile = _fileQueue.value.firstOrNull { it.id == fileId } ?: return 0
    if (targetFile.results.isEmpty() || findQuery.isEmpty()) return 0

    val (updatedResults, changedCount) = WarbandTextProcessor.findAndReplaceInLines(
        lines = targetFile.results,
        findQuery = findQuery,
        replaceWith = replaceWith,
        caseSensitive = caseSensitive,
        separator = separator
    )
    if (changedCount == 0) return 0

    val updatedFile = targetFile.copy(results = updatedResults)
    _fileQueue.update { q -> q.map { if (it.id == fileId) updatedFile else it } }
    if (_activePreviewFileId.value == fileId) {
      refreshPreviewRows(updatedFile)
    }

    val sep = separator.ifEmpty { "|" }
    val cacheUpdates = mutableListOf<TranslationCacheEntity>()
    updatedResults.forEachIndexed { idx, newLine ->
      if (newLine != targetFile.results.getOrNull(idx)) {
        val origSplit = WarbandTextProcessor.splitLine(targetFile.lines.getOrNull(idx) ?: "", sep, "")
        val newSplit = WarbandTextProcessor.splitLine(newLine, sep, "")
        if (origSplit.textPart.isNotEmpty() && newSplit.textPart.isNotEmpty()) {
          cacheUpdates.add(
              TranslationCacheEntity(
                  targetLang = targetLang,
                  sourceText = origSplit.textPart,
                  translatedText = newSplit.textPart
              )
          )
        }
      }
    }
    if (cacheUpdates.isNotEmpty()) {
      AppDatabase.getInstance(context).translationCacheDao().upsertAll(cacheUpdates)
    }
    appendLog("Cari-Ganti pada ${targetFile.name}: $changedCount baris diperbarui.", LogLevel.OK)
    return changedCount
  }

  suspend fun autoFixFileMissingTokens(
      context: Context,
      fileId: String,
      separator: String,
      stripTag: String,
      targetLang: String
  ): Int {
    val targetFile = _fileQueue.value.firstOrNull { it.id == fileId } ?: return 0
    if (targetFile.results.isEmpty()) return 0

    val updatedResults = targetFile.results.toMutableList()
    var fixedCount = 0
    val cacheUpdates = mutableListOf<TranslationCacheEntity>()

    targetFile.lines.forEachIndexed { idx, origLine ->
      val currentTrans = updatedResults.getOrNull(idx) ?: return@forEachIndexed
      val fixed = WarbandTextProcessor.autoFixMissingTokens(origLine, currentTrans, separator, stripTag)
      if (fixed != currentTrans) {
        updatedResults[idx] = fixed
        fixedCount++
        val origSplit = WarbandTextProcessor.splitLine(origLine, separator, stripTag)
        val fixedSplit = WarbandTextProcessor.splitLine(fixed, separator, "")
        if (origSplit.textPart.isNotEmpty() && fixedSplit.textPart.isNotEmpty()) {
          cacheUpdates.add(
              TranslationCacheEntity(
                  targetLang = targetLang,
                  sourceText = origSplit.textPart,
                  translatedText = fixedSplit.textPart
              )
          )
        }
      }
    }

    if (fixedCount > 0) {
      val updatedFile = targetFile.copy(results = updatedResults)
      _fileQueue.update { q -> q.map { if (it.id == fileId) updatedFile else it } }
      if (_activePreviewFileId.value == fileId) {
        refreshPreviewRows(updatedFile)
      }
      if (cacheUpdates.isNotEmpty()) {
        AppDatabase.getInstance(context).translationCacheDao().upsertAll(cacheUpdates)
      }
      appendLog("QA Auto-Fix pada ${targetFile.name}: $fixedCount baris diperbaiki.", LogLevel.OK)
    }
    return fixedCount
  }

  suspend fun runBatchTranslation(
      context: Context,
      settings: TranslatorSettings,
      onProgressUpdate: (TranslationProgressState) -> Unit
  ) {
    val pendingFiles = _fileQueue.value.filter { it.status == FileQueueStatus.WAIT }
    if (pendingFiles.isEmpty()) return

    val db = AppDatabase.getInstance(context)
    val dao = db.translationCacheDao()
    val historyRepo = TranslationHistoryRepository(db.translationHistoryDao())
    isPausedFlag.set(false)
    shouldStopFlag.set(false)
    // Preserve pre-flight format errors? Clear only previous row errors before batch run
    _errorLogEntries.update { list -> list.filter { it.isFormatError } }
    syncFailedRowsStrings()
    _sessionSummary.value = null

    val batchStartMs = System.currentTimeMillis()
    var sessionOriginalLines = 0
    var sessionTranslatedLines = 0
    var sessionSkippedLines = 0
    var sessionErrors = 0
    var sessionWords = 0
    var sessionFilesSucceeded = 0
    var sessionFilesFailed = 0

    val fallbackPool = TranslationEngine.entries.filter { it != settings.engine }
    val glossary = WarbandTextProcessor.parseGlossary(settings.glossaryText)
    val sep = settings.separator.ifEmpty { "|" }

    appendLog(
        "Memulai batch ${pendingFiles.size} file menggunakan ${settings.engine.displayName}...",
        LogLevel.OK
    )

    val pendingBuffer = mutableListOf<TranslationCacheEntity>()

    for ((fIdx, pendingItem) in pendingFiles.withIndex()) {
      if (shouldStopFlag.get()) break

      updateQueueItem(pendingItem.id) {
        it.copy(status = FileQueueStatus.RUN, results = emptyList(), processedCount = 0, errorMessage = null)
      }
      _activePreviewFileId.value = pendingItem.id

      val lines = pendingItem.lines
      val total = lines.size
      val fileStartMs = System.currentTimeMillis()
      val results = ArrayList<String>(total)
      var cacheHits = 0
      var fileTranslatedLines = 0
      var fileSkippedLines = 0
      var fileErrorsCount = 0
      var activeElapsedMs = 0L
      var lastTickMs = System.currentTimeMillis()
      var lastSampleMs = 0L
      var peakSpeed = 0f
      val samples = mutableListOf(
          TelemetrySample(
              elapsedSec = 0f,
              speedLinesPerSec = 0f,
              progressPercent = 0f,
              processedLines = 0
          )
      )
      val windowEvents = ArrayDeque<Pair<Long, Int>>() // (activeMs, processedCount)

      fun sampleTelemetry(done: Int, forceSample: Boolean): TranslationProgressState {
        val now = System.currentTimeMillis()
        activeElapsedMs += (now - lastTickMs).coerceAtLeast(0L)
        lastTickMs = now

        windowEvents.addLast(activeElapsedMs to done)
        while (windowEvents.size > 1 && (activeElapsedMs - windowEvents.first().first) > 4000L) {
          windowEvents.removeFirst()
        }

        val instantSpeed = if (windowEvents.size >= 2) {
          val dtSec = (windowEvents.last().first - windowEvents.first().first) / 1000f
          val dLines = (windowEvents.last().second - windowEvents.first().second).coerceAtLeast(0)
          if (dtSec > 0.15f) dLines / dtSec else {
            val totalSec = (activeElapsedMs / 1000f).coerceAtLeast(0.2f)
            done / totalSec
          }
        } else {
          val totalSec = (activeElapsedMs / 1000f).coerceAtLeast(0.2f)
          if (done > 0) done / totalSec else 0f
        }

        if (instantSpeed > peakSpeed && done > 2) {
          peakSpeed = instantSpeed
        }

        val pct = if (total > 0) ((done.toFloat() / total) * 100f).coerceIn(0f, 100f) else 0f
        if (forceSample || (activeElapsedMs - lastSampleMs) >= 350L) {
          lastSampleMs = activeElapsedMs
          samples.add(
              TelemetrySample(
                  elapsedSec = activeElapsedMs / 1000f,
                  speedLinesPerSec = instantSpeed,
                  progressPercent = pct,
                  processedLines = done
              )
          )
          if (samples.size > 48) {
            samples.removeAt(0)
          }
        }

        val avgSpeed = if (activeElapsedMs > 1000L && done > 3) {
          done / (activeElapsedMs / 1000.0)
        } else {
          instantSpeed.toDouble()
        }
        val etaStr = if (done > 3 && avgSpeed > 0.05 && total > done) {
          val remSec = ((total - done) / avgSpeed).toLong().coerceAtLeast(0L)
          val mins = remSec / 60
          val secs = remSec % 60
          "~${mins}m ${secs}d"
        } else {
          ""
        }

        return TranslationProgressState(
            isRunning = true,
            isPaused = isPausedFlag.get(),
            currentFileIndex = fIdx + 1,
            totalPendingFiles = pendingFiles.size,
            currentFileName = pendingItem.name,
            processedLines = done,
            totalLines = total,
            cacheHits = cacheHits,
            etaText = etaStr,
            currentSpeedLinesPerSec = instantSpeed,
            peakSpeedLinesPerSec = peakSpeed,
            telemetrySamples = samples.toList()
        )
      }

      val initialProgress = TranslationProgressState(
          isRunning = true,
          isPaused = false,
          currentFileIndex = fIdx + 1,
          totalPendingFiles = pendingFiles.size,
          currentFileName = pendingItem.name,
          processedLines = 0,
          totalLines = total,
          cacheHits = 0,
          etaText = "",
          currentSpeedLinesPerSec = 0f,
          peakSpeedLinesPerSec = 0f,
          telemetrySamples = samples.toList()
      )
      _progressState.value = initialProgress
      onProgressUpdate(initialProgress)

      val batchSplitRegex = Regex("\\s*ZXQ\\s*BR\\s*QXZ\\s*", RegexOption.IGNORE_CASE)
      val batchSizeLimit = settings.batchSize.coerceIn(1, 10)

      suspend fun translateSingleWithRetry(
          protectedText: String,
          tokens: List<String>,
          lineNumber1Based: Int
      ): Pair<String?, String> {
        var translated: String? = null
        var lastError = "Koneksi ke server penerjemah gagal"

        for (attempt in 1..3) {
          if (shouldStopFlag.get()) break
          try {
            val raw = apiClient.translate(
                engine = settings.engine,
                text = protectedText,
                sourceLang = settings.sourceLang,
                targetLang = settings.targetLang
            )
            val restored = WarbandTextProcessor.restoreWarbandTokens(raw, tokens, glossary)
            translated = WarbandTextProcessor.applyMedievalTonePostProcessing(
                restored,
                settings.targetLang,
                settings.medievalToneEnabled
            )
            _connectionStatus.value = ConnectionStatus(
                "Terhubung (${settings.engine.displayName})",
                ConnectionBadgeType.OK
            )
            break
          } catch (e: Exception) {
            lastError = e.message ?: lastError
            if (attempt < 3) delay(800L * attempt)
          }
        }

        if (translated == null && settings.autoFallback && !shouldStopFlag.get()) {
          for (altEngine in fallbackPool) {
            if (shouldStopFlag.get()) break
            try {
              val raw = apiClient.translate(
                  engine = altEngine,
                  text = protectedText,
                  sourceLang = settings.sourceLang,
                  targetLang = settings.targetLang
              )
              val restored = WarbandTextProcessor.restoreWarbandTokens(raw, tokens, glossary)
              translated = WarbandTextProcessor.applyMedievalTonePostProcessing(
                  restored,
                  settings.targetLang,
                  settings.medievalToneEnabled
              )
              _connectionStatus.value = ConnectionStatus(
                  "Fallback (${altEngine.displayName})",
                  ConnectionBadgeType.WARN
              )
              appendLog(
                  "Baris $lineNumber1Based dialihkan ke ${altEngine.displayName}",
                  LogLevel.WARN
              )
              break
            } catch (altErr: Exception) {
              lastError = altErr.message ?: lastError
            }
          }
        }

        return translated to lastError
      }

      var i = 0
      while (i < lines.size) {
        if (isPausedFlag.get() && !shouldStopFlag.get()) {
          while (isPausedFlag.get() && !shouldStopFlag.get()) {
            delay(250L)
          }
          lastTickMs = System.currentTimeMillis()
        }
        if (shouldStopFlag.get()) break

        val originalLine = lines[i]

        if (WarbandTextProcessor.matchesSkipRegex(originalLine, settings.skipRegex)) {
          results.add(originalLine)
          fileSkippedLines++
          val doneNow = results.size
          val force = (doneNow == total) || (doneNow % 25 == 0)
          val p = sampleTelemetry(doneNow, forceSample = force)
          _progressState.value = p
          if (doneNow % 10 == 0 || doneNow == total) {
            updateQueueItem(pendingItem.id) { it.copy(processedCount = doneNow) }
            onProgressUpdate(p)
          }
          i++
          continue
        }

        val split = WarbandTextProcessor.splitLine(originalLine, sep, settings.stripTag)
        if (split.textPart.isEmpty()) {
          val emptyOut = if (split.hasCode) "${split.codePart}$sep" else ""
          results.add(emptyOut)
          cacheHits++
          fileSkippedLines++
          val doneNow = results.size
          val force = (doneNow == total) || (doneNow % 25 == 0)
          val p = sampleTelemetry(doneNow, forceSample = force)
          _progressState.value = p
          if (doneNow % 10 == 0 || doneNow == total) {
            updateQueueItem(pendingItem.id) { it.copy(processedCount = doneNow, cachedLinesCount = maxOf(it.cachedLinesCount, cacheHits)) }
            onProgressUpdate(p)
          }
          i++
          continue
        }

        val cached = dao.getTranslation(settings.targetLang, split.textPart)
        if (cached != null) {
          val out = if (split.hasCode) "${split.codePart}$sep$cached" else cached
          results.add(out)
          cacheHits++
          fileTranslatedLines++
          val doneNow = results.size
          val force = (doneNow == total) || (doneNow % 25 == 0)
          val p = sampleTelemetry(doneNow, forceSample = force)
          _progressState.value = p
          if (doneNow % 5 == 0 || doneNow == total) {
            updateQueueItem(pendingItem.id) {
              it.copy(
                  results = results.toList(),
                  processedCount = doneNow,
                  cachedLinesCount = maxOf(it.cachedLinesCount, cacheHits)
              )
            }
            refreshPreviewFromLists(lines, results)
            onProgressUpdate(p)
          }
          i++
          continue
        }

        // Collect mini-batch of consecutive uncached translatable lines when batchSizeLimit > 1
        data class PendingBatchItem(
            val index: Int,
            val originalLine: String,
            val split: com.example.domain.SplitLineResult,
            val protected: com.example.domain.ProtectedTextResult
        )

        val batchItems = mutableListOf<PendingBatchItem>()
        if (batchSizeLimit > 1) {
          var scanIdx = i
          var charBudget = 0
          while (scanIdx < lines.size && batchItems.size < batchSizeLimit) {
            val scanRaw = lines[scanIdx]
            if (WarbandTextProcessor.matchesSkipRegex(scanRaw, settings.skipRegex)) break
            val scanSplit = WarbandTextProcessor.splitLine(scanRaw, sep, settings.stripTag)
            if (scanSplit.textPart.isEmpty()) break
            if (dao.getTranslation(settings.targetLang, scanSplit.textPart) != null) break
            val scanProt = WarbandTextProcessor.protectWarbandTokens(scanSplit.textPart, glossary)
            if (batchItems.isNotEmpty() && charBudget + scanProt.protectedText.length > 1050) break
            charBudget += scanProt.protectedText.length + 14
            batchItems.add(PendingBatchItem(scanIdx, scanRaw, scanSplit, scanProt))
            scanIdx++
          }
        }

        if (batchItems.size > 1) {
          val joinedPayload = batchItems.joinToString("\nZXQBRQXZ\n") { it.protected.protectedText }
          var batchSegments: List<String>? = null
          try {
            val rawBatch = apiClient.translate(
                engine = settings.engine,
                text = joinedPayload,
                sourceLang = settings.sourceLang,
                targetLang = settings.targetLang
            )
            val splitSegs = rawBatch.split(batchSplitRegex).map { it.trim() }
            if (splitSegs.size == batchItems.size) {
              batchSegments = splitSegs
              _connectionStatus.value = ConnectionStatus(
                  "Batch ×${batchItems.size} (${settings.engine.displayName})",
                  ConnectionBadgeType.OK
              )
            }
          } catch (_: Exception) {
            batchSegments = null
          }

          if (batchSegments != null) {
            batchItems.forEachIndexed { bIdx, bItem ->
              val restored = WarbandTextProcessor.restoreWarbandTokens(
                  batchSegments[bIdx],
                  bItem.protected.tokens,
                  glossary
              )
              val polished = WarbandTextProcessor.applyMedievalTonePostProcessing(
                  restored,
                  settings.targetLang,
                  settings.medievalToneEnabled
              )
              val finalOut = if (bItem.split.hasCode) "${bItem.split.codePart}$sep$polished" else polished
              results.add(finalOut)
              fileTranslatedLines++
              pendingBuffer.add(
                  TranslationCacheEntity(
                      targetLang = settings.targetLang,
                      sourceText = bItem.split.textPart,
                      translatedText = polished
                  )
              )
            }

            if (pendingBuffer.size >= 15) {
              dao.upsertAll(pendingBuffer.toList())
              pendingBuffer.clear()
            }

            val doneNow = results.size
            val force = (doneNow == total)
            val p = sampleTelemetry(doneNow, forceSample = force)
            _progressState.value = p
            updateQueueItem(pendingItem.id) {
              it.copy(
                  results = results.toList(),
                  processedCount = doneNow,
                  cachedLinesCount = maxOf(it.cachedLinesCount, results.size)
              )
            }
            refreshPreviewFromLists(lines, results)
            onProgressUpdate(p)

            i += batchItems.size
            delay(settings.delayMs)
            continue
          }
        }

        // Single-line path (default or fallback when batch split mismatches)
        val protected = WarbandTextProcessor.protectWarbandTokens(split.textPart, glossary)
        val (translatedText, lastErrorMsg) = translateSingleWithRetry(
            protectedText = protected.protectedText,
            tokens = protected.tokens,
            lineNumber1Based = i + 1
        )

        if (shouldStopFlag.get()) break

        val finalOut: String
        if (translatedText != null) {
          finalOut = if (split.hasCode) "${split.codePart}$sep$translatedText" else translatedText
          fileTranslatedLines++
          pendingBuffer.add(
              TranslationCacheEntity(
                  targetLang = settings.targetLang,
                  sourceText = split.textPart,
                  translatedText = translatedText
              )
          )
        } else {
          finalOut = originalLine
          fileErrorsCount++
          _connectionStatus.value = ConnectionStatus("Koneksi Terputus / Limit", ConnectionBadgeType.ERROR)
          _errorLogEntries.update { current ->
            current + TranslationErrorItem(
                id = UUID.randomUUID().toString(),
                fileId = pendingItem.id,
                fileName = pendingItem.name,
                lineIndex = i,
                originalLine = originalLine,
                errorMessage = lastErrorMsg,
                isFormatError = false
            )
          }
          syncFailedRowsStrings()
          appendLog("Gagal baris ${i + 1} di ${pendingItem.name} ($lastErrorMsg)", LogLevel.ERROR)
          emitThrottledErrorAlert(
              title = "Gagal Menerjemahkan Baris",
              detail = "Teks asli dipertahankan sementara. Penyebab: $lastErrorMsg",
              lineInfo = "${pendingItem.name} · Baris #${i + 1}"
          )
        }

        results.add(finalOut)

        if (pendingBuffer.size >= 15) {
          dao.upsertAll(pendingBuffer.toList())
          pendingBuffer.clear()
        }

        val doneNow = results.size
        val force = (doneNow == total)
        val p = sampleTelemetry(doneNow, forceSample = force)
        _progressState.value = p

        if (doneNow % 3 == 0 || doneNow == total) {
          updateQueueItem(pendingItem.id) {
            it.copy(
                results = results.toList(),
                processedCount = doneNow,
                cachedLinesCount = maxOf(it.cachedLinesCount, results.size)
            )
          }
          refreshPreviewFromLists(lines, results)
          onProgressUpdate(p)
        }

        i++
        delay(settings.delayMs)
      }

      if (pendingBuffer.isNotEmpty()) {
        dao.upsertAll(pendingBuffer.toList())
        pendingBuffer.clear()
      }
      dao.trimOldEntries(15000)

      val fileDurationMs = (System.currentTimeMillis() - fileStartMs).coerceAtLeast(1L)
      val fileWordsCount = WarbandTextProcessor.countWordsInLines(
          lines = lines.take(results.size),
          separator = sep,
          stripTag = settings.stripTag,
          skipRegex = settings.skipRegex
      )
      sessionOriginalLines += total
      sessionTranslatedLines += fileTranslatedLines
      sessionSkippedLines += fileSkippedLines
      sessionErrors += fileErrorsCount
      sessionWords += fileWordsCount

      if (!shouldStopFlag.get()) {
        sessionFilesSucceeded++
        val completedResults = results.toList()
        updateQueueItem(pendingItem.id) {
          it.copy(
              status = FileQueueStatus.DONE,
              results = completedResults,
              processedCount = total,
              cachedLinesCount = total
          )
        }
        refreshPreviewFromLists(lines, completedResults)
        appendLog("Selesai: ${pendingItem.name} ($total baris)", LogLevel.OK)

        val srcJoin = lines.joinToString("\n").let { if (it.length <= 500_000) it else "" }
        val trJoin = completedResults.joinToString("\n").let { if (it.length <= 500_000) it else "" }
        historyRepo.recordSession(
            TranslationHistoryEntity(
                fileName = pendingItem.name,
                timestamp = System.currentTimeMillis(),
                status = "DONE",
                uriString = pendingItem.uriString,
                totalLines = total,
                processedLines = total,
                sourceLang = settings.sourceLang,
                targetLang = settings.targetLang,
                engineName = settings.engine.displayName,
                sourceSnapshot = srcJoin,
                translatedSnapshot = trJoin,
                wordsProcessed = fileWordsCount,
                durationMs = fileDurationMs
            )
        )
      } else {
        sessionFilesFailed++
        updateQueueItem(pendingItem.id) {
          it.copy(
              status = FileQueueStatus.WAIT,
              results = emptyList(),
              processedCount = results.size,
              cachedLinesCount = maxOf(it.cachedLinesCount, results.size)
          )
        }
        appendLog(
            "Dijeda/Dihentikan pada ${pendingItem.name} (${results.size}/$total baris). Auto-Resume siap!",
            LogLevel.WARN
        )

        val srcJoin = lines.joinToString("\n").let { if (it.length <= 500_000) it else "" }
        historyRepo.recordSession(
            TranslationHistoryEntity(
                fileName = pendingItem.name,
                timestamp = System.currentTimeMillis(),
                status = "STOPPED",
                uriString = pendingItem.uriString,
                totalLines = total,
                processedLines = results.size,
                sourceLang = settings.sourceLang,
                targetLang = settings.targetLang,
                engineName = settings.engine.displayName,
                sourceSnapshot = srcJoin,
                translatedSnapshot = "",
                wordsProcessed = fileWordsCount,
                durationMs = fileDurationMs
            )
        )
      }
    }

    val stoppedByUser = shouldStopFlag.get()
    isPausedFlag.set(false)
    shouldStopFlag.set(false)
    _progressState.update { it.copy(isRunning = false, isPaused = false) }
    val batchDurationMs = (System.currentTimeMillis() - batchStartMs).coerceAtLeast(1L)
    _sessionSummary.value = SessionSummaryReport(
        sessionId = System.currentTimeMillis(),
        completedAt = System.currentTimeMillis(),
        originalLines = sessionOriginalLines,
        translatedLines = sessionTranslatedLines,
        skippedLines = sessionSkippedLines,
        errorsEncountered = sessionErrors,
        totalDurationMs = batchDurationMs,
        wordsProcessed = sessionWords,
        filesSucceeded = sessionFilesSucceeded,
        filesFailed = sessionFilesFailed,
        stoppedEarly = stoppedByUser
    )
    refreshAutoResumeCounts(context, settings)
    if (!stoppedByUser) {
      _completionToast.value = "Semua file dalam antrean selesai diterjemahkan!"
    }
  }

  suspend fun retryErrorEntry(
      context: Context,
      settings: TranslatorSettings,
      errorId: String
  ): Boolean {
    val entry = _errorLogEntries.value.firstOrNull { it.id == errorId } ?: return false
    if (entry.isFormatError || entry.fileId == null || entry.lineIndex == null) return false

    _errorLogEntries.update { list ->
      list.map { if (it.id == errorId) it.copy(isRetrying = true) else it }
    }

    val glossary = WarbandTextProcessor.parseGlossary(settings.glossaryText)
    val sep = settings.separator.ifEmpty { "|" }
    val split = WarbandTextProcessor.splitLine(entry.originalLine, sep, settings.stripTag)
    val protected = WarbandTextProcessor.protectWarbandTokens(split.textPart, glossary)
    val fallbackPool = TranslationEngine.entries.filter { it != settings.engine }

    var translated: String? = null
    var lastError = "Koneksi gagal"
    val enginesToTry = if (settings.autoFallback) listOf(settings.engine) + fallbackPool else listOf(settings.engine)

    for (eng in enginesToTry) {
      try {
        val raw = apiClient.translate(
            engine = eng,
            text = protected.protectedText,
            sourceLang = settings.sourceLang,
            targetLang = settings.targetLang
        )
        val restored = WarbandTextProcessor.restoreWarbandTokens(raw, protected.tokens, glossary)
        translated = WarbandTextProcessor.applyMedievalTonePostProcessing(
            restored,
            settings.targetLang,
            settings.medievalToneEnabled
        )
        break
      } catch (e: Exception) {
        lastError = e.message ?: lastError
      }
    }

    return if (translated != null) {
      val finalLine = if (split.hasCode) "${split.codePart}$sep$translated" else translated
      updateLineInQueueFile(
          context = context,
          fileId = entry.fileId,
          lineIndex = entry.lineIndex,
          newTranslatedLine = finalLine,
          separator = sep,
          targetLang = settings.targetLang
      )
      _errorLogEntries.update { list -> list.filterNot { it.id == errorId } }
      syncFailedRowsStrings()
      _sessionSummary.update { current ->
        current?.copy(
            translatedLines = current.translatedLines + 1,
            errorsEncountered = (current.errorsEncountered - 1).coerceAtLeast(0)
        )
      }
      appendLog("Retry berhasil: ${entry.fileName} baris #${entry.lineIndex + 1}", LogLevel.OK)
      true
    } else {
      _errorLogEntries.update { list ->
        list.map { if (it.id == errorId) it.copy(isRetrying = false, errorMessage = lastError) else it }
      }
      syncFailedRowsStrings()
      appendLog("Retry gagal: ${entry.fileName} baris #${entry.lineIndex + 1} ($lastError)", LogLevel.ERROR)
      false
    }
  }

  suspend fun retryAllRowErrors(context: Context, settings: TranslatorSettings): Pair<Int, Int> {
    val rowErrors = _errorLogEntries.value.filter { !it.isFormatError && it.fileId != null && it.lineIndex != null }
    var succeeded = 0
    var failed = 0
    for (err in rowErrors) {
      if (retryErrorEntry(context, settings, err.id)) {
        succeeded++
      } else {
        failed++
      }
    }
    return succeeded to failed
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
