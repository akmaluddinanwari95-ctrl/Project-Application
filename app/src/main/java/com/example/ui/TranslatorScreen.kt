package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.BatchSizeOptions
import com.example.domain.CsvValidationReport
import com.example.domain.CsvValidationStatus
import com.example.domain.DelayOptions
import com.example.domain.FileQueueStatus
import com.example.domain.GlossaryPreset
import com.example.domain.I18nStrings
import com.example.domain.LanguageOption
import com.example.domain.LogLevel
import com.example.domain.PreviewRow
import com.example.domain.QueuedFileItem
import com.example.domain.SupportedLanguages
import com.example.domain.TranslationEngine
import com.example.domain.TranslationProgressState
import com.example.domain.TranslatorSettings
import com.example.domain.UiLanguage
import com.example.domain.WarbandTextProcessor
import com.example.ui.theme.AmberBg
import com.example.ui.theme.AmberText
import com.example.ui.theme.Burgundy
import com.example.ui.theme.BurgundyBorder
import com.example.ui.theme.BurgundyBorderSoft
import com.example.ui.theme.Crimson
import com.example.ui.theme.CrimsonText
import com.example.ui.theme.Forest
import com.example.ui.theme.ForestText
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.LocalWarbandPalette
import com.example.ui.theme.MahoganyHeader
import com.example.ui.theme.MahoganyInput
import com.example.ui.theme.Obsidian
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.Parchment
import com.example.ui.theme.ParchmentDim

private enum class AppTab {
  TRANSLATOR,
  HISTORY,
  SETTINGS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel,
    snackbarHostState: SnackbarHostState,
    onPickCsvFiles: () -> Unit,
    onPickSingleCsvFile: () -> Unit,
    onImportGlossary: () -> Unit,
    onExportGlossary: () -> Unit,
    onExportErrorLog: () -> Unit,
    onExportCacheBackup: () -> Unit,
    onImportCacheBackup: () -> Unit,
    onPickDiffNewMod: () -> Unit,
    onPickDiffOldTrans: () -> Unit,
    onPickSaveFolder: () -> Unit,
    onStartWithNotificationCheck: () -> Unit
) {
  val pal = LocalWarbandPalette.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val targetLanguages by viewModel.targetLanguages.collectAsStateWithLifecycle()
  val cacheCount by viewModel.cacheCount.collectAsStateWithLifecycle()
  val historyEntries by viewModel.historyEntries.collectAsStateWithLifecycle()
  val filteredHistoryEntries by viewModel.filteredHistoryEntries.collectAsStateWithLifecycle()
  val historySearchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
  val historyStatusFilter by viewModel.historyStatusFilter.collectAsStateWithLifecycle()
  val dailyProductivityStats by viewModel.dailyProductivityStats.collectAsStateWithLifecycle()
  val fileQueue by viewModel.fileQueue.collectAsStateWithLifecycle()
  val progressState by viewModel.progressState.collectAsStateWithLifecycle()
  val previewRows by viewModel.previewRows.collectAsStateWithLifecycle()
  val csvStructurePreview by viewModel.csvStructurePreview.collectAsStateWithLifecycle()
  val validationReports by viewModel.validationReports.collectAsStateWithLifecycle()
  val showValidationWarningDialog by viewModel.showValidationWarningDialog.collectAsStateWithLifecycle()
  val validationWarningReports by viewModel.validationWarningReports.collectAsStateWithLifecycle()
  val logs by viewModel.logs.collectAsStateWithLifecycle()
  val errorLogEntries by viewModel.errorLogEntries.collectAsStateWithLifecycle()
  val failedRows by viewModel.failedRows.collectAsStateWithLifecycle()
  val sessionSummary by viewModel.sessionSummary.collectAsStateWithLifecycle()
  val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
  val errorAlert by viewModel.errorAlert.collectAsStateWithLifecycle()
  val pendingExportPreviews by viewModel.pendingExportPreviews.collectAsStateWithLifecycle()
  val showSaveDialog by viewModel.showSaveDialog.collectAsStateWithLifecycle()
  val qaAuditSummary by viewModel.qaAuditSummary.collectAsStateWithLifecycle()
  val showModDiffDialog by viewModel.showModDiffDialog.collectAsStateWithLifecycle()
  val diffNewModName by viewModel.diffNewModName.collectAsStateWithLifecycle()
  val diffOldTransName by viewModel.diffOldTransName.collectAsStateWithLifecycle()
  val diffMergePreview by viewModel.diffMergePreview.collectAsStateWithLifecycle()

  val lang = settings.uiLanguage
  fun tr(key: String) = I18nStrings.get(lang, key)

  var activeTab by remember { mutableStateOf(AppTab.TRANSLATOR) }
  var activeHelpTopicKey by remember { mutableStateOf<String?>(null) }
  var glossaryExpanded by remember { mutableStateOf(false) }
  var guideExpanded by remember { mutableStateOf(false) }
  var showAddCustomLangDialog by remember { mutableStateOf(false) }

  BackHandler(enabled = activeTab != AppTab.TRANSLATOR) {
    activeTab = AppTab.TRANSLATOR
  }

  val glossaryRules = remember(settings.glossaryText) {
    WarbandTextProcessor.parseGlossary(settings.glossaryText)
  }
  val hasWaitingFiles = remember(fileQueue) { fileQueue.any { it.status == FileQueueStatus.WAIT } }
  val hasDoneFiles = remember(fileQueue) { fileQueue.any { it.status == FileQueueStatus.DONE } }
  val isRegexValid = remember(settings.skipRegex) { WarbandTextProcessor.isValidRegex(settings.skipRegex) }

  Scaffold(
      containerColor = pal.bgBottom,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        TopMedievalHeader(
            navTitle = tr("navTitle"),
            navSub = tr("navSub"),
            activeLang = lang,
            onSelectLang = { viewModel.setUiLanguage(it) },
            onOpenHelp = { activeHelpTopicKey = it }
        )
      },
      bottomBar = {
        Column(modifier = Modifier.fillMaxWidth()) {
          AnimatedVisibility(visible = progressState.isRunning) {
            StickyProgressControlBar(
                progress = progressState,
                pauseLabel = if (progressState.isPaused) tr("btnResume") else tr("btnPause"),
                onTogglePause = { viewModel.togglePause() },
                onStop = { viewModel.stopTranslation() }
            )
          }
          HorizontalDivider(color = pal.border, thickness = 1.dp)
          NavigationBar(
              containerColor = pal.headerBg,
              contentColor = pal.textPrimary
          ) {
            val navColors = NavigationBarItemDefaults.colors(
                selectedIconColor = pal.primaryAccent,
                selectedTextColor = pal.primaryAccent,
                indicatorColor = pal.actionBg.copy(alpha = 0.55f),
                unselectedIconColor = pal.textSecondary,
                unselectedTextColor = pal.textSecondary
            )
            NavigationBarItem(
                selected = activeTab == AppTab.TRANSLATOR,
                onClick = { activeTab = AppTab.TRANSLATOR },
                icon = {
                  Icon(
                      imageVector = if (activeTab == AppTab.TRANSLATOR) Icons.Filled.Translate else Icons.Outlined.Translate,
                      contentDescription = tr("tabTranslator")
                  )
                },
                label = { Text(tr("tabTranslator"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = navColors,
                modifier = Modifier.testTag("nav_tab_translator")
            )
            NavigationBarItem(
                selected = activeTab == AppTab.HISTORY,
                onClick = { activeTab = AppTab.HISTORY },
                icon = {
                  Icon(
                      imageVector = if (activeTab == AppTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                      contentDescription = tr("tabHistory")
                  )
                },
                label = { Text(tr("tabHistory"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = navColors,
                modifier = Modifier.testTag("nav_tab_history")
            )
            NavigationBarItem(
                selected = activeTab == AppTab.SETTINGS,
                onClick = { activeTab = AppTab.SETTINGS },
                icon = {
                  Icon(
                      imageVector = if (activeTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                      contentDescription = tr("tabSettings")
                  )
                },
                label = { Text(tr("tabSettings"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = navColors,
                modifier = Modifier.testTag("nav_tab_settings")
            )
          }
        }
      }
  ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(pal.bgTop, pal.bgMid, pal.bgBottom)
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(pal.parchmentGlow.copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(500f, 0f),
                    radius = 1250f
                )
            )
            .padding(innerPadding)
    ) {
      when (activeTab) {
        AppTab.HISTORY -> {
          HistoryScreenContent(
              historyEntries = filteredHistoryEntries,
              isRunning = progressState.isRunning,
              tr = ::tr,
              onReopenEntry = { entry ->
                viewModel.reopenHistoryEntry(entry) {
                  activeTab = AppTab.TRANSLATOR
                }
              },
              onDeleteEntry = { viewModel.deleteHistoryEntry(it) },
              onClearAll = { viewModel.clearHistory() },
              onOpenHelp = { activeHelpTopicKey = it },
              searchQuery = historySearchQuery,
              onSearchQueryChange = { viewModel.setHistorySearchQuery(it) },
              statusFilter = historyStatusFilter,
              onStatusFilterChange = { viewModel.setHistoryStatusFilter(it) },
              dailyStats = dailyProductivityStats,
              totalDoneCount = historyEntries.count { it.status == "DONE" },
              totalFailedCount = historyEntries.count { it.status != "DONE" }
          )
        }
        AppTab.SETTINGS -> {
          SettingsScreenContent(
              settings = settings,
              cacheCount = cacheCount,
              tr = ::tr,
              onSelectExportDelimiter = { viewModel.setExportDelimiter(it) },
              onSelectExportEncoding = { viewModel.setExportEncoding(it) },
              onExportPrefixChange = { viewModel.setExportFilenamePrefix(it) },
              onExportSuffixChange = { viewModel.setExportFilenameSuffix(it) },
              onSelectUiLanguage = { viewModel.setUiLanguage(it) },
              onToggleMedievalTheme = { viewModel.setMedievalThemeEnabled(it) },
              onClearCache = { viewModel.clearCache() },
              onExportCacheBackup = onExportCacheBackup,
              onImportCacheBackup = onImportCacheBackup,
              onOpenHelp = { activeHelpTopicKey = it }
          )
        }
        AppTab.TRANSLATOR -> {
          Column(
              modifier = Modifier
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState())
                  .padding(horizontal = 14.dp, vertical = 14.dp),
              horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Column(
                modifier = Modifier.widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
          ErrorAlertBanner(
              alert = errorAlert,
              onDismiss = { viewModel.dismissErrorAlert() }
          )

          // Hero Banner
          HeroBannerSection(
              badge = tr("heroBadge"),
              title = tr("heroTitle"),
              description = tr("heroDesc")
          )

          // 1. Mesin Terjemahan & Kecepatan
          EngineSection(
              settings = settings,
              cacheCount = cacheCount,
              connectionStatus = connectionStatus,
              tr = ::tr,
              onSelectEngine = { viewModel.selectEngine(it) },
              onSelectDelay = { viewModel.setDelayMs(it) },
              onSelectBatchSize = { viewModel.setBatchSize(it) },
              onToggleFallback = { viewModel.setAutoFallback(it) },
              onClearCache = { viewModel.clearCache() },
              onOpenHelp = { activeHelpTopicKey = it }
          )

          // 2. Pengaturan Bahasa & Format Warband
          FormatSection(
              settings = settings,
              targetLanguages = targetLanguages,
              isRegexValid = isRegexValid,
              tr = ::tr,
              onSelectSourceLang = { viewModel.setSourceLang(it) },
              onSelectTargetLang = { viewModel.setTargetLang(it) },
              onOpenAddCustomLang = { showAddCustomLangDialog = true },
              onSeparatorChange = { viewModel.setSeparator(it) },
              onStripTagChange = { viewModel.setStripTag(it) },
              onSkipRegexChange = { viewModel.setSkipRegex(it) },
              onToggleMedievalTone = { viewModel.setMedievalToneEnabled(it) },
              onOpenHelp = { activeHelpTopicKey = it }
          )

          // 3. Glosarium Kustom (Collapsible)
          MedievalAccordionCard(
              title = tr("secGloss"),
              leadingEmoji = "📖",
              helpTopicKey = "secGloss",
              expanded = glossaryExpanded,
              onToggle = { glossaryExpanded = !glossaryExpanded },
              onOpenHelp = { activeHelpTopicKey = it },
              headerTestTag = "glossary_accordion_header",
              trailingBadge = {
                val count = glossaryRules.totalCount
                Surface(
                    shape = CircleShape,
                    color = if (count > 0) Burgundy.copy(alpha = 0.5f) else Color(0xFF190F0E),
                    border = BorderStroke(1.dp, if (count > 0) GoldAccent else BurgundyBorder)
                ) {
                  Text(
                      text = "$count ${tr("glossBadgeUnit")}",
                      color = if (count > 0) GoldAccent else ParchmentDim,
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
              }
          ) {
            Text(
                text = tr("glossHint"),
                style = MaterialTheme.typography.bodySmall,
                color = ParchmentDim
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = settings.glossaryText,
                onValueChange = { viewModel.setGlossaryText(it) },
                placeholder = {
                  Text(
                      text = tr("glossPlaceholder"),
                      style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                      color = ParchmentDim.copy(alpha = 0.45f)
                  )
                },
                minLines = 4,
                maxLines = 8,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Parchment
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = BurgundyBorder,
                    focusedContainerColor = ObsidianDeep,
                    unfocusedContainerColor = ObsidianDeep,
                    cursorColor = GoldAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("glossary_text_field")
            )
            Text(
                text = tr("lblGlossPresets"),
                style = MaterialTheme.typography.labelSmall,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              WarbandTextProcessor.builtInGlossaryPresets.forEach { preset ->
                OutlinedButton(
                    onClick = { viewModel.applyGlossaryPreset(preset) },
                    border = BorderStroke(1.dp, GoldMuted),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF1F1211),
                        contentColor = GoldAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("glossary_preset_${preset.id}")
                ) {
                  Text(tr(preset.titleKey), fontSize = 11.sp, maxLines = 1)
                }
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                  onClick = onExportGlossary,
                  border = BorderStroke(1.dp, Burgundy),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Parchment),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                  modifier = Modifier
                      .weight(1f)
                      .minimumInteractiveComponentSize()
                      .testTag("export_glossary_button")
              ) {
                Text("📤 ${tr("btnExpGloss")}", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
              }
              OutlinedButton(
                  onClick = onImportGlossary,
                  border = BorderStroke(1.dp, Burgundy),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Parchment),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                  modifier = Modifier
                      .weight(1f)
                      .minimumInteractiveComponentSize()
                      .testTag("import_glossary_button")
              ) {
                Text("📥 ${tr("btnImpGloss")}", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
              }
              OutlinedButton(
                  onClick = { viewModel.clearGlossary() },
                  border = BorderStroke(1.dp, Crimson),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonText),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                  modifier = Modifier
                      .minimumInteractiveComponentSize()
                      .testTag("clear_glossary_button")
              ) {
                Text("🗑️ ${tr("btnClearGloss")}", maxLines = 1, fontSize = 12.sp)
              }
            }
          }

          // 4. Unggah & Antrean File CSV
          QueueSection(
              fileQueue = fileQueue,
              validationReports = validationReports,
              selectedPreviewFileId = csvStructurePreview?.fileId,
              isRunning = progressState.isRunning,
              exportPrefix = settings.exportFilenamePrefix,
              exportSuffix = settings.exportFilenameSuffix,
              tr = ::tr,
              onPickFiles = onPickCsvFiles,
              onPickSingleFile = onPickSingleCsvFile,
              onOpenModDiff = { viewModel.openModDiffDialog() },
              onSelectPreviewFile = { viewModel.selectPreviewFile(it) },
              onOpenQaEditor = { viewModel.openFullEditorForFile(it) },
              onMoveUp = { viewModel.moveQueueItemUp(it) },
              onMoveDown = { viewModel.moveQueueItemDown(it) },
              onRetryItem = { viewModel.retryQueueItem(it) },
              onClearQueue = { viewModel.clearQueue() },
              onRemoveItem = { viewModel.removeQueueItem(it) },
              onSaveItem = { viewModel.openSaveForSingleFile(it) },
              onOpenHelp = { activeHelpTopicKey = it }
          )

          // 4b. Pratinjau Struktur CSV (10 Baris Pertama)
          CsvPreviewerCard(
              fileQueue = fileQueue,
              preview = csvStructurePreview,
              tr = ::tr,
              onSelectFile = { viewModel.selectPreviewFile(it) },
              onOpenHelp = { activeHelpTopicKey = it }
          )

          // 5. Kontrol Proses & Progress Bar
          ControlsAndProgressSection(
              progress = progressState,
              hasWaitingFiles = hasWaitingFiles,
              hasDoneFiles = hasDoneFiles,
              failedCount = errorLogEntries.size.coerceAtLeast(failedRows.size),
              tr = ::tr,
              onStart = onStartWithNotificationCheck,
              onTogglePause = { viewModel.togglePause() },
              onStop = { viewModel.stopTranslation() },
              onOpenQaEditor = { viewModel.openFullEditorForFirstDone() },
              onSaveAll = { viewModel.openSaveForAllDone() },
              onExportErrors = onExportErrorLog
          )

          // 5b. Grafik Kecepatan & Progres Real-Time
          TelemetryChartCard(
              progress = progressState,
              tr = ::tr,
              onOpenHelp = { activeHelpTopicKey = it }
          )

          // 5c. Dasbor Ringkasan Sesi Pasca-Batch + Produktivitas
          val activeSummary = sessionSummary
          if (activeSummary != null && !progressState.isRunning) {
            SessionSummaryDashboardCard(
                summary = activeSummary,
                dailyStats = dailyProductivityStats,
                tr = ::tr,
                onDismiss = { viewModel.dismissSessionSummary() },
                onOpenHelp = { activeHelpTopicKey = it }
            )
          }

          // 5d. Log Error Terjemahan (Inspect & Coba Ulang per Baris)
          ErrorLogSectionCard(
              entries = errorLogEntries,
              isRunning = progressState.isRunning,
              tr = ::tr,
              onRetryEntry = { viewModel.retryErrorEntry(it) },
              onRetryAllRowErrors = { viewModel.retryAllErrors() },
              onDismissEntry = { viewModel.dismissErrorEntry(it) },
              onClearAll = { viewModel.clearErrorLog() },
              onExportErrors = onExportErrorLog,
              onOpenHelp = { activeHelpTopicKey = it }
          )

          // 6. Live Preview & Edit Cepat
          if (previewRows.isNotEmpty()) {
            LivePreviewSection(
                previewRows = previewRows,
                tr = ::tr,
                onSaveEdit = { idx, value -> viewModel.savePreviewEdit(idx, value) },
                onOpenHelp = { activeHelpTopicKey = it }
            )
          }

          // 7. Log Aktivitas
          if (logs.isNotEmpty()) {
            SystemLogSection(
                logs = logs,
                tr = ::tr,
                onClearLogs = { viewModel.clearLogs() },
                onOpenHelp = { activeHelpTopicKey = it }
            )
          }

          // 8. Panduan Pemasangan Android
          MedievalAccordionCard(
              title = tr("secGuide"),
              leadingEmoji = "📱",
              helpTopicKey = "secGuide",
              expanded = guideExpanded,
              onToggle = { guideExpanded = !guideExpanded },
              onOpenHelp = { activeHelpTopicKey = it },
              headerTestTag = "guide_accordion_header"
          ) {
            val steps = listOf(
                tr("gStep1"),
                tr("gStep2"),
                tr("gStep3"),
                tr("gStep4"),
                tr("gStep5")
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              steps.forEachIndexed { idx, stepText ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text(
                      text = "${idx + 1}.",
                      color = GoldAccent,
                      fontWeight = FontWeight.Bold,
                      style = MaterialTheme.typography.bodySmall
                  )
                  Text(
                      text = stepText,
                      color = ParchmentDim,
                      style = MaterialTheme.typography.bodySmall
                  )
                }
              }
            }
          }

          // Footer
          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
                text = tr("footerCredit"),
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentDim.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "100% Native Android Processing · Zero API Key Required",
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentDim.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
            }
          }
        }
      }
    }
  }

  HelpModalDialog(
      topic = activeHelpTopicKey?.let { I18nStrings.getHelp(lang, it) },
      closeButtonLabel = tr("btnCloseHelp"),
      onDismiss = { activeHelpTopicKey = null }
  )

  ExportPreviewAndSaveDialog(
      visible = showSaveDialog,
      previewItems = pendingExportPreviews,
      exportDelimiter = settings.exportDelimiter,
      exportEncoding = settings.exportEncoding,
      prefix = settings.exportFilenamePrefix,
      suffix = settings.exportFilenameSuffix,
      targetLang = settings.targetLang,
      tr = ::tr,
      onPrefixChange = { viewModel.setExportFilenamePrefix(it) },
      onSuffixChange = { viewModel.setExportFilenameSuffix(it) },
      onSaveToDownloads = { viewModel.savePendingToDownloads() },
      onPickFolder = onPickSaveFolder,
      onDismiss = { viewModel.dismissSaveDialog() }
  )

  FullEditorAndQaDialog(
      summary = qaAuditSummary,
      tr = ::tr,
      onSaveLine = { fileId, lineIdx, newLine -> viewModel.saveFullEditorLine(fileId, lineIdx, newLine) },
      onFindAndReplace = { fileId, find, rep, cs -> viewModel.applyFindAndReplace(fileId, find, rep, cs) },
      onAutoFixMissingTokens = { fileId -> viewModel.autoFixQaMissingTokens(fileId) },
      onDismiss = { viewModel.dismissFullEditor() }
  )

  ModDiffMergeDialog(
      visible = showModDiffDialog,
      newModName = diffNewModName,
      oldTransName = diffOldTransName,
      diffPreview = diffMergePreview,
      tr = ::tr,
      onPickNewModFile = onPickDiffNewMod,
      onPickOldTranslatedFile = onPickDiffOldTrans,
      onApplyDiff = { viewModel.applyModDiffToQueue() },
      onDismiss = { viewModel.dismissModDiffDialog() }
  )

  CsvValidationWarningDialog(
      visible = showValidationWarningDialog,
      reports = validationWarningReports,
      tr = ::tr,
      onProceedAnyway = { viewModel.startTranslation(bypassValidation = true) },
      onDismiss = { viewModel.dismissValidationWarningDialog() }
  )

  AddCustomLanguageDialog(
      visible = showAddCustomLangDialog,
      tr = ::tr,
      onAdd = { code, label -> viewModel.addCustomTargetLanguage(code, label) },
      onDismiss = { showAddCustomLangDialog = false }
  )
}

@Composable
private fun TopMedievalHeader(
    navTitle: String,
    navSub: String,
    activeLang: UiLanguage,
    onSelectLang: (UiLanguage) -> Unit,
    onOpenHelp: (String) -> Unit
) {
  val pal = LocalWarbandPalette.current
  Surface(
      color = pal.headerBg.copy(alpha = 0.96f),
      shadowElevation = 6.dp,
      modifier = Modifier.fillMaxWidth()
  ) {
    Column {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
          Box(
              modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Burgundy.copy(alpha = 0.4f))
                  .border(1.dp, GoldMuted, RoundedCornerShape(8.dp)),
              contentAlignment = Alignment.Center
          ) {
            Text(text = "⚔", color = GoldAccent, fontSize = 18.sp)
          }
          Column {
            Text(
                text = navTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = Parchment,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = navSub,
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentDim.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Row(
              modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF1A0F0E))
                  .border(1.dp, BurgundyBorder, RoundedCornerShape(8.dp))
                  .padding(2.dp),
              horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            UiLanguage.entries.forEach { langItem ->
              val selected = langItem == activeLang
              Box(
                  modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (selected) Burgundy else Color.Transparent)
                      .clickable { onSelectLang(langItem) }
                      .padding(horizontal = 9.dp, vertical = 6.dp)
                      .testTag("lang_btn_${langItem.code}"),
                  contentAlignment = Alignment.Center
              ) {
                Text(
                    text = langItem.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (selected) Parchment else ParchmentDim
                )
              }
            }
          }
          HelpIconButton(
              topicKey = "uiLang",
              contentDescription = "UI Language Help",
              onOpenHelp = onOpenHelp
          )
        }
      }
      HorizontalDivider(color = BurgundyBorder, thickness = 1.dp)
    }
  }
}

@Composable
private fun HeroBannerSection(
    badge: String,
    title: String,
    description: String
) {
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Surface(
        shape = CircleShape,
        color = Burgundy.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, Color(0xFF5A342D))
    ) {
      Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text("🛡️", fontSize = 12.sp)
        Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = ParchmentDim,
            textAlign = TextAlign.Center
        )
      }
    }

    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        color = Parchment,
        textAlign = TextAlign.Center
    )

    Text(
        text = description,
        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
        color = ParchmentDim,
        textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun EngineSection(
    settings: TranslatorSettings,
    cacheCount: Int,
    connectionStatus: com.example.domain.ConnectionStatus,
    tr: (String) -> String,
    onSelectEngine: (TranslationEngine) -> Unit,
    onSelectDelay: (Long) -> Unit,
    onSelectBatchSize: (Int) -> Unit,
    onToggleFallback: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onOpenHelp: (String) -> Unit
) {
  MedievalCard {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
      ) {
        Text("⚙️", fontSize = 15.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = tr("secEngine").uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = GoldAccent,
            modifier = Modifier.weight(1f, fill = false)
        )
        HelpIconButton(
            topicKey = "secEngine",
            contentDescription = tr("secEngine"),
            onOpenHelp = onOpenHelp
        )
      }
      ConnectionBadge(
          status = if (connectionStatus.text == "Siap digunakan") {
            connectionStatus.copy(text = tr("statusReady"))
          } else {
            connectionStatus
          }
      )
    }

    HorizontalDivider(color = BurgundyBorderSoft, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

    // Engine cards
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      val engineSpecs = listOf(
          Triple(TranslationEngine.GOOGLE, "🌐", tr("engGoogleSub") to "engGoogle"),
          Triple(TranslationEngine.LINGVA, "🦅", tr("engLingvaSub") to "engLingva"),
          Triple(TranslationEngine.MYMEMORY, "🧠", tr("engMmSub") to "engMyMemory")
      )

      engineSpecs.forEach { (engine, emoji, meta) ->
        val (subtitle, helpKey) = meta
        val selected = settings.engine == engine
        val borderColor = if (selected) GoldAccent else BurgundyBorder
        val borderWidth = if (selected) 2.dp else 1.dp
        val bgColor = if (selected) Color(0xFF2B1917) else Color(0xFF190F0E)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                .clickable { onSelectEngine(engine) }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("engine_card_${engine.id}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
              modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MahoganyHeader)
                  .border(1.dp, BurgundyBorder, RoundedCornerShape(8.dp)),
              contentAlignment = Alignment.Center
          ) {
            Text(emoji, fontSize = 20.sp)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = engine.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = Parchment
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (engine == TranslationEngine.GOOGLE) Color(0xFF4ADE80) else ParchmentDim
            )
          }
          HelpIconButton(
              topicKey = helpKey,
              contentDescription = engine.displayName,
              onOpenHelp = onOpenHelp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Delay selector
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF160D0C))
            .border(1.dp, BurgundyBorderSoft, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = tr("lblSpeed"),
            style = MaterialTheme.typography.labelMedium,
            color = ParchmentDim,
            modifier = Modifier.weight(1f, fill = false)
        )
        HelpIconButton(
            topicKey = "lblSpeed",
            contentDescription = tr("lblSpeed"),
            onOpenHelp = onOpenHelp
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      val activeDelay = DelayOptions.all.firstOrNull { it.ms == settings.delayMs } ?: DelayOptions.all[1]
      MedievalDropdown(
          selectedLabel = tr(activeDelay.titleKey),
          items = DelayOptions.all,
          itemLabel = { tr(it.titleKey) },
          onSelect = { onSelectDelay(it.ms) },
          testTag = "delay_dropdown"
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
          text = tr("lblBatchSize"),
          style = MaterialTheme.typography.labelMedium,
          color = ParchmentDim
      )
      Spacer(modifier = Modifier.height(4.dp))
      val activeBatch = BatchSizeOptions.all.firstOrNull { it.size == settings.batchSize } ?: BatchSizeOptions.all.first()
      MedievalDropdown(
          selectedLabel = tr(activeBatch.titleKey),
          items = BatchSizeOptions.all,
          itemLabel = { tr(it.titleKey) },
          onSelect = { onSelectBatchSize(it.size) },
          testTag = "batch_size_dropdown"
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Cache & Auto-Fallback
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF160D0C))
            .border(1.dp, BurgundyBorderSoft, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Text(
              text = tr("lblCache"),
              style = MaterialTheme.typography.labelMedium,
              color = ParchmentDim,
              modifier = Modifier.weight(1f, fill = false)
          )
          HelpIconButton(
              topicKey = "lblCache",
              contentDescription = tr("lblCache"),
              onOpenHelp = onOpenHelp
          )
        }
        Text(
            text = "$cacheCount ${tr("cacheUnit")}",
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = GoldAccent
        )
      }

      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable { onToggleFallback(!settings.autoFallback) }
        ) {
          Checkbox(
              checked = settings.autoFallback,
              onCheckedChange = onToggleFallback,
              colors = CheckboxDefaults.colors(
                  checkedColor = Burgundy,
                  uncheckedColor = GoldMuted,
                  checkmarkColor = Parchment
              ),
              modifier = Modifier.testTag("auto_fallback_checkbox")
          )
          Text(
              text = tr("chkFallback"),
              style = MaterialTheme.typography.bodySmall,
              color = Parchment,
              modifier = Modifier.weight(1f, fill = false)
          )
          HelpIconButton(
              topicKey = "chkFallback",
              contentDescription = tr("chkFallback"),
              onOpenHelp = onOpenHelp
          )
        }

        OutlinedButton(
            onClick = onClearCache,
            border = BorderStroke(1.dp, Crimson),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Crimson.copy(alpha = 0.25f),
                contentColor = CrimsonText
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("clear_cache_button")
        ) {
          Text(tr("btnClearCache"), fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
private fun FormatSection(
    settings: TranslatorSettings,
    targetLanguages: List<LanguageOption>,
    isRegexValid: Boolean,
    tr: (String) -> String,
    onSelectSourceLang: (String) -> Unit,
    onSelectTargetLang: (String) -> Unit,
    onOpenAddCustomLang: () -> Unit,
    onSeparatorChange: (String) -> Unit,
    onStripTagChange: (String) -> Unit,
    onSkipRegexChange: (String) -> Unit,
    onToggleMedievalTone: (Boolean) -> Unit,
    onOpenHelp: (String) -> Unit
) {
  val pal = LocalWarbandPalette.current
  val effectiveTargetLanguages = targetLanguages.ifEmpty { SupportedLanguages.targetLanguages }
  MedievalCard {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("📜", fontSize = 15.sp)
      Spacer(modifier = Modifier.width(6.dp))
      Text(
          text = tr("secFormat").uppercase(),
          style = MaterialTheme.typography.titleMedium,
          color = pal.primaryAccent,
          modifier = Modifier.weight(1f, fill = false)
      )
      HelpIconButton(
          topicKey = "secFormat",
          contentDescription = tr("secFormat"),
          onOpenHelp = onOpenHelp
      )
    }

    HorizontalDivider(color = pal.borderSoft, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

    // Source language
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(tr("lblSrcLang"), style = MaterialTheme.typography.labelMedium, color = pal.textSecondary)
      HelpIconButton(topicKey = "lblSrcLang", contentDescription = tr("lblSrcLang"), onOpenHelp = onOpenHelp)
    }
    val activeSrc = SupportedLanguages.sourceLanguages.firstOrNull { it.code == settings.sourceLang }
        ?: SupportedLanguages.sourceLanguages.first()
    MedievalDropdown(
        selectedLabel = if (activeSrc.code == "auto") tr("optAuto") else activeSrc.label,
        items = SupportedLanguages.sourceLanguages,
        itemLabel = { if (it.code == "auto") tr("optAuto") else it.label },
        onSelect = { onSelectSourceLang(it.code) },
        testTag = "source_lang_dropdown"
    )

    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "⬇",
        color = pal.primaryAccent,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(2.dp))

    // Target language (Room-backed)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Text(tr("lblTgtLang"), style = MaterialTheme.typography.labelMedium, color = pal.textSecondary)
        HelpIconButton(topicKey = "lblTgtLang", contentDescription = tr("lblTgtLang"), onOpenHelp = onOpenHelp)
      }
      TextButton(
          onClick = onOpenAddCustomLang,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.testTag("open_add_custom_lang_button")
      ) {
        Text(tr("btnAddCustomLang"), fontSize = 11.sp, color = pal.primaryAccent)
      }
    }
    val activeTgt = effectiveTargetLanguages.firstOrNull { it.code == settings.targetLang }
        ?: LanguageOption(settings.targetLang, settings.targetLang.uppercase())
    MedievalDropdown(
        selectedLabel = activeTgt.label,
        items = effectiveTargetLanguages,
        itemLabel = { it.label },
        onSelect = { onSelectTargetLang(it.code) },
        testTag = "target_lang_dropdown"
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Separator & Strip Tag row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(tr("lblSep"), style = MaterialTheme.typography.labelSmall, color = ParchmentDim, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
          HelpIconButton(topicKey = "lblSep", contentDescription = tr("lblSep"), onOpenHelp = onOpenHelp)
        }
        OutlinedTextField(
            value = settings.separator,
            onValueChange = onSeparatorChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = GoldAccent,
                textAlign = TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = BurgundyBorder,
                focusedContainerColor = MahoganyInput,
                unfocusedContainerColor = MahoganyInput,
                cursorColor = GoldAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("separator_input")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(tr("hintSep"), style = MaterialTheme.typography.labelSmall, color = ParchmentDim.copy(alpha = 0.7f))
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(tr("lblStrip"), style = MaterialTheme.typography.labelSmall, color = ParchmentDim, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
          HelpIconButton(topicKey = "lblStrip", contentDescription = tr("lblStrip"), onOpenHelp = onOpenHelp)
        }
        OutlinedTextField(
            value = settings.stripTag,
            onValueChange = onStripTagChange,
            singleLine = true,
            placeholder = { Text("{***}", color = ParchmentDim.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = Parchment,
                textAlign = TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = BurgundyBorder,
                focusedContainerColor = MahoganyInput,
                unfocusedContainerColor = MahoganyInput,
                cursorColor = GoldAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("strip_tag_input")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(tr("hintStrip"), style = MaterialTheme.typography.labelSmall, color = ParchmentDim.copy(alpha = 0.7f))
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Regex skip
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(tr("lblRegex"), style = MaterialTheme.typography.labelSmall, color = ParchmentDim)
        HelpIconButton(topicKey = "lblRegex", contentDescription = tr("lblRegex"), onOpenHelp = onOpenHelp)
      }
      OutlinedTextField(
          value = settings.skipRegex,
          onValueChange = onSkipRegexChange,
          singleLine = true,
          placeholder = { Text("^\\d+$", color = ParchmentDim.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace) },
          textStyle = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.Monospace,
              color = Parchment
          ),
          colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldAccent,
              unfocusedBorderColor = BurgundyBorder,
              focusedContainerColor = MahoganyInput,
              unfocusedContainerColor = MahoganyInput,
              cursorColor = GoldAccent
          ),
          modifier = Modifier
              .fillMaxWidth()
              .testTag("skip_regex_input")
      )
      Spacer(modifier = Modifier.height(4.dp))
      val regexMsg = when {
        settings.skipRegex.trim().isEmpty() -> tr("hintRegex")
        isRegexValid -> tr("regexValid")
        else -> tr("regexInvalid")
      }
      val regexColor = when {
        settings.skipRegex.trim().isEmpty() -> ParchmentDim.copy(alpha = 0.7f)
        isRegexValid -> Color(0xFF4ADE80)
        else -> CrimsonText
      }
      Text(regexMsg, style = MaterialTheme.typography.labelSmall, color = regexColor)
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Medieval Tone Post-Processing Toggle
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF160D0C))
            .border(1.dp, BurgundyBorderSoft, RoundedCornerShape(10.dp))
            .clickable { onToggleMedievalTone(!settings.medievalToneEnabled) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Checkbox(
          checked = settings.medievalToneEnabled,
          onCheckedChange = onToggleMedievalTone,
          colors = CheckboxDefaults.colors(
              checkedColor = Burgundy,
              uncheckedColor = GoldMuted,
              checkmarkColor = Parchment
          ),
          modifier = Modifier.testTag("medieval_tone_checkbox")
      )
      Text(
          text = tr("chkMedievalTone"),
          style = MaterialTheme.typography.bodySmall,
          color = Parchment
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Variable Guard Info Pill
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF160D0C))
            .border(1.dp, BurgundyBorderSoft, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
      Text("🛡️", fontSize = 15.sp, modifier = Modifier.padding(top = 2.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = tr("varProtDesc"),
          style = MaterialTheme.typography.bodySmall,
          color = ParchmentDim,
          modifier = Modifier.weight(1f)
      )
      HelpIconButton(topicKey = "varProt", contentDescription = "Variable Guard", onOpenHelp = onOpenHelp)
    }
  }
}

@Composable
private fun QueueSection(
    fileQueue: List<QueuedFileItem>,
    validationReports: Map<String, CsvValidationReport>,
    selectedPreviewFileId: String?,
    isRunning: Boolean,
    exportPrefix: String,
    exportSuffix: String,
    tr: (String) -> String,
    onPickFiles: () -> Unit,
    onPickSingleFile: () -> Unit,
    onOpenModDiff: () -> Unit,
    onSelectPreviewFile: (String) -> Unit,
    onOpenQaEditor: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onRetryItem: (String) -> Unit,
    onClearQueue: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onSaveItem: (QueuedFileItem) -> Unit,
    onOpenHelp: (String) -> Unit
) {
  MedievalCard {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Text("📂", fontSize = 15.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = tr("secQueue").uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = GoldAccent,
            modifier = Modifier.weight(1f, fill = false)
        )
        HelpIconButton(topicKey = "secQueue", contentDescription = tr("secQueue"), onOpenHelp = onOpenHelp)
      }
      Text(
          text = tr("queueHint"),
          style = MaterialTheme.typography.labelSmall,
          color = ParchmentDim
      )
    }

    HorizontalDivider(color = BurgundyBorderSoft, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

    // Dashed Dropzone CTA (OpenMultipleDocuments SAF)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF160D0C))
            .drawBehind {
              drawRoundRect(
                  color = Burgundy,
                  style = Stroke(
                      width = 2.dp.toPx(),
                      pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                  ),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
              )
            }
            .clickable(enabled = !isRunning) { onPickFiles() }
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("pick_csv_files_button"),
        contentAlignment = Alignment.Center
    ) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
            imageVector = Icons.Default.UploadFile,
            contentDescription = tr("dropTitle"),
            tint = GoldAccent,
            modifier = Modifier.size(34.dp)
        )
        Text(
            text = tr("dropTitle"),
            style = MaterialTheme.typography.titleSmall,
            color = Parchment,
            textAlign = TextAlign.Center
        )
        Text(
            text = tr("dropSub"),
            style = MaterialTheme.typography.labelSmall,
            color = ParchmentDim,
            textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedButton(
          onClick = onPickSingleFile,
          enabled = !isRunning,
          border = BorderStroke(1.dp, BurgundyBorder),
          colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color(0xFF170E0D),
              contentColor = Parchment
          ),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
          modifier = Modifier
              .weight(1f)
              .minimumInteractiveComponentSize()
              .testTag("pick_single_csv_button")
      ) {
        Icon(Icons.Default.NoteAdd, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(tr("btnPickSingle"), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }

      OutlinedButton(
          onClick = onOpenModDiff,
          enabled = !isRunning,
          border = BorderStroke(1.dp, GoldMuted),
          colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color(0xFF1F1211),
              contentColor = GoldAccent
          ),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
          modifier = Modifier
              .weight(1f)
              .minimumInteractiveComponentSize()
              .testTag("open_mod_diff_button")
      ) {
        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(tr("btnModDiff"), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
    }

    if (fileQueue.isNotEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      val totalLines = fileQueue.sumOf { it.lines.size }
      val doneFiles = fileQueue.count { it.status == FileQueueStatus.DONE }
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
            text = "${fileQueue.size} file ($doneFiles/${fileQueue.size} selesai · $totalLines baris)",
            style = MaterialTheme.typography.labelSmall,
            color = ParchmentDim
        )
        TextButton(
            onClick = onClearQueue,
            enabled = !isRunning,
            modifier = Modifier.testTag("clear_queue_button")
        ) {
          Text(tr("btnClearQueue"), color = CrimsonText, fontSize = 12.sp)
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        fileQueue.forEachIndexed { index, item ->
          val isSelectedForPreview = item.id == selectedPreviewFileId
          val borderClr = if (isSelectedForPreview) GoldAccent else Color(0xFF3E231F)

          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelectedForPreview) Color(0xFF211311) else Color(0xFF170E0D))
                  .border(1.dp, borderClr, RoundedCornerShape(10.dp))
                  .clickable { onSelectPreviewFile(item.id) }
                  .padding(horizontal = 12.dp, vertical = 10.dp)
                  .testTag("queue_item_${item.name}"),
              verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${index + 1}. ${item.name}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Parchment,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val resumeInfo = if (item.status == FileQueueStatus.WAIT && item.cachedLinesCount > 0) {
                  " · ${tr("autoResumeReady")}: ${item.cachedLinesCount}/${item.lines.size} (${item.resumePercentage}%)"
                } else if (item.status == FileQueueStatus.RUN) {
                  " · ${item.processedCount}/${item.lines.size} (${item.resumePercentage}%)"
                } else {
                  ""
                }
                Text(
                    text = "${item.lines.size} baris$resumeInfo",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.cachedLinesCount > 0 && item.status == FileQueueStatus.WAIT) GoldAccent else ParchmentDim
                )
                val renamedOutput = WarbandTextProcessor.buildExportFileName(
                    originalName = item.name,
                    prefix = exportPrefix,
                    suffix = exportSuffix
                )
                if (renamedOutput != item.name) {
                  Text(
                      text = "→ $renamedOutput",
                      style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                      color = GoldAccent,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                  )
                }
                val valReport = validationReports[item.id]
                if (valReport != null) {
                  val (valColor, valText) = when (valReport.status) {
                    CsvValidationStatus.VALID -> ForestText to "✓ ${tr("csvValOk")}"
                    CsvValidationStatus.WARNING -> AmberText to "⚠ ${tr("csvValWarn")}: ${valReport.malformedLinesCount}"
                    CsvValidationStatus.MALFORMED -> CrimsonText to "✕ ${tr("csvValMalformed")}"
                  }
                  Text(
                      text = valText,
                      style = MaterialTheme.typography.labelSmall,
                      color = valColor,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.testTag("queue_validation_badge_${item.name}")
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              val (badgeBg, badgeBorder, badgeTextColor, badgeLabel) = when (item.status) {
                FileQueueStatus.WAIT -> Quad(Color(0xFF261715), BurgundyBorder, ParchmentDim, tr("stWait"))
                FileQueueStatus.RUN -> Quad(AmberBg, GoldMuted, AmberText, tr("stRun"))
                FileQueueStatus.DONE -> Quad(Color(0xFF0E2914), Forest, ForestText, tr("stDone"))
                FileQueueStatus.ERROR -> Quad(Color(0xFF331015), Crimson, CrimsonText, tr("stErr"))
              }

              Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = badgeBg,
                  border = BorderStroke(1.dp, badgeBorder)
              ) {
                Text(
                    text = badgeLabel,
                    color = badgeTextColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            if (item.resumePercentage > 0 && item.status != FileQueueStatus.DONE) {
              LinearProgressIndicator(
                  progress = { item.resumePercentage / 100f },
                  color = if (item.status == FileQueueStatus.RUN) GoldAccent else GoldMuted,
                  trackColor = Color(0xFF110808),
                  modifier = Modifier
                      .fillMaxWidth()
                      .height(4.dp)
                      .clip(CircleShape)
              )
            }

            // Queue item quick action bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
              if (!isRunning && fileQueue.size > 1) {
                IconButton(
                    onClick = { onMoveUp(item.id) },
                    enabled = index > 0,
                    modifier = Modifier.testTag("move_up_${item.name}")
                ) {
                  Icon(
                      imageVector = Icons.Default.ArrowUpward,
                      contentDescription = "Move Up",
                      tint = if (index > 0) ParchmentDim else ParchmentDim.copy(alpha = 0.25f),
                      modifier = Modifier.size(18.dp)
                  )
                }
                IconButton(
                    onClick = { onMoveDown(item.id) },
                    enabled = index < fileQueue.lastIndex,
                    modifier = Modifier.testTag("move_down_${item.name}")
                ) {
                  Icon(
                      imageVector = Icons.Default.ArrowDownward,
                      contentDescription = "Move Down",
                      tint = if (index < fileQueue.lastIndex) ParchmentDim else ParchmentDim.copy(alpha = 0.25f),
                      modifier = Modifier.size(18.dp)
                  )
                }
              }

              if (!isRunning && item.status == FileQueueStatus.ERROR) {
                OutlinedButton(
                    onClick = { onRetryItem(item.id) },
                    border = BorderStroke(1.dp, GoldMuted),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = GoldAccent, modifier = Modifier.size(14.dp))
                }
              }

              if (item.status == FileQueueStatus.DONE) {
                OutlinedButton(
                    onClick = { onOpenQaEditor(item.id) },
                    border = BorderStroke(1.dp, GoldMuted),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AmberBg,
                        contentColor = GoldAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("qa_editor_file_${item.name}")
                ) {
                  Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(tr("btnQaEditor"), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = { onSaveItem(item) },
                    border = BorderStroke(1.dp, Forest),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Forest.copy(alpha = 0.45f),
                        contentColor = ForestText
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("save_file_${item.name}")
                ) {
                  Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(tr("btnDlFile"), fontSize = 11.sp)
                }
              }

              if (!isRunning) {
                IconButton(
                    onClick = { onRemoveItem(item.id) },
                    modifier = Modifier.testTag("remove_file_${item.name}")
                ) {
                  Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Remove",
                      tint = ParchmentDim,
                      modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun ControlsAndProgressSection(
    progress: TranslationProgressState,
    hasWaitingFiles: Boolean,
    hasDoneFiles: Boolean,
    failedCount: Int,
    tr: (String) -> String,
    onStart: () -> Unit,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    onOpenQaEditor: () -> Unit,
    onSaveAll: () -> Unit,
    onExportErrors: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    if (progress.isRunning || progress.totalLines > 0) {
      MedievalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
              text = if (progress.currentFileName.isNotEmpty()) {
                "[${progress.currentFileIndex}/${progress.totalPendingFiles}] ${progress.currentFileName}"
              } else {
                "..."
              },
              style = MaterialTheme.typography.titleSmall,
              color = GoldAccent,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
              text = "${progress.percentage}% (${progress.processedLines} / ${progress.totalLines})",
              style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
              color = Parchment
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress.percentage / 100f },
            color = GoldAccent,
            trackColor = Color(0xFF110808),
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .border(1.dp, BurgundyBorder, CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
              text = "Cache hit: ${progress.cacheHits} ${tr("cacheUnit")}",
              style = MaterialTheme.typography.labelSmall,
              color = ParchmentDim
          )
          if (progress.etaText.isNotEmpty()) {
            Text(
                text = "ETA: ${progress.etaText}",
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentDim
            )
          }
        }
      }
    }

    // Start / Pause / Stop buttons
    Button(
        onClick = onStart,
        enabled = !progress.isRunning && hasWaitingFiles,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF754339),
            contentColor = Parchment,
            disabledContainerColor = Color(0xFF3A201C),
            disabledContentColor = ParchmentDim.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, if (!progress.isRunning && hasWaitingFiles) GoldMuted else BurgundyBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("start_translation_button")
    ) {
      Icon(Icons.Default.PlayArrow, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = tr("btnStart"),
          style = MaterialTheme.typography.titleMedium,
          color = if (!progress.isRunning && hasWaitingFiles) Parchment else ParchmentDim.copy(alpha = 0.5f)
      )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
          onClick = onTogglePause,
          enabled = progress.isRunning,
          border = BorderStroke(1.dp, if (progress.isRunning) GoldMuted else BurgundyBorder),
          colors = ButtonDefaults.outlinedButtonColors(
              containerColor = AmberBg,
              contentColor = GoldAccent
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("pause_translation_button")
      ) {
        Icon(
            imageVector = if (progress.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (progress.isPaused) tr("btnResume") else tr("btnPause"))
      }

      OutlinedButton(
          onClick = onStop,
          enabled = progress.isRunning,
          border = BorderStroke(1.dp, if (progress.isRunning) Crimson else BurgundyBorder),
          colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color(0xFF2A1114),
              contentColor = CrimsonText
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("stop_translation_button")
      ) {
        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(tr("btnStop"))
      }
    }

    if (hasDoneFiles) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
              onClick = onOpenQaEditor,
              border = BorderStroke(1.dp, GoldMuted),
              colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = AmberBg,
                  contentColor = GoldAccent
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("open_qa_editor_button")
          ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(tr("btnQaEditor"), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
          }

          OutlinedButton(
              onClick = onSaveAll,
              border = BorderStroke(1.dp, GoldMuted),
              colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Burgundy.copy(alpha = 0.35f),
                  contentColor = GoldAccent
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("preview_export_button")
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(tr("btnPreviewExport"), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
          }
        }

        Button(
            onClick = onSaveAll,
            colors = ButtonDefaults.buttonColors(
                containerColor = Forest.copy(alpha = 0.65f),
                contentColor = ForestText
            ),
            border = BorderStroke(1.dp, Forest),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_all_button")
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(tr("btnDlAll"), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
        }
      }
    }

    if (failedCount > 0) {
      OutlinedButton(
          onClick = onExportErrors,
          border = BorderStroke(1.dp, Crimson),
          colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Crimson.copy(alpha = 0.25f),
              contentColor = CrimsonText
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("export_errors_button")
      ) {
        Text("⚠️ ${tr("lblErrExport")} ($failedCount)")
      }
    }
  }
}

@Composable
private fun LivePreviewSection(
    previewRows: List<PreviewRow>,
    tr: (String) -> String,
    onSaveEdit: (Int, String) -> Unit,
    onOpenHelp: (String) -> Unit
) {
  MedievalCard {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("👁️", fontSize = 15.sp)
      Spacer(modifier = Modifier.width(6.dp))
      Text(
          text = tr("secPreview").uppercase(),
          style = MaterialTheme.typography.titleMedium,
          color = GoldAccent,
          modifier = Modifier.weight(1f, fill = false)
      )
      HelpIconButton(topicKey = "secPreview", contentDescription = tr("secPreview"), onOpenHelp = onOpenHelp)
    }
    Text(
        text = tr("previewHint"),
        style = MaterialTheme.typography.labelSmall,
        color = ParchmentDim
    )
    HorizontalDivider(color = BurgundyBorderSoft, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      previewRows.forEach { row ->
        var isEditing by remember(row.lineIndex, row.translatedLine) { mutableStateOf(false) }
        var editDraft by remember(row.lineIndex, row.translatedLine) { mutableStateOf(row.translatedLine) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MahoganyHeader)
                .border(1.dp, Color(0xFF361D1A), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
                text = "[ASLI #${row.lineIndex + 1}] ${row.originalLine}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = ParchmentDim.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(
                onClick = { isEditing = !isEditing },
                border = BorderStroke(1.dp, Color(0xFF5A342D)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("edit_preview_${row.lineIndex}")
            ) {
              Icon(Icons.Default.Edit, contentDescription = tr("btnEdit"), tint = GoldAccent, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(tr("btnEdit"), color = GoldAccent, fontSize = 11.sp)
            }
          }

          if (!isEditing) {
            Text(
                text = "[HASIL] ${row.translatedLine}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = ForestText
            )
          } else {
            OutlinedTextField(
                value = editDraft,
                onValueChange = { editDraft = it },
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Parchment
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = BurgundyBorder,
                    focusedContainerColor = MahoganyInput,
                    unfocusedContainerColor = MahoganyInput
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_preview_input_${row.lineIndex}")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
              TextButton(onClick = {
                editDraft = row.translatedLine
                isEditing = false
              }) {
                Text(tr("btnCancel"), color = ParchmentDim, fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.width(6.dp))
              Button(
                  onClick = {
                    onSaveEdit(row.lineIndex, editDraft)
                    isEditing = false
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Forest, contentColor = ForestText),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("save_preview_edit_${row.lineIndex}")
              ) {
                Text(tr("btnSave"), fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SystemLogSection(
    logs: List<com.example.domain.LogEntry>,
    tr: (String) -> String,
    onClearLogs: () -> Unit,
    onOpenHelp: (String) -> Unit
) {
  MedievalCard {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Text(
            text = tr("secLog").uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = ParchmentDim
        )
        HelpIconButton(topicKey = "secLog", contentDescription = tr("secLog"), onOpenHelp = onOpenHelp)
      }
      TextButton(onClick = onClearLogs, modifier = Modifier.testTag("clear_logs_button")) {
        Icon(Icons.Default.Delete, contentDescription = null, tint = ParchmentDim, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(tr("btnClearLog"), color = ParchmentDim, fontSize = 11.sp)
      }
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(logs.size) {
      scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianDeep)
            .border(1.dp, Color(0xFF321B18), RoundedCornerShape(8.dp))
            .verticalScroll(scrollState)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      logs.forEach { entry ->
        val color = when (entry.level) {
          LogLevel.OK -> Color(0xFF4ADE80)
          LogLevel.WARN -> AmberText
          LogLevel.ERROR -> CrimsonText
          LogLevel.INFO -> ParchmentDim
        }
        Text(
            text = "[${entry.timestamp}] ${entry.message}",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = color
        )
      }
    }
  }
}

@Composable
private fun StickyProgressControlBar(
    progress: TranslationProgressState,
    pauseLabel: String,
    onTogglePause: () -> Unit,
    onStop: () -> Unit
) {
  Surface(
      color = Color(0xFF160D0C).copy(alpha = 0.96f),
      border = BorderStroke(1.dp, GoldAccent),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth()
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
              text = progress.currentFileName.ifEmpty { "..." },
              style = MaterialTheme.typography.labelMedium,
              color = GoldAccent,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
              text = "${progress.percentage}% (${progress.processedLines}/${progress.totalLines})",
              style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
              color = Parchment
          )
        }
        Spacer(modifier = Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { progress.percentage / 100f },
            color = GoldAccent,
            trackColor = Color(0xFF110808),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
      }

      OutlinedButton(
          onClick = onTogglePause,
          border = BorderStroke(1.dp, GoldMuted),
          colors = ButtonDefaults.outlinedButtonColors(containerColor = AmberBg, contentColor = GoldAccent),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("sticky_pause_button")
      ) {
        Text(pauseLabel, fontSize = 11.sp)
      }

      OutlinedButton(
          onClick = onStop,
          border = BorderStroke(1.dp, Crimson),
          colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF2A1114), contentColor = CrimsonText),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("sticky_stop_button")
      ) {
        Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
      }
    }
  }
}
