package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.local.TranslationHistoryEntity
import com.example.data.local.TranslationHistoryRepository
import com.example.domain.ConnectionBadgeType
import com.example.domain.ConnectionStatus
import com.example.domain.CsvStructurePreview
import com.example.domain.ErrorAlert
import com.example.domain.ExportDelimiter
import com.example.domain.ExportEncoding
import com.example.domain.FileQueueStatus
import com.example.domain.LogEntry
import com.example.domain.LogLevel
import com.example.domain.PendingExportPreviewItem
import com.example.domain.PreviewRow
import com.example.domain.QueuedFileItem
import com.example.domain.TranslationEngine
import com.example.domain.TranslationProgressState
import com.example.domain.TranslatorSettings
import com.example.domain.UiLanguage
import com.example.domain.WarbandTextProcessor
import com.example.service.TranslationCoordinator
import com.example.service.TranslationForegroundService
import com.example.util.AndroidFileHelper
import com.example.util.SaveOutcome
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiEvent {
  data class ShowToast(val message: String) : UiEvent()
  data class LaunchLegacyCreateDocument(
      val fileName: String,
      val content: String,
      val mimeType: String
  ) : UiEvent()
}

class TranslatorViewModel(application: Application) : AndroidViewModel(application) {
  private val appContext = application.applicationContext
  private val settingsStore = SettingsDataStore(appContext)
  private val db = AppDatabase.getInstance(appContext)
  private val cacheDao = db.translationCacheDao()
  private val historyRepository = TranslationHistoryRepository(db.translationHistoryDao())

  val settings: StateFlow<TranslatorSettings> = settingsStore.settingsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = TranslatorSettings()
  )

  val cacheCount: StateFlow<Int> = cacheDao.getCountFlow().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = 0
  )

  val historyEntries: StateFlow<List<TranslationHistoryEntity>> =
      historyRepository.allHistory.stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5000L),
          initialValue = emptyList()
      )

  val fileQueue: StateFlow<List<QueuedFileItem>> = TranslationCoordinator.fileQueue
  val progressState: StateFlow<TranslationProgressState> = TranslationCoordinator.progressState
  val previewRows: StateFlow<List<PreviewRow>> = TranslationCoordinator.previewRows
  val logs: StateFlow<List<LogEntry>> = TranslationCoordinator.logs
  val failedRows: StateFlow<List<String>> = TranslationCoordinator.failedRows
  val connectionStatus: StateFlow<ConnectionStatus> = TranslationCoordinator.connectionStatus
  val errorAlert: StateFlow<ErrorAlert?> = TranslationCoordinator.errorAlert

  private val _selectedPreviewFileId = MutableStateFlow<String?>(null)
  val selectedPreviewFileId: StateFlow<String?> = _selectedPreviewFileId.asStateFlow()

  val csvStructurePreview: StateFlow<CsvStructurePreview?> = combine(
      fileQueue,
      _selectedPreviewFileId,
      settings
  ) { queue, selectedId, currentSettings ->
    val targetFile = queue.firstOrNull { it.id == selectedId } ?: queue.firstOrNull()
    targetFile?.let {
      WarbandTextProcessor.buildCsvStructurePreview(
          file = it,
          separator = currentSettings.separator,
          stripTag = currentSettings.stripTag,
          skipRegex = currentSettings.skipRegex,
          maxRows = 10
      )
    }
  }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = null
  )

  private val _pendingExportItems = MutableStateFlow<List<QueuedFileItem>>(emptyList())

  val pendingExportPreviews: StateFlow<List<PendingExportPreviewItem>> = combine(
      _pendingExportItems,
      settings
  ) { items, s ->
    items.map { item ->
      val outName = WarbandTextProcessor.buildExportFileName(
          originalName = item.name,
          prefix = s.exportFilenamePrefix,
          suffix = s.exportFilenameSuffix
      )
      val formatted = WarbandTextProcessor.formatExportContent(
          lines = item.results,
          sourceSeparator = s.separator,
          exportDelimiter = s.exportDelimiter
      )
      PendingExportPreviewItem(
          fileId = item.id,
          originalFileName = item.name,
          outputFileName = outName,
          formattedContent = formatted,
          lineCount = item.results.size
      )
    }
  }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = emptyList()
  )

  private val _showSaveDialog = MutableStateFlow(false)
  val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

  private val _uiEvents = MutableSharedFlow<UiEvent>()
  val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

  private var legacySaveQueue: ArrayDeque<Pair<String, String>> = ArrayDeque()

  init {
    viewModelScope.launch {
      TranslationCoordinator.completionToast.collect { msg ->
        if (msg != null) {
          _uiEvents.emit(UiEvent.ShowToast(msg))
          TranslationCoordinator.consumeCompletionToast()
        }
      }
    }

    viewModelScope.launch {
      settings
          .distinctUntilChanged { old, new ->
            old.targetLang == new.targetLang &&
                old.separator == new.separator &&
                old.stripTag == new.stripTag &&
                old.skipRegex == new.skipRegex
          }
          .collect { currentSettings ->
            if (fileQueue.value.isNotEmpty() && !progressState.value.isRunning) {
              TranslationCoordinator.refreshAutoResumeCounts(appContext, currentSettings)
            }
          }
    }
  }

  fun setUiLanguage(lang: UiLanguage) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(uiLanguage = lang) }
    }
  }

  fun selectEngine(engine: TranslationEngine) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(engine = engine) }
      TranslationCoordinator.setConnectionStatus(
          "Mesin aktif: ${engine.displayName}",
          ConnectionBadgeType.NEUTRAL
      )
    }
  }

  fun setDelayMs(delayMs: Long) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(delayMs = delayMs) }
    }
  }

  fun setAutoFallback(enabled: Boolean) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(autoFallback = enabled) }
    }
  }

  fun setSourceLang(code: String) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(sourceLang = code) }
    }
  }

  fun setTargetLang(code: String) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(targetLang = code) }
    }
  }

  fun setSeparator(sep: String) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(separator = sep.take(3)) }
    }
  }

  fun setStripTag(tag: String) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(stripTag = tag.take(16)) }
    }
  }

  fun setSkipRegex(regex: String) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(skipRegex = regex) }
    }
  }

  fun setGlossaryText(text: String) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(glossaryText = text) }
    }
  }

  fun setExportDelimiter(delimiter: ExportDelimiter) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(exportDelimiter = delimiter) }
    }
  }

  fun setExportEncoding(encoding: ExportEncoding) {
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(exportEncoding = encoding) }
    }
  }

  fun setExportFilenamePrefix(prefix: String) {
    val clean = WarbandTextProcessor.sanitizeFilenameAffix(prefix)
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(exportFilenamePrefix = clean) }
    }
  }

  fun setExportFilenameSuffix(suffix: String) {
    val clean = WarbandTextProcessor.sanitizeFilenameAffix(suffix)
    viewModelScope.launch {
      settingsStore.updateSettings { it.copy(exportFilenameSuffix = clean) }
    }
  }

  fun deleteHistoryEntry(id: Long) {
    viewModelScope.launch {
      historyRepository.deleteEntry(id)
    }
  }

  fun clearHistory() {
    viewModelScope.launch {
      historyRepository.clearAllHistory()
      _uiEvents.emit(UiEvent.ShowToast("Riwayat sesi telah dibersihkan."))
    }
  }

  fun reopenHistoryEntry(entry: TranslationHistoryEntity, onReopened: () -> Unit) {
    viewModelScope.launch {
      if (progressState.value.isRunning) {
        _uiEvents.emit(UiEvent.ShowToast("Tunggu proses terjemahan aktif selesai terlebih dahulu."))
        return@launch
      }

      val srcLines = if (entry.sourceSnapshot.isNotEmpty()) {
        entry.sourceSnapshot.split('\n').filter { it.trim().isNotEmpty() }
      } else {
        entry.uriString?.let { uriStr ->
          runCatching {
            AndroidFileHelper.readTextFromUri(appContext, Uri.parse(uriStr))
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .split('\n')
                .filter { it.trim().isNotEmpty() }
          }.getOrNull()
        } ?: emptyList()
      }

      if (srcLines.isEmpty()) {
        _uiEvents.emit(UiEvent.ShowToast("File asli '${entry.fileName}' tidak lagi dapat diakses di penyimpanan."))
        return@launch
      }

      val trLines = if (entry.status == "DONE" && entry.translatedSnapshot.isNotEmpty()) {
        entry.translatedSnapshot.split('\n').filter { it.trim().isNotEmpty() }
      } else {
        emptyList()
      }

      val restoredStatus = if (trLines.size == srcLines.size && trLines.isNotEmpty()) {
        FileQueueStatus.DONE
      } else {
        FileQueueStatus.WAIT
      }

      val item = QueuedFileItem(
          id = UUID.randomUUID().toString(),
          name = entry.fileName,
          uriString = entry.uriString,
          lines = srcLines,
          results = trLines,
          status = restoredStatus,
          cachedLinesCount = if (restoredStatus == FileQueueStatus.DONE) srcLines.size else entry.processedLines,
          processedCount = if (restoredStatus == FileQueueStatus.DONE) srcLines.size else entry.processedLines
      )

      TranslationCoordinator.enqueueRestoredItem(item)
      TranslationCoordinator.refreshAutoResumeCounts(appContext, settings.value)
      _selectedPreviewFileId.value = item.id
      _uiEvents.emit(UiEvent.ShowToast("Dimuat ulang: ${entry.fileName}"))
      onReopened()
    }
  }

  fun clearGlossary() {
    setGlossaryText("")
    viewModelScope.launch {
      _uiEvents.emit(UiEvent.ShowToast("Kolom glosarium dikosongkan."))
    }
  }

  fun clearCache() {
    viewModelScope.launch {
      cacheDao.clearAll()
      TranslationCoordinator.refreshAutoResumeCounts(appContext, settings.value)
      TranslationCoordinator.appendLog("Cache memori telah dihapus.", LogLevel.WARN)
      _uiEvents.emit(UiEvent.ShowToast("Cache terjemahan berhasil dibersihkan."))
    }
  }

  fun selectPreviewFile(fileId: String) {
    _selectedPreviewFileId.value = fileId
  }

  fun handlePickedFiles(uris: List<Uri>) {
    if (uris.isEmpty()) return
    viewModelScope.launch {
      val outcome = AndroidFileHelper.readQueuedFiles(appContext, uris)
      if (outcome.loaded.isNotEmpty()) {
        TranslationCoordinator.enqueueFiles(outcome.loaded)
        TranslationCoordinator.refreshAutoResumeCounts(appContext, settings.value)
        if (_selectedPreviewFileId.value == null) {
          _selectedPreviewFileId.value = outcome.loaded.first().id
        } else {
          _selectedPreviewFileId.value = outcome.loaded.last().id
        }
      }
      outcome.rejectedNames.forEach { name ->
        TranslationCoordinator.appendLog("Format tidak didukung: $name", LogLevel.ERROR)
        _uiEvents.emit(UiEvent.ShowToast("Format tidak didukung: $name (gunakan .csv, .txt, .tsv)"))
      }
      outcome.failedNames.forEach { name ->
        TranslationCoordinator.appendLog("Gagal membaca file: $name", LogLevel.ERROR)
        _uiEvents.emit(UiEvent.ShowToast("Gagal membaca file: $name"))
      }
    }
  }

  fun removeQueueItem(id: String) {
    TranslationCoordinator.removeQueueItem(id)
    if (_selectedPreviewFileId.value == id) {
      _selectedPreviewFileId.value = fileQueue.value.firstOrNull()?.id
    }
  }

  fun moveQueueItemUp(id: String) = TranslationCoordinator.moveQueueItem(id, -1)

  fun moveQueueItemDown(id: String) = TranslationCoordinator.moveQueueItem(id, +1)

  fun retryQueueItem(id: String) = TranslationCoordinator.retryQueueItem(id)

  fun clearQueue() {
    TranslationCoordinator.clearQueue()
    _selectedPreviewFileId.value = null
  }

  fun startTranslation() {
    if (progressState.value.isRunning) return
    val hasPending = fileQueue.value.any { it.status == FileQueueStatus.WAIT }
    if (!hasPending) return
    TranslationForegroundService.start(appContext)
  }

  fun togglePause() = TranslationCoordinator.togglePause()

  fun stopTranslation() = TranslationCoordinator.requestStop()

  fun clearLogs() = TranslationCoordinator.clearLogs()

  fun dismissErrorAlert() = TranslationCoordinator.dismissErrorAlert()

  fun savePreviewEdit(lineIndex: Int, newTranslatedLine: String) {
    viewModelScope.launch {
      val s = settings.value
      TranslationCoordinator.updateManualPreviewLine(
          context = appContext,
          lineIndex = lineIndex,
          newTranslatedLine = newTranslatedLine,
          separator = s.separator,
          targetLang = s.targetLang
      )
      _uiEvents.emit(UiEvent.ShowToast("Baris #${lineIndex + 1} diperbarui."))
    }
  }

  fun importGlossaryFromUri(uri: Uri) {
    viewModelScope.launch {
      try {
        val text = AndroidFileHelper.readTextFromUri(appContext, uri)
        setGlossaryText(text)
        val name = AndroidFileHelper.getDisplayName(appContext, uri) ?: "glosarium.txt"
        _uiEvents.emit(UiEvent.ShowToast("Glosarium \"$name\" dimuat!"))
      } catch (e: Exception) {
        _uiEvents.emit(UiEvent.ShowToast("Gagal memuat glosarium: ${e.message}"))
      }
    }
  }

  fun exportGlossaryToUri(uri: Uri) {
    viewModelScope.launch {
      try {
        AndroidFileHelper.writeTextToUri(appContext, uri, settings.value.glossaryText)
        _uiEvents.emit(UiEvent.ShowToast("Glosarium berhasil diekspor!"))
      } catch (e: Exception) {
        _uiEvents.emit(UiEvent.ShowToast("Gagal mengekspor glosarium: ${e.message}"))
      }
    }
  }

  fun exportErrorLogToUri(uri: Uri) {
    viewModelScope.launch {
      val rows = failedRows.value
      if (rows.isEmpty()) return@launch
      try {
        AndroidFileHelper.writeTextToUri(appContext, uri, rows.joinToString("\r\n") + "\r\n")
        _uiEvents.emit(UiEvent.ShowToast("Log baris gagal diekspor!"))
      } catch (e: Exception) {
        _uiEvents.emit(UiEvent.ShowToast("Gagal mengekspor log: ${e.message}"))
      }
    }
  }

  fun openSaveForSingleFile(item: QueuedFileItem) {
    if (item.status != FileQueueStatus.DONE || item.results.isEmpty()) return
    _pendingExportItems.value = listOf(item)
    _showSaveDialog.value = true
  }

  fun openSaveForAllDone() {
    val doneItems = fileQueue.value.filter { it.status == FileQueueStatus.DONE && it.results.isNotEmpty() }
    if (doneItems.isEmpty()) return
    _pendingExportItems.value = doneItems
    _showSaveDialog.value = true
  }

  fun dismissSaveDialog() {
    _showSaveDialog.value = false
  }

  private fun buildPendingSavePairs(): List<Pair<String, String>> {
    val s = settings.value
    return _pendingExportItems.value.map { item ->
      val outName = WarbandTextProcessor.buildExportFileName(
          originalName = item.name,
          prefix = s.exportFilenamePrefix,
          suffix = s.exportFilenameSuffix
      )
      val formatted = WarbandTextProcessor.formatExportContent(
          lines = item.results,
          sourceSeparator = s.separator,
          exportDelimiter = s.exportDelimiter
      )
      outName to formatted
    }
  }

  fun savePendingToDownloads() {
    val files = buildPendingSavePairs()
    _showSaveDialog.value = false
    if (files.isEmpty()) return

    val charset = settings.value.exportEncoding.toCharset()
    viewModelScope.launch {
      when (val outcome = AndroidFileHelper.saveToDownloads(appContext, files, charset = charset)) {
        is SaveOutcome.Saved -> {
          _uiEvents.emit(
              UiEvent.ShowToast("${outcome.count} file (${settings.value.exportEncoding.charsetName}) disimpan ke ${outcome.locationLabel}")
          )
        }
        is SaveOutcome.RequiresLegacyPicker -> {
          legacySaveQueue = ArrayDeque(outcome.pendingFiles)
          triggerNextLegacySave()
        }
        is SaveOutcome.Failed -> {
          _uiEvents.emit(UiEvent.ShowToast(outcome.errorMessage))
        }
      }
    }
  }

  fun savePendingToTreeUri(treeUri: Uri) {
    val files = buildPendingSavePairs()
    _showSaveDialog.value = false
    if (files.isEmpty()) return

    val charset = settings.value.exportEncoding.toCharset()
    viewModelScope.launch {
      when (val outcome = AndroidFileHelper.saveToFolderTree(appContext, treeUri, files, charset = charset)) {
        is SaveOutcome.Saved -> {
          _uiEvents.emit(
              UiEvent.ShowToast("${outcome.count} file (${settings.value.exportEncoding.charsetName}) berhasil disimpan ke folder tujuan!")
          )
        }
        is SaveOutcome.Failed -> {
          _uiEvents.emit(UiEvent.ShowToast(outcome.errorMessage))
        }
        else -> Unit
      }
    }
  }

  fun handleLegacySaveUri(uri: Uri?) {
    val current = legacySaveQueue.removeFirstOrNull() ?: return
    if (uri == null) {
      legacySaveQueue.clear()
      return
    }
    val charset = settings.value.exportEncoding.toCharset()
    viewModelScope.launch {
      try {
        AndroidFileHelper.writeTextToUri(appContext, uri, current.second, charset = charset)
        if (legacySaveQueue.isEmpty()) {
          _uiEvents.emit(UiEvent.ShowToast("File (${settings.value.exportEncoding.charsetName}) berhasil disimpan!"))
        } else {
          triggerNextLegacySave()
        }
      } catch (e: Exception) {
        _uiEvents.emit(UiEvent.ShowToast("Gagal menyimpan: ${e.message}"))
      }
    }
  }

  private suspend fun triggerNextLegacySave() {
    val next = legacySaveQueue.firstOrNull() ?: return
    _uiEvents.emit(UiEvent.LaunchLegacyCreateDocument(next.first, next.second, "text/csv"))
  }
}
