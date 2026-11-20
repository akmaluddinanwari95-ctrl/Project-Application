package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.domain.CsvStructurePreview
import com.example.domain.QueuedFileItem
import com.example.ui.theme.LocalWarbandPalette

@Composable
fun CsvPreviewerCard(
    fileQueue: List<QueuedFileItem>,
    preview: CsvStructurePreview?,
    tr: (String) -> String,
    onSelectFile: (String) -> Unit,
    onOpenHelp: (String) -> Unit
) {
  if (preview == null || fileQueue.isEmpty()) return
  val pal = LocalWarbandPalette.current

  MedievalCard(modifier = Modifier.testTag("csv_structure_preview_card")) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(
            imageVector = Icons.Default.TableChart,
            contentDescription = tr("secCsvStruct"),
            tint = pal.primaryAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = tr("secCsvStruct").uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = pal.primaryAccent,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        HelpIconButton(
            topicKey = "secCsvStruct",
            contentDescription = tr("secCsvStruct"),
            onOpenHelp = onOpenHelp
        )
      }
    }

    Text(
        text = tr("csvStructHint"),
        style = MaterialTheme.typography.labelSmall,
        color = pal.textSecondary
    )

    if (fileQueue.size > 1) {
      Spacer(modifier = Modifier.height(10.dp))
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        fileQueue.forEachIndexed { idx, item ->
          val selected = item.id == preview.fileId
          Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selected) pal.actionBg.copy(alpha = 0.55f) else pal.deepSurface,
              border = BorderStroke(1.dp, if (selected) pal.primaryAccent else pal.border),
              modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .clickable { onSelectFile(item.id) }
                  .testTag("csv_preview_tab_${idx}")
          ) {
            Text(
                text = "${idx + 1}. ${item.name}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selected) pal.primaryAccent else pal.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
          }
        }
      }
    }

    HorizontalDivider(
        color = pal.borderSoft,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 10.dp)
    )

    // Structure verification diagnostic pill
    val rowCount = preview.rows.size
    val sepLooksHealthy = rowCount == 0 || preview.matchedSeparatorCount > 0
    val diagBg = if (sepLooksHealthy) pal.successBg else pal.warningBg
    val diagBorder = if (sepLooksHealthy) pal.successBorder else pal.primaryMuted
    val diagText = if (sepLooksHealthy) pal.successText else pal.warningText

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = diagBg,
        border = BorderStroke(1.dp, diagBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
      Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
            imageVector = if (sepLooksHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = diagText,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (sepLooksHealthy) {
              "${preview.fileName}: ${preview.matchedSeparatorCount}/$rowCount ${tr("csvSepMatchOk")} '${preview.separator}' · Total ${preview.totalLines}"
            } else {
              "${preview.fileName}: ${tr("csvSepMatchWarn")} '${preview.separator}'"
            },
            style = MaterialTheme.typography.labelSmall,
            color = diagText
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Scrollable structured 10-row CSV table
    val horizontalScroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(pal.deepSurface)
            .border(1.dp, pal.border, RoundedCornerShape(10.dp))
            .horizontalScroll(horizontalScroll)
    ) {
      // Table header
      Row(
          modifier = Modifier
              .background(pal.cardElevated)
              .padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
            text = tr("csvColIdx"),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = pal.primaryAccent,
            modifier = Modifier.width(34.dp)
        )
        Text(
            text = tr("csvColKey"),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = pal.primaryAccent,
            modifier = Modifier.width(170.dp)
        )
        Text(
            text = tr("csvColText"),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = pal.primaryAccent,
            modifier = Modifier.width(260.dp)
        )
        Text(
            text = tr("csvColStatus"),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = pal.primaryAccent,
            modifier = Modifier.width(110.dp)
        )
      }

      HorizontalDivider(color = pal.border, thickness = 1.dp)

      preview.rows.forEachIndexed { idx, row ->
        val rowBg = if (idx % 2 == 0) pal.deepSurface else pal.cardBottom
        Row(
            modifier = Modifier
                .background(rowBg)
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .testTag("csv_preview_row_${row.rowNumber}"),
            verticalAlignment = Alignment.Top
        ) {
          Text(
              text = "${row.rowNumber}",
              style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
              color = pal.textSecondary.copy(alpha = 0.7f),
              modifier = Modifier.width(34.dp)
          )

          Text(
              text = if (row.hasSeparator) row.columnKey else "—",
              style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp
              ),
              color = if (row.hasSeparator) pal.primaryAccent.copy(alpha = 0.92f) else pal.warningText,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                  .width(170.dp)
                  .padding(end = 8.dp)
          )

          Text(
              text = row.columnText.ifEmpty { row.rawLine },
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = if (row.isSkippedByRegex) pal.textSecondary.copy(alpha = 0.55f) else pal.textPrimary,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                  .width(260.dp)
                  .padding(end = 8.dp)
          )

          Box(modifier = Modifier.width(110.dp)) {
            when {
              row.isSkippedByRegex -> {
                PreviewTagPill(
                    text = tr("csvSkipTag"),
                    bg = pal.warningBg,
                    border = pal.primaryMuted,
                    textColor = pal.warningText
                )
              }
              !row.hasSeparator -> {
                PreviewTagPill(
                    text = tr("csvNoSepTag"),
                    bg = pal.errorBg.copy(alpha = 0.65f),
                    border = pal.border,
                    textColor = pal.textSecondary
                )
              }
              row.protectedTokens.isNotEmpty() -> {
                PreviewTagPill(
                    text = row.protectedTokens.take(2).joinToString(" "),
                    bg = pal.successBg,
                    border = pal.successBorder,
                    textColor = pal.successText
                )
              }
              else -> {
                PreviewTagPill(
                    text = "OK",
                    bg = pal.cardElevated,
                    border = pal.borderSoft,
                    textColor = pal.textSecondary
                )
              }
            }
          }
        }
        if (idx < preview.rows.lastIndex) {
          HorizontalDivider(color = pal.borderSoft.copy(alpha = 0.55f), thickness = 0.5.dp)
        }
      }
    }
  }
}

@Composable
private fun PreviewTagPill(
    text: String,
    bg: Color,
    border: Color,
    textColor: Color
) {
  Surface(
      shape = RoundedCornerShape(5.dp),
      color = bg,
      border = BorderStroke(1.dp, border)
  ) {
    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
  }
}
