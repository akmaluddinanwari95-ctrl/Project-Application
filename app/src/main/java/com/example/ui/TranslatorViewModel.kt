package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
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
import com.example.domain.UiLanguage
import com.example.service.TranslationCoordinator
import com.example.service.TranslationForegroundService
import com.example.util.AndroidFileHelper
import com.example.util.SaveOutcome
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
  private val cacheDao = AppDatabase.getInstance(appContext).translationCacheDao()

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

  val fileQueue: StateFlow<List<QueuedFileItem>> = TranslationCoordinator.fileQueue
  val progressState: StateFlow<TranslationProgressState> = TranslationCoordinator.progressState
  val previewRows: StateFlow<List<PreviewRow>> = TranslationCoordinator.previewRows
  val logs: StateFlow<List<LogEntry>> = TranslationCoordinator.logs
  val failedRows: StateFlow<List<String>> = TranslationCoordinator.failedRows
  val connectionStatus: StateFlow<ConnectionStatus> = TranslationCoordinator.connectionStatus
  val errorAlert: StateFlow<ErrorAlert?> = TranslationCoordinator.errorAlert

  private val _pendingSaveFiles = MutableStateFlow<List<Pair<String, String>>>(emptyList())
  val pendingSaveFiles: StateFlow<List<Pair<String, String>>> = _pendingSaveFiles.asStateFlow()

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

  fun clearGlossary() {
    setGlossaryText("")
    viewModelScope.launch {
      _uiEvents.emit(UiEvent.ShowToast("Kolom glosarium dikosongkan."))
    }
  }

  fun clearCache() {
    viewModelScope.launch {
      cacheDao.clearAll()
      TranslationCoordinator.appendLog("Cache memori telah dihapus.", LogLevel.WARN)
      _uiEvents.emit(UiEvent.ShowToast("Cache terjemahan berhasil dibersihkan."))
    }
  }

  fun handlePickedFiles(uris: List<Uri>) {
    if (uris.isEmpty()) return
    viewModelScope.launch {
      val outcome = AndroidFileHelper.readQueuedFiles(appContext, uris)
      if (outcome.loaded.isNotEmpty()) {
        TranslationCoordinator.enqueueFiles(outcome.loaded)
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

  fun removeQueueItem(id: String) = TranslationCoordinator.removeQueueItem(id)

  fun clearQueue() = TranslationCoordinator.clearQueue()

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
    val content = item.results.joinToString("\r\n") + "\r\n"
    _pendingSaveFiles.value = listOf(item.name to content)
    _showSaveDialog.value = true
  }

  fun openSaveForAllDone() {
    val doneItems = fileQueue.value.filter { it.status == FileQueueStatus.DONE && it.results.isNotEmpty() }
    if (doneItems.isEmpty()) return
    _pendingSaveFiles.value = doneItems.map { item ->
      item.name to (item.results.joinToString("\r\n") + "\r\n")
    }
    _showSaveDialog.value = true
  }

  fun dismissSaveDialog() {
    _showSaveDialog.value = false
  }

  fun savePendingToDownloads() {
    val files = _pendingSaveFiles.value
    _showSaveDialog.value = false
    if (files.isEmpty()) return

    viewModelScope.launch {
      when (val outcome = AndroidFileHelper.saveToDownloads(appContext, files)) {
        is SaveOutcome.Saved -> {
          _uiEvents.emit(
              UiEvent.ShowToast("${outcome.count} file disimpan ke ${outcome.locationLabel}")
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
    val files = _pendingSaveFiles.value
    _showSaveDialog.value = false
    if (files.isEmpty()) return

    viewModelScope.launch {
      when (val outcome = AndroidFileHelper.saveToFolderTree(appContext, treeUri, files)) {
        is SaveOutcome.Saved -> {
          _uiEvents.emit(
              UiEvent.ShowToast("${outcome.count} file berhasil disimpan ke folder tujuan!")
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
    viewModelScope.launch {
      try {
        AndroidFileHelper.writeTextToUri(appContext, uri, current.second)
        if (legacySaveQueue.isEmpty()) {
          _uiEvents.emit(UiEvent.ShowToast("File berhasil disimpan!"))
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
