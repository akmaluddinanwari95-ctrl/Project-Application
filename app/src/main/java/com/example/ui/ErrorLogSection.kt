package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CsvValidationReport
import com.example.domain.TranslationErrorItem
import com.example.ui.theme.LocalWarbandPalette

@Composable
fun ErrorLogSectionCard(
    entries: List<TranslationErrorItem>,
    isRunning: Boolean,
    tr: (String) -> String,
    onRetryEntry: (String) -> Unit,
    onRetryAllRowErrors: () -> Unit,
    onDismissEntry: (String) -> Unit,
    onClearAll: () -> Unit,
    onExportErrors: () -> Unit,
    onOpenHelp: (String) -> Unit
) {
  if (entries.isEmpty()) return

  val pal = LocalWarbandPalette.current
  val retryableCount = entries.count { !it.isFormatError && it.fileId != null && it.lineIndex != null }

  MedievalCard(modifier = Modifier.testTag("translation_error_log_section")) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = tr("secErrorLog"),
            tint = pal.errorText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${tr("secErrorLog").uppercase()} (${entries.size})",
            style = MaterialTheme.typography.titleMedium,
            color = pal.errorText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        HelpIconButton(
            topicKey = "secErrorLog",
            contentDescription = tr("secErrorLog"),
            onOpenHelp = onOpenHelp
        )
      }

      IconButton(
          onClick = onClearAll,
          modifier = Modifier.testTag("clear_error_log_button")
      ) {
        Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = tr("btnClearLog"),
            tint = pal.textSecondary,
            modifier = Modifier.size(18.dp)
        )
      }
    }

    Text(
        text = tr("errLogHint"),
        style = MaterialTheme.typography.bodySmall,
        color = pal.textSecondary
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (retryableCount > 0) {
        Button(
            onClick = onRetryAllRowErrors,
            enabled = !isRunning,
            colors = ButtonDefaults.buttonColors(
                containerColor = pal.actionBg,
                contentColor = pal.textPrimary
            ),
            border = BorderStroke(1.dp, pal.primaryMuted),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("retry_all_errors_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("${tr("btnRetryAllErrors")} ($retryableCount)", fontSize = 11.sp, maxLines = 1)
        }
      }

      OutlinedButton(
          onClick = onExportErrors,
          border = BorderStroke(1.dp, pal.errorBorder),
          colors = ButtonDefaults.outlinedButtonColors(
              containerColor = pal.errorBg,
              contentColor = pal.errorText
          ),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
              .weight(1f)
              .minimumInteractiveComponentSize()
              .testTag("error_log_export_button")
      ) {
        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(tr("lblErrExport"), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
    }

    HorizontalDivider(
        color = pal.borderSoft,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 10.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      entries.forEachIndexed { idx, entry ->
        var expanded by remember(entry.id) { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(pal.deepSurface)
                .border(1.dp, pal.errorBorder.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                .padding(10.dp)
                .testTag("error_log_entry_$idx"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
              Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (entry.isFormatError) pal.warningBg else pal.errorBg,
                  border = BorderStroke(
                      1.dp,
                      if (entry.isFormatError) pal.primaryMuted else pal.errorBorder
                  )
              ) {
                Text(
                    text = if (entry.isFormatError) {
                      tr("errBadgeFormat")
                    } else {
                      "${tr("errBadgeRow")} #${(entry.lineIndex ?: 0) + 1}"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (entry.isFormatError) pal.warningText else pal.errorText,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }

              Text(
                  text = entry.fileName,
                  style = MaterialTheme.typography.labelMedium,
                  color = pal.textPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
              )
            }

            IconButton(
                onClick = { onDismissEntry(entry.id) },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("dismiss_error_entry_$idx")
            ) {
              Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = tr("btnCancel"),
                  tint = pal.textSecondary,
                  modifier = Modifier.size(15.dp)
              )
            }
          }

          Text(
              text = entry.errorMessage,
              style = MaterialTheme.typography.bodySmall,
              color = pal.errorText
          )

          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
          ) {
            if (entry.originalLine.isNotBlank()) {
              OutlinedButton(
                  onClick = { expanded = !expanded },
                  border = BorderStroke(1.dp, pal.border),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  modifier = Modifier
                      .minimumInteractiveComponentSize()
                      .testTag("inspect_error_entry_$idx")
              ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = tr("btnInspectError"),
                    tint = pal.primaryAccent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(tr("btnInspectError"), fontSize = 11.sp, color = pal.primaryAccent)
              }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!entry.isFormatError && entry.fileId != null && entry.lineIndex != null) {
              Button(
                  onClick = { onRetryEntry(entry.id) },
                  enabled = !isRunning && !entry.isRetrying,
                  colors = ButtonDefaults.buttonColors(
                      containerColor = pal.actionBg,
                      contentColor = pal.textPrimary
                  ),
                  border = BorderStroke(1.dp, pal.primaryMuted),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                      .minimumInteractiveComponentSize()
                      .testTag("retry_error_entry_$idx")
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (entry.isRetrying) tr("errRetrying") else tr("btnRetryRow"),
                    fontSize = 11.sp
                )
              }
            }
          }

          AnimatedVisibility(visible = expanded && entry.originalLine.isNotBlank()) {
            Text(
                text = entry.originalLine,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = pal.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(pal.cardBottom)
                    .border(1.dp, pal.borderSoft, RoundedCornerShape(6.dp))
                    .padding(8.dp)
                    .testTag("inspected_error_line_$idx")
            )
          }
        }
      }
    }
  }
}

@Composable
fun CsvValidationWarningDialog(
    visible: Boolean,
    reports: List<CsvValidationReport>,
    tr: (String) -> String,
    onProceedAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
  if (!visible || reports.isEmpty()) return
  val pal = LocalWarbandPalette.current

  AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = pal.cardTop,
      shape = RoundedCornerShape(18.dp),
      modifier = Modifier.testTag("csv_validation_dialog"),
      title = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = pal.warningText
          )
          Text(
              text = tr("csvValTitle"),
              style = MaterialTheme.typography.titleMedium,
              color = pal.warningText
          )
        }
      },
      text = {
        Column(
            modifier = Modifier
                .heightIn(max = 340.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
              text = tr("csvValDesc"),
              style = MaterialTheme.typography.bodySmall,
              color = pal.textSecondary
          )
          reports.take(4).forEach { rep ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(pal.deepSurface)
                    .border(1.dp, pal.errorBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                  text = rep.fileName,
                  style = MaterialTheme.typography.titleSmall,
                  color = pal.textPrimary
              )
              Text(
                  text = rep.summaryMessage,
                  style = MaterialTheme.typography.labelSmall,
                  color = pal.warningText
              )
              rep.issues.take(2).forEach { issue ->
                Text(
                    text = "#${issue.lineNumber}: ${issue.reason}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = pal.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
            onClick = onProceedAnyway,
            colors = ButtonDefaults.buttonColors(
                containerColor = pal.actionBg,
                contentColor = pal.textPrimary
            ),
            border = BorderStroke(1.dp, pal.primaryMuted),
            modifier = Modifier.testTag("validation_proceed_button")
        ) {
          Text(tr("btnProceedAnyway"))
        }
      },
      dismissButton = {
        OutlinedButton(
            onClick = onDismiss,
            border = BorderStroke(1.dp, pal.border),
            modifier = Modifier.testTag("validation_review_button")
        ) {
          Text(tr("btnFixSeparator"), color = pal.textSecondary)
        }
      }
  )
}

@Composable
fun AddCustomLanguageDialog(
    visible: Boolean,
    tr: (String) -> String,
    onAdd: (code: String, label: String) -> Unit,
    onDismiss: () -> Unit
) {
  if (!visible) return
  val pal = LocalWarbandPalette.current
  var code by remember { mutableStateOf("") }
  var label by remember { mutableStateOf("") }

  AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = pal.cardTop,
      shape = RoundedCornerShape(18.dp),
      modifier = Modifier.testTag("add_custom_lang_dialog"),
      title = {
        Text(
            text = tr("addLangTitle"),
            style = MaterialTheme.typography.titleMedium,
            color = pal.primaryAccent
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
              value = code,
              onValueChange = { code = it.take(10) },
              singleLine = true,
              label = { Text(tr("addLangCode"), fontSize = 11.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = pal.primaryAccent,
                  unfocusedBorderColor = pal.border,
                  focusedContainerColor = pal.inputBg,
                  unfocusedContainerColor = pal.inputBg,
                  focusedTextColor = pal.textPrimary,
                  unfocusedTextColor = pal.textPrimary
              ),
              modifier = Modifier
                  .fillMaxWidth()
                  .testTag("custom_lang_code_input")
          )
          OutlinedTextField(
              value = label,
              onValueChange = { label = it.take(40) },
              singleLine = true,
              label = { Text(tr("addLangLabel"), fontSize = 11.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = pal.primaryAccent,
                  unfocusedBorderColor = pal.border,
                  focusedContainerColor = pal.inputBg,
                  unfocusedContainerColor = pal.inputBg,
                  focusedTextColor = pal.textPrimary,
                  unfocusedTextColor = pal.textPrimary
              ),
              modifier = Modifier
                  .fillMaxWidth()
                  .testTag("custom_lang_label_input")
          )
        }
      },
      confirmButton = {
        Button(
            onClick = {
              if (code.trim().isNotEmpty()) {
                onAdd(code.trim().lowercase(), label.trim())
                onDismiss()
              }
            },
            enabled = code.trim().isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = pal.actionBg,
                contentColor = pal.textPrimary
            ),
            modifier = Modifier.testTag("save_custom_lang_button")
        ) {
          Text(tr("btnSave"))
        }
      },
      dismissButton = {
        OutlinedButton(onClick = onDismiss) {
          Text(tr("btnCancel"), color = pal.textSecondary)
        }
      }
  )
}
