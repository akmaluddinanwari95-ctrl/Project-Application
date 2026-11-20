package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.ModDiffMergeResult
import com.example.ui.theme.LocalWarbandPalette

@Composable
fun ModDiffMergeDialog(
    visible: Boolean,
    newModName: String,
    oldTransName: String,
    diffPreview: ModDiffMergeResult?,
    tr: (String) -> String,
    onPickNewModFile: () -> Unit,
    onPickOldTranslatedFile: () -> Unit,
    onApplyDiff: () -> Unit,
    onDismiss: () -> Unit
) {
  if (!visible) return
  val pal = LocalWarbandPalette.current

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = pal.cardTop,
        border = BorderStroke(1.dp, pal.border),
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .widthIn(max = 520.dp)
            .heightIn(max = 640.dp)
            .testTag("mod_diff_dialog")
    ) {
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
              imageVector = Icons.Default.CompareArrows,
              contentDescription = null,
              tint = pal.primaryAccent,
              modifier = Modifier.size(20.dp)
          )
          Text(
              text = tr("modDiffTitle"),
              style = MaterialTheme.typography.titleMedium,
              color = pal.primaryAccent
          )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
              text = tr("modDiffDesc"),
              style = MaterialTheme.typography.bodySmall,
              color = pal.textSecondary
          )

          OutlinedButton(
              onClick = onPickNewModFile,
              border = BorderStroke(1.dp, if (newModName.isNotEmpty()) pal.primaryAccent else pal.border),
              colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = pal.deepSurface,
                  contentColor = pal.textPrimary
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
              modifier = Modifier
                  .fillMaxWidth()
                  .minimumInteractiveComponentSize()
                  .testTag("diff_pick_new_button")
          ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, tint = pal.primaryAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(tr("modDiffPickNew"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              if (newModName.isNotEmpty()) {
                Text(
                    text = newModName,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = pal.successText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          OutlinedButton(
              onClick = onPickOldTranslatedFile,
              border = BorderStroke(1.dp, if (oldTransName.isNotEmpty()) pal.primaryAccent else pal.border),
              colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = pal.deepSurface,
                  contentColor = pal.textPrimary
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
              modifier = Modifier
                  .fillMaxWidth()
                  .minimumInteractiveComponentSize()
                  .testTag("diff_pick_old_button")
          ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, tint = pal.primaryAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(tr("modDiffPickOld"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              if (oldTransName.isNotEmpty()) {
                Text(
                    text = oldTransName,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = pal.successText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          if (diffPreview != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(pal.deepSurface)
                    .border(1.dp, pal.border, RoundedCornerShape(10.dp))
                    .padding(12.dp)
                    .testTag("mod_diff_summary_box"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                  text = tr("modDiffSummary"),
                  style = MaterialTheme.typography.labelMedium,
                  color = pal.primaryAccent
              )
              DiffStatRow(label = tr("modDiffReused"), count = diffPreview.carriedOverCount, valueColor = pal.successText)
              DiffStatRow(label = tr("modDiffNew"), count = diffPreview.newOrChangedCount, valueColor = pal.warningText)
              DiffStatRow(label = tr("modDiffSkipped"), count = diffPreview.skippedCount, valueColor = pal.textSecondary)
            }
          }
        }

        HorizontalDivider(color = pal.borderSoft, thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
              onClick = onDismiss,
              border = BorderStroke(1.dp, pal.border),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = pal.textSecondary),
              modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("diff_cancel_button")
          ) {
            Text(tr("btnCancel"))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
              onClick = onApplyDiff,
              enabled = diffPreview != null,
              colors = ButtonDefaults.buttonColors(
                  containerColor = pal.actionBg,
                  contentColor = pal.textPrimary
              ),
              border = BorderStroke(1.dp, if (diffPreview != null) pal.primaryMuted else pal.border),
              modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("diff_apply_button")
          ) {
            Text(tr("modDiffApply"))
          }
        }
      }
    }
  }
}

@Composable
private fun DiffStatRow(label: String, count: Int, valueColor: Color) {
  val pal = LocalWarbandPalette.current
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, style = MaterialTheme.typography.bodySmall, color = pal.textSecondary)
    Text(
        text = count.toString(),
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
        color = valueColor
    )
  }
}
