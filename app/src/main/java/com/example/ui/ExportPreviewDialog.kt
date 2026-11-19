package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.ExportDelimiter
import com.example.domain.ExportEncoding
import com.example.domain.PendingExportPreviewItem
import com.example.ui.theme.Burgundy
import com.example.ui.theme.BurgundyBorder
import com.example.ui.theme.BurgundyBorderSoft
import com.example.ui.theme.Forest
import com.example.ui.theme.ForestText
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.MahoganyInput
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.Parchment
import com.example.ui.theme.ParchmentDim

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportPreviewAndSaveDialog(
    visible: Boolean,
    previewItems: List<PendingExportPreviewItem>,
    exportDelimiter: ExportDelimiter,
    exportEncoding: ExportEncoding,
    prefix: String,
    suffix: String,
    targetLang: String,
    tr: (String) -> String,
    onPrefixChange: (String) -> Unit,
    onSuffixChange: (String) -> Unit,
    onSaveToDownloads: () -> Unit,
    onPickFolder: () -> Unit,
    onDismiss: () -> Unit
) {
  if (!visible || previewItems.isEmpty()) return

  var selectedIndex by remember(previewItems.size) { mutableIntStateOf(0) }
  LaunchedEffect(previewItems.size) {
    if (selectedIndex >= previewItems.size) {
      selectedIndex = 0
    }
  }

  val activeItem = previewItems.getOrElse(selectedIndex) { previewItems.first() }
  val sampleLines = remember(activeItem.formattedContent) {
    activeItem.formattedContent
        .replace("\r\n", "\n")
        .split('\n')
        .filter { it.isNotEmpty() }
        .take(12)
  }
  val sampleText = remember(sampleLines) {
    sampleLines.joinToString("\n").replace("\t", "  [TAB]  ")
  }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF1D1110),
        border = BorderStroke(1.dp, BurgundyBorder),
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .widthIn(max = 560.dp)
    ) {
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
              imageVector = Icons.Default.Visibility,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
          )
          Text(
              text = "${tr("previewExportTitle")} (${previewItems.size})",
              style = MaterialTheme.typography.titleMedium,
              color = GoldAccent
          )
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
              text = tr("previewExportDesc"),
              style = MaterialTheme.typography.bodySmall,
              color = ParchmentDim
          )

          // File selector chips for multi-file batch export
          if (previewItems.size > 1) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              previewItems.forEachIndexed { idx, item ->
                val isSelected = idx == selectedIndex
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Burgundy.copy(alpha = 0.65f) else Color(0xFF170E0D),
                    border = BorderStroke(1.dp, if (isSelected) GoldAccent else BurgundyBorder),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable { selectedIndex = idx }
                        .testTag("export_preview_file_chip_$idx")
                ) {
                  Text(
                      text = "${idx + 1}. ${item.outputFileName}",
                      style = MaterialTheme.typography.labelSmall,
                      color = if (isSelected) GoldAccent else ParchmentDim,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }

          // Output filename + format metadata card
          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF150C0B))
                  .border(1.dp, BurgundyBorderSoft, RoundedCornerShape(10.dp))
                  .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
                text = tr("lblOutputFile"),
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentDim
            )
            Text(
                text = if (activeItem.outputFileName != activeItem.originalFileName) {
                  "${activeItem.originalFileName} → ${activeItem.outputFileName}"
                } else {
                  activeItem.outputFileName
                },
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                color = GoldAccent,
                modifier = Modifier.testTag("export_preview_output_filename")
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              MetaBadgeChip(text = exportDelimiter.displayLabel)
              MetaBadgeChip(text = exportEncoding.charsetName)
              MetaBadgeChip(text = "${sampleLines.size}/${activeItem.lineCount} ${tr("cacheUnit")}")
            }
          }

          // Quick Batch Rename controls inside Preview Export
          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF150C0B))
                  .border(1.dp, BurgundyBorderSoft, RoundedCornerShape(10.dp))
                  .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Default.DriveFileRenameOutline,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                  text = tr("lblQuickRename"),
                  style = MaterialTheme.typography.labelMedium,
                  color = GoldAccent
              )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                  value = prefix,
                  onValueChange = onPrefixChange,
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
                      .testTag("export_preview_prefix_input")
              )
              OutlinedTextField(
                  value = suffix,
                  onValueChange = onSuffixChange,
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
                      .testTag("export_preview_suffix_input")
              )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              QuickRenameChip(
                  label = tr("presetOriginal"),
                  selected = prefix.isEmpty() && suffix.isEmpty(),
                  testTag = "preview_rename_preset_original",
                  onClick = {
                    onPrefixChange("")
                    onSuffixChange("")
                  }
              )
              QuickRenameChip(
                  label = tr("presetTranslated"),
                  selected = suffix == "_translated",
                  testTag = "preview_rename_preset_translated",
                  onClick = { onSuffixChange("_translated") }
              )
              val langSuffix = "_${targetLang.ifBlank { "id" }}"
              QuickRenameChip(
                  label = langSuffix,
                  selected = suffix == langSuffix,
                  testTag = "preview_rename_preset_lang",
                  onClick = { onSuffixChange(langSuffix) }
              )
            }
          }

          // Sample formatted translated content box
          Text(
              text = tr("lblSampleHeader"),
              style = MaterialTheme.typography.labelSmall,
              color = GoldAccent
          )
          Box(
              modifier = Modifier
                  .fillMaxWidth()
                  .height(140.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(ObsidianDeep)
                  .border(1.dp, BurgundyBorder, RoundedCornerShape(10.dp))
                  .verticalScroll(rememberScrollState())
                  .horizontalScroll(rememberScrollState())
                  .padding(10.dp)
                  .testTag("export_preview_sample_box")
          ) {
            Text(
                text = sampleText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                ),
                color = ForestText
            )
          }

          HorizontalDivider(color = BurgundyBorderSoft, thickness = 1.dp)

          Text(
              text = tr("lblSaveDestination"),
              style = MaterialTheme.typography.labelSmall,
              color = ParchmentDim
          )

          Surface(
              onClick = onSaveToDownloads,
              shape = RoundedCornerShape(12.dp),
              color = Forest.copy(alpha = 0.25f),
              border = BorderStroke(1.dp, Forest),
              modifier = Modifier
                  .fillMaxWidth()
                  .testTag("save_to_downloads_option")
          ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                  imageVector = Icons.Default.Download,
                  contentDescription = tr("saveToDownloads"),
                  tint = ForestText
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tr("saveToDownloads"),
                    style = MaterialTheme.typography.titleSmall,
                    color = ForestText
                )
                Text(
                    text = tr("saveToDownloadsSub"),
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentDim
                )
              }
            }
          }

          Surface(
              onClick = onPickFolder,
              shape = RoundedCornerShape(12.dp),
              color = Burgundy.copy(alpha = 0.35f),
              border = BorderStroke(1.dp, GoldMuted),
              modifier = Modifier
                  .fillMaxWidth()
                  .testTag("save_to_folder_option")
          ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                  imageVector = Icons.Default.FolderOpen,
                  contentDescription = tr("saveToFolder"),
                  tint = GoldAccent
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tr("saveToFolder"),
                    style = MaterialTheme.typography.titleSmall,
                    color = Parchment
                )
                Text(
                    text = tr("saveToFolderSub"),
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentDim
                )
              }
            }
          }
        }

        // Bottom dismiss button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(
              onClick = onDismiss,
              border = BorderStroke(1.dp, BurgundyBorder),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentDim),
              modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("export_preview_cancel_button")
          ) {
            Text(tr("btnCancel"))
          }
        }
      }
    }
  }
}

@Composable
private fun MetaBadgeChip(text: String) {
  Surface(
      shape = RoundedCornerShape(6.dp),
      color = Color(0xFF211311),
      border = BorderStroke(1.dp, BurgundyBorderSoft)
  ) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = ParchmentDim,
        maxLines = 1,
        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
    )
  }
}

@Composable
private fun QuickRenameChip(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
  Surface(
      shape = CircleShape,
      color = if (selected) Burgundy.copy(alpha = 0.55f) else Color(0xFF1B100E),
      border = BorderStroke(1.dp, if (selected) GoldAccent else BurgundyBorder),
      modifier = Modifier
          .minimumInteractiveComponentSize()
          .clickable { onClick() }
          .testTag(testTag)
  ) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        ),
        color = if (selected) GoldAccent else ParchmentDim,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}
