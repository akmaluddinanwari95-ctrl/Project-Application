package com.example.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ExportDelimiter
import com.example.domain.ExportEncoding
import com.example.domain.TranslatorSettings
import com.example.domain.UiLanguage
import com.example.domain.WarbandTextProcessor
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
import com.example.ui.theme.MahoganyInput
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.Parchment
import com.example.ui.theme.ParchmentDim

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreenContent(
    settings: TranslatorSettings,
    cacheCount: Int,
    tr: (String) -> String,
    onSelectExportDelimiter: (ExportDelimiter) -> Unit,
    onSelectExportEncoding: (ExportEncoding) -> Unit,
    onExportPrefixChange: (String) -> Unit = {},
    onExportSuffixChange: (String) -> Unit = {},
    onSelectUiLanguage: (UiLanguage) -> Unit,
    onToggleMedievalTheme: (Boolean) -> Unit = {},
    onClearCache: () -> Unit,
    onExportCacheBackup: () -> Unit = {},
    onImportCacheBackup: () -> Unit = {},
    onOpenHelp: (String) -> Unit
) {
  val pal = LocalWarbandPalette.current
  val sampleExport = remember(settings.separator, settings.exportDelimiter) {
    WarbandTextProcessor.formatExportContent(
        lines = listOf(
            "str_welcome|Selamat datang di Calradia, petualang!",
            "str_swadia_king|Raja Harlaus dari Kerajaan Swadia"
        ),
        sourceSeparator = "|",
        exportDelimiter = settings.exportDelimiter
    ).trimEnd()
  }

  Column(
      modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 14.dp, vertical = 14.dp)
          .testTag("settings_screen_content"),
      horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
        modifier = Modifier.widthIn(max = 720.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Export Delimiter & Encoding Card
      MedievalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = tr("secExportSettings"),
                tint = GoldAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tr("secExportSettings").uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent,
                modifier = Modifier.weight(1f, fill = false)
            )
            HelpIconButton(
                topicKey = "secExportSettings",
                contentDescription = tr("secExportSettings"),
                onOpenHelp = onOpenHelp
            )
          }
        }

        Text(
            text = tr("exportSettingsDesc"),
            style = MaterialTheme.typography.bodySmall,
            color = ParchmentDim
        )

        HorizontalDivider(
            color = BurgundyBorderSoft,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // CSV Delimiter section
        Text(
            text = tr("lblExportDelimiter"),
            style = MaterialTheme.typography.labelLarge,
            color = GoldAccent
        )
        Spacer(modifier = Modifier.height(8.dp))

        val delimiterMeta = listOf(
            ExportDelimiter.COMMA to tr("delCommaDesc"),
            ExportDelimiter.TAB to tr("delTabDesc"),
            ExportDelimiter.SEMICOLON to tr("delSemicolonDesc"),
            ExportDelimiter.SOURCE_PIPE to tr("delPipeDesc")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          delimiterMeta.forEach { (delimiter, desc) ->
            val selected = settings.exportDelimiter == delimiter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Color(0xFF2B1917) else Color(0xFF170E0D))
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) GoldAccent else BurgundyBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectExportDelimiter(delimiter) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("export_delimiter_${delimiter.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                  selected = selected,
                  onClick = { onSelectExportDelimiter(delimiter) },
                  colors = RadioButtonDefaults.colors(
                      selectedColor = GoldAccent,
                      unselectedColor = GoldMuted
                  )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = delimiter.displayLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = Parchment
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentDim
                )
              }
              if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Character Encoding section
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              imageVector = Icons.Default.TextFields,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
              text = tr("lblExportEncoding"),
              style = MaterialTheme.typography.labelLarge,
              color = GoldAccent
          )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val encodingMeta = listOf(
            ExportEncoding.UTF_8 to tr("encUtf8Desc"),
            ExportEncoding.UTF_8_BOM to tr("encUtf8BomDesc"),
            ExportEncoding.UTF_16_LE to tr("encUtf16LeDesc"),
            ExportEncoding.UTF_16_BE to tr("encUtf16BeDesc")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          encodingMeta.forEach { (encoding, desc) ->
            val selected = settings.exportEncoding == encoding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Color(0xFF2B1917) else Color(0xFF170E0D))
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) GoldAccent else BurgundyBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectExportEncoding(encoding) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("export_encoding_${encoding.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                  selected = selected,
                  onClick = { onSelectExportEncoding(encoding) },
                  colors = RadioButtonDefaults.colors(
                      selectedColor = GoldAccent,
                      unselectedColor = GoldMuted
                  )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = encoding.displayLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = Parchment
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentDim
                )
              }
              Surface(
                  shape = CircleShape,
                  color = if (selected) Color(0xFF0E2914) else Color(0xFF1F1312),
                  border = BorderStroke(1.dp, if (selected) Forest else BurgundyBorder)
              ) {
                Text(
                    text = encoding.charsetName,
                    color = if (selected) ForestText else ParchmentDim,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Batch Rename Output Files section
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              imageVector = Icons.Default.DriveFileRenameOutline,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
              text = tr("lblBatchRename"),
              style = MaterialTheme.typography.labelLarge,
              color = GoldAccent
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tr("hintBatchRename"),
            style = MaterialTheme.typography.bodySmall,
            color = ParchmentDim
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
              value = settings.exportFilenamePrefix,
              onValueChange = onExportPrefixChange,
              singleLine = true,
              label = { Text(tr("lblFilePrefix"), fontSize = 11.sp) },
              placeholder = { Text("id_", fontSize = 11.sp, color = ParchmentDim.copy(alpha = 0.4f)) },
              textStyle = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
                  color = Parchment
              ),
              colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GoldAccent,
                  unfocusedBorderColor = BurgundyBorder,
                  focusedContainerColor = MahoganyInput,
                  unfocusedContainerColor = MahoganyInput,
                  focusedLabelColor = GoldAccent,
                  unfocusedLabelColor = ParchmentDim
              ),
              modifier = Modifier
                  .weight(1f)
                  .testTag("export_prefix_input")
          )
          OutlinedTextField(
              value = settings.exportFilenameSuffix,
              onValueChange = onExportSuffixChange,
              singleLine = true,
              label = { Text(tr("lblFileSuffix"), fontSize = 11.sp) },
              placeholder = { Text("_translated", fontSize = 11.sp, color = ParchmentDim.copy(alpha = 0.4f)) },
              textStyle = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
                  color = Parchment
              ),
              colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GoldAccent,
                  unfocusedBorderColor = BurgundyBorder,
                  focusedContainerColor = MahoganyInput,
                  unfocusedContainerColor = MahoganyInput,
                  focusedLabelColor = GoldAccent,
                  unfocusedLabelColor = ParchmentDim
              ),
              modifier = Modifier
                  .weight(1f)
                  .testTag("export_suffix_input")
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val presets = listOf(
              Triple(tr("presetOriginal"), "" to "", "rename_preset_original"),
              Triple(tr("presetTranslated"), settings.exportFilenamePrefix to "_translated", "rename_preset_translated"),
              Triple("_${settings.targetLang}", settings.exportFilenamePrefix to "_${settings.targetLang}", "rename_preset_lang")
          )
          presets.forEach { (label, pair, tag) ->
            val selected = if (tag == "rename_preset_original") {
              settings.exportFilenamePrefix.isEmpty() && settings.exportFilenameSuffix.isEmpty()
            } else {
              settings.exportFilenameSuffix == pair.second
            }
            Surface(
                shape = CircleShape,
                color = if (selected) Burgundy.copy(alpha = 0.55f) else Color(0xFF170E0D),
                border = BorderStroke(1.dp, if (selected) GoldAccent else BurgundyBorder),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable {
                      if (tag == "rename_preset_original") {
                        onExportPrefixChange("")
                        onExportSuffixChange("")
                      } else {
                        onExportSuffixChange(pair.second)
                      }
                    }
                    .testTag(tag)
            ) {
              Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (selected) GoldAccent else ParchmentDim,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val renamedSample = remember(settings.exportFilenamePrefix, settings.exportFilenameSuffix) {
          WarbandTextProcessor.buildExportFileName(
              originalName = "dialogs.csv",
              prefix = settings.exportFilenamePrefix,
              suffix = settings.exportFilenameSuffix
          )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianDeep)
                .border(1.dp, BurgundyBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .testTag("settings_rename_preview"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
              text = tr("lblRenamePreview"),
              style = MaterialTheme.typography.labelSmall,
              color = ParchmentDim
          )
          Text(
              text = "dialogs.csv  →  $renamedSample",
              style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.SemiBold
              ),
              color = GoldAccent
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live export sample preview
        Text(
            text = tr("lblExportPreview"),
            style = MaterialTheme.typography.labelSmall,
            color = ParchmentDim
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianDeep)
                .border(1.dp, BurgundyBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
                .testTag("settings_export_sample_preview")
        ) {
          Text(
              text = sampleExport.replace("\t", "  [TAB]  "),
              style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp
              ),
              color = ForestText
          )
        }
      }

      // Interface Language, Medieval Theme & Cache Maintenance Card
      MedievalCard {
        Text(
            text = tr("secUiPref").uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = pal.primaryAccent
        )
        HorizontalDivider(
            color = pal.borderSoft,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        // Medieval Theme Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(pal.deepSurface)
                .border(
                    width = 1.dp,
                    color = if (settings.medievalThemeEnabled) pal.primaryAccent else pal.border,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onToggleMedievalTheme(!settings.medievalThemeEnabled) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = tr("lblMedievalTheme"),
              tint = pal.primaryAccent,
              modifier = Modifier.size(20.dp)
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tr("lblMedievalTheme"),
                style = MaterialTheme.typography.titleSmall,
                color = pal.textPrimary
            )
            Text(
                text = tr("lblMedievalThemeSub"),
                style = MaterialTheme.typography.labelSmall,
                color = pal.textSecondary
            )
          }
          Switch(
              checked = settings.medievalThemeEnabled,
              onCheckedChange = onToggleMedievalTheme,
              colors = SwitchDefaults.colors(
                  checkedThumbColor = pal.textPrimary,
                  checkedTrackColor = pal.actionBg,
                  checkedBorderColor = pal.primaryAccent,
                  uncheckedThumbColor = pal.textSecondary,
                  uncheckedTrackColor = pal.inputBg
              ),
              modifier = Modifier.testTag("medieval_theme_switch")
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          UiLanguage.entries.forEach { langItem ->
            val selected = settings.uiLanguage == langItem
            OutlinedButton(
                onClick = { onSelectUiLanguage(langItem) },
                border = BorderStroke(1.dp, if (selected) pal.primaryAccent else pal.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) pal.actionBg.copy(alpha = 0.55f) else pal.deepSurface,
                    contentColor = if (selected) pal.primaryAccent else pal.textSecondary
                ),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("settings_ui_lang_${langItem.code}")
            ) {
              Text(langItem.label, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF160D0C))
                .border(1.dp, BurgundyBorderSoft, RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tr("lblCache"),
                style = MaterialTheme.typography.labelMedium,
                color = Parchment
            )
            Text(
                text = "$cacheCount ${tr("cacheUnit")}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = GoldAccent
            )
          }

          OutlinedButton(
              onClick = onClearCache,
              border = BorderStroke(1.dp, Crimson),
              colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Crimson.copy(alpha = 0.25f),
                  contentColor = CrimsonText
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("settings_clear_cache_button")
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(tr("btnClearCache"), fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
              onClick = onExportCacheBackup,
              border = BorderStroke(1.dp, Burgundy),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Parchment),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                  .weight(1f)
                  .minimumInteractiveComponentSize()
                  .testTag("export_cache_json_button")
          ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(tr("btnExpCacheJson"), fontSize = 11.sp, maxLines = 1)
          }

          OutlinedButton(
              onClick = onImportCacheBackup,
              border = BorderStroke(1.dp, Burgundy),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Parchment),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                  .weight(1f)
                  .minimumInteractiveComponentSize()
                  .testTag("import_cache_json_button")
          ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(tr("btnImpCacheJson"), fontSize = 11.sp, maxLines = 1)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
