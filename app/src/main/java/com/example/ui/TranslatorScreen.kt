package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.domain.DelayOptions
import com.example.domain.FileQueueStatus
import com.example.domain.I18nStrings
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
import com.example.ui.theme.MahoganyHeader
import com.example.ui.theme.MahoganyInput
import com.example.ui.theme.Obsidian
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.Parchment
import com.example.ui.theme.ParchmentDim

@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel,
    snackbarHostState: SnackbarHostState,
    onPickCsvFiles: () -> Unit,
    onImportGlossary: () -> Unit,
    onExportGlossary: () -> Unit,
    onExportErrorLog: () -> Unit,
    onPickSaveFolder: () -> Unit,
    onStartWithNotificationCheck: () -> Unit
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val cacheCount by viewModel.cacheCount.collectAsStateWithLifecycle()
  val fileQueue by viewModel.fileQueue.collectAsStateWithLifecycle()
  val progressState by viewModel.progressState.collectAsStateWithLifecycle()
  val previewRows by viewModel.previewRows.collectAsStateWithLifecycle()
  val logs by viewModel.logs.collectAsStateWithLifecycle()
  val failedRows by viewModel.failedRows.collectAsStateWithLifecycle()
  val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
  val errorAlert by viewModel.errorAlert.collectAsStateWithLifecycle()
  val pendingSaveFiles by viewModel.pendingSaveFiles.collectAsStateWithLifecycle()
  val showSaveDialog by viewModel.showSaveDialog.collectAsStateWithLifecycle()

  val lang = settings.uiLanguage
  fun tr(key: String) = I18nStrings.get(lang, key)

  var activeHelpTopicKey by remember { mutableStateOf<String?>(null) }
  var glossaryExpanded by remember { mutableStateOf(false) }
  var guideExpanded by remember { mutableStateOf(false) }

  val glossaryRules = remember(settings.glossaryText) {
    WarbandTextProcessor.parseGlossary(settings.glossaryText)
  }
  val hasWaitingFiles = remember(fileQueue) { fileQueue.any { it.status == FileQueueStatus.WAIT } }
  val hasDoneFiles = remember(fileQueue) { fileQueue.any { it.status == FileQueueStatus.DONE } }
  val isRegexValid = remember(settings.skipRegex) { WarbandTextProcessor.isValidRegex(settings.skipRegex) }

  Scaffold(
      containerColor = Obsidian,
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
        AnimatedVisibility(visible = progressState.isRunning) {
          StickyProgressControlBar(
              progress = progressState,
              pauseLabel = if (progressState.isPaused) tr("btnResume") else tr("btnPause"),
              onTogglePause = { viewModel.togglePause() },
              onStop = { viewModel.stopTranslation() }
          )
        }
      }
  ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Burgundy.copy(alpha = 0.24f), Obsidian),
                    center = Offset(500f, 0f),
                    radius = 1200f
                )
            )
            .padding(innerPadding)
    ) {
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
              onToggleFallback = { viewModel.setAutoFallback(it) },
              onClearCache = { viewModel.clearCache() },
              onOpenHelp = { activeHelpTopicKey = it }
          )

          // 2. Pengaturan Bahasa & Format Warband
          FormatSection(
              settings = settings,
              isRegexValid = isRegexValid,
              tr = ::tr,
              onSelectSourceLang = { viewModel.setSourceLang(it) },
              onSelectTargetLang = { viewModel.setTargetLang(it) },
              onSeparatorChange = { viewModel.setSeparator(it) },
              onStripTagChange = { viewModel.setStripTag(it) },
              onSkipRegexChange = { viewModel.setSkipRegex(it) },
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
              isRunning = progressState.isRunning,
              tr = ::tr,
              onPickFiles = onPickCsvFiles,
              onClearQueue = { viewModel.clearQueue() },
              onRemoveItem = { viewModel.removeQueueItem(it) },
              onSaveItem = { viewModel.openSaveForSingleFile(it) },
              onOpenHelp = { activeHelpTopicKey = it }
          )

          // 5. Kontrol Proses & Progress Bar
          ControlsAndProgressSection(
              progress = progressState,
              hasWaitingFiles = hasWaitingFiles,
              hasDoneFiles = hasDoneFiles,
              failedCount = failedRows.size,
              tr = ::tr,
              onStart = onStartWithNotificationCheck,
              onTogglePause = { viewModel.togglePause() },
              onStop = { viewModel.stopTranslation() },
              onSaveAll = { viewModel.openSaveForAllDone() },
              onExportErrors = onExportErrorLog
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

          Spacer(modifier = Modifier.height(if (progressState.isRunning) 72.dp else 16.dp))
        }
      }
    }
  }

  HelpModalDialog(
      topic = activeHelpTopicKey?.let { I18nStrings.getHelp(lang, it) },
      closeButtonLabel = tr("btnCloseHelp"),
      onDismiss = { activeHelpTopicKey = null }
  )

  SaveDestinationDialog(
      visible = showSaveDialog,
      fileCount = pendingSaveFiles.size,
      title = tr("saveDialogTitle"),
      description = tr("saveDialogDesc"),
      downloadsLabel = tr("saveToDownloads"),
      downloadsSub = tr("saveToDownloadsSub"),
      folderLabel = tr("saveToFolder"),
      folderSub = tr("saveToFolderSub"),
      cancelLabel = tr("btnCancel"),
      onSaveToDownloads = { viewModel.savePendingToDownloads() },
      onPickFolder = onPickSaveFolder,
      onDismiss = { viewModel.dismissSaveDialog() }
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
  Surface(
      color = MahoganyHeader.copy(alpha = 0.96f),
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
    isRegexValid: Boolean,
    tr: (String) -> String,
    onSelectSourceLang: (String) -> Unit,
    onSelectTargetLang: (String) -> Unit,
    onSeparatorChange: (String) -> Unit,
    onStripTagChange: (String) -> Unit,
    onSkipRegexChange: (String) -> Unit,
    onOpenHelp: (String) -> Unit
) {
  MedievalCard {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("📜", fontSize = 15.sp)
      Spacer(modifier = Modifier.width(6.dp))
      Text(
          text = tr("secFormat").uppercase(),
          style = MaterialTheme.typography.titleMedium,
          color = GoldAccent,
          modifier = Modifier.weight(1f, fill = false)
      )
      HelpIconButton(
          topicKey = "secFormat",
          contentDescription = tr("secFormat"),
          onOpenHelp = onOpenHelp
      )
    }

    HorizontalDivider(color = BurgundyBorderSoft, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

    // Source language
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(tr("lblSrcLang"), style = MaterialTheme.typography.labelMedium, color = ParchmentDim)
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
        color = GoldAccent,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(2.dp))

    // Target language
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(tr("lblTgtLang"), style = MaterialTheme.typography.labelMedium, color = ParchmentDim)
      HelpIconButton(topicKey = "lblTgtLang", contentDescription = tr("lblTgtLang"), onOpenHelp = onOpenHelp)
    }
    val activeTgt = SupportedLanguages.targetLanguages.firstOrNull { it.code == settings.targetLang }
        ?: SupportedLanguages.targetLanguages.first()
    MedievalDropdown(
        selectedLabel = activeTgt.label,
        items = SupportedLanguages.targetLanguages,
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

    Spacer(modifier = Modifier.height(12.dp))

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
    isRunning: Boolean,
    tr: (String) -> String,
    onPickFiles: () -> Unit,
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

    // Dashed Dropzone CTA
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
            .padding(horizontal = 16.dp, vertical = 22.dp)
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

    if (fileQueue.isNotEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      val totalLines = fileQueue.sumOf { it.lines.size }
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
            text = "${fileQueue.size} file ($totalLines baris)",
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
        fileQueue.forEach { item ->
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF170E0D))
                  .border(1.dp, Color(0xFF3E231F), RoundedCornerShape(10.dp))
                  .padding(horizontal = 12.dp, vertical = 10.dp)
                  .testTag("queue_item_${item.name}"),
              verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                  text = item.name,
                  style = MaterialTheme.typography.titleSmall,
                  color = Parchment,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
              )
              Text(
                  text = "${item.lines.size} baris",
                  style = MaterialTheme.typography.labelSmall,
                  color = ParchmentDim
              )
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

            if (item.status == FileQueueStatus.DONE) {
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
        Spacer(modifier = Modifier.width(8.dp))
        Text(tr("btnDlAll"))
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
      modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
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
