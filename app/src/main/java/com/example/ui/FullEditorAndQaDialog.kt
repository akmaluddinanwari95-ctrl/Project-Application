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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.domain.QaAuditRow
import com.example.domain.QaAuditSummary
import com.example.domain.QaIssueType
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
import com.example.ui.theme.MahoganyInput
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.Parchment
import com.example.ui.theme.ParchmentDim

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullEditorAndQaDialog(
    summary: QaAuditSummary?,
    tr: (String) -> String,
    onSaveLine: (fileId: String, lineIndex: Int, newLine: String) -> Unit,
    onFindAndReplace: (fileId: String, findQuery: String, replaceWith: String, caseSensitive: Boolean) -> Unit,
    onAutoFixMissingTokens: (fileId: String) -> Unit,
    onDismiss: () -> Unit
) {
  if (summary == null) return

  var searchQuery by remember(summary.fileId) { mutableStateOf("") }
  var onlyIssues by remember(summary.fileId) { mutableStateOf(summary.issueCount > 0) }
  var findText by remember(summary.fileId) { mutableStateOf("") }
  var replaceText by remember(summary.fileId) { mutableStateOf("") }
  var caseSensitive by remember(summary.fileId) { mutableStateOf(false) }
  var editingLineIndex by remember(summary.fileId) { mutableIntStateOf(-1) }
  var editingDraft by remember(summary.fileId) { mutableStateOf("") }

  val filteredRows = remember(summary.rows, searchQuery, onlyIssues) {
    val q = searchQuery.trim()
    summary.rows.filter { row ->
      val passesIssue = !onlyIssues || row.hasIssue
      val passesQuery = q.isEmpty() ||
          row.originalLine.contains(q, ignoreCase = true) ||
          row.translatedLine.contains(q, ignoreCase = true) ||
          (row.lineIndex + 1).toString() == q
      passesIssue && passesQuery
    }
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
            .padding(horizontal = 14.dp, vertical = 20.dp)
            .widthIn(max = 620.dp)
            .testTag("qa_editor_dialog")
    ) {
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
          ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                  text = tr("qaEditorTitle"),
                  style = MaterialTheme.typography.titleMedium,
                  color = GoldAccent,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
              )
              Text(
                  text = summary.fileName,
                  style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                  color = ParchmentDim,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
              )
            }
          }

          if (summary.issueCount > 0) {
            OutlinedButton(
                onClick = { onAutoFixMissingTokens(summary.fileId) },
                border = BorderStroke(1.dp, GoldMuted),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AmberBg,
                    contentColor = GoldAccent
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("qa_auto_fix_button")
            ) {
              Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(tr("qaBtnAutoFix"), fontSize = 11.sp, maxLines = 1)
            }
          }
        }

        // QA Status Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (summary.issueCount == 0) Color(0xFF0E2914) else Color(0xFF2E170F),
            border = BorderStroke(1.dp, if (summary.issueCount == 0) Forest else AmberText),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qa_status_banner")
        ) {
          Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
                imageVector = if (summary.issueCount == 0) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                contentDescription = null,
                tint = if (summary.issueCount == 0) ForestText else AmberText,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (summary.issueCount == 0) {
                  tr("qaNoIssuesBanner")
                } else {
                  "${summary.issueCount}/${summary.totalRows} ${tr("qaIssuesFoundBanner")}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (summary.issueCount == 0) ForestText else AmberText
            )
          }
        }

        // Search & QA Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = GoldMuted, modifier = Modifier.size(16.dp))
              },
              placeholder = { Text(tr("qaSearchPlaceholder"), fontSize = 12.sp, color = ParchmentDim.copy(alpha = 0.5f)) },
              textStyle = MaterialTheme.typography.bodySmall.copy(color = Parchment),
              colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GoldAccent,
                  unfocusedBorderColor = BurgundyBorder,
                  focusedContainerColor = MahoganyInput,
                  unfocusedContainerColor = MahoganyInput
              ),
              modifier = Modifier
                  .weight(1f)
                  .testTag("qa_search_input")
          )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChipButton(
              label = "${tr("qaFilterAll")} (${summary.totalRows})",
              selected = !onlyIssues,
              testTag = "qa_filter_all",
              onClick = { onlyIssues = false }
          )
          FilterChipButton(
              label = "${tr("qaFilterIssues")} (${summary.issueCount})",
              selected = onlyIssues,
              testTag = "qa_filter_issues",
              onClick = { onlyIssues = true }
          )
        }

        // Find & Replace Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF150C0B))
                .border(1.dp, BurgundyBorderSoft, RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            OutlinedTextField(
                value = findText,
                onValueChange = { findText = it },
                singleLine = true,
                label = { Text(tr("qaFindLabel"), fontSize = 11.sp) },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Parchment),
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
                    .testTag("qa_find_input")
            )
            OutlinedTextField(
                value = replaceText,
                onValueChange = { replaceText = it },
                singleLine = true,
                label = { Text(tr("qaReplaceLabel"), fontSize = 11.sp) },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Parchment),
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
                    .testTag("qa_replace_input")
            )
          }

          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { caseSensitive = !caseSensitive }
            ) {
              Checkbox(
                  checked = caseSensitive,
                  onCheckedChange = { caseSensitive = it },
                  colors = CheckboxDefaults.colors(
                      checkedColor = Burgundy,
                      uncheckedColor = GoldMuted,
                      checkmarkColor = Parchment
                  )
              )
              Text(tr("qaCaseSensitive"), style = MaterialTheme.typography.labelSmall, color = ParchmentDim)
            }

            Button(
                onClick = { onFindAndReplace(summary.fileId, findText, replaceText, caseSensitive) },
                enabled = findText.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Burgundy,
                    contentColor = Parchment
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("qa_replace_all_button")
            ) {
              Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(tr("qaBtnReplaceAll"), fontSize = 11.sp)
            }
          }
        }

        // Scrollable Bounded Row List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianDeep)
                .border(1.dp, BurgundyBorder, RoundedCornerShape(10.dp))
                .padding(8.dp)
                .testTag("qa_rows_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredRows, key = { it.lineIndex }) { row ->
            QaRowCard(
                row = row,
                isEditing = editingLineIndex == row.lineIndex,
                draft = if (editingLineIndex == row.lineIndex) editingDraft else row.translatedLine,
                tr = tr,
                onStartEdit = {
                  editingLineIndex = row.lineIndex
                  editingDraft = row.translatedLine
                },
                onDraftChange = { editingDraft = it },
                onCancelEdit = { editingLineIndex = -1 },
                onSaveEdit = {
                  onSaveLine(summary.fileId, row.lineIndex, editingDraft)
                  editingLineIndex = -1
                }
            )
          }
        }

        HorizontalDivider(color = BurgundyBorderSoft, thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(
              onClick = onDismiss,
              border = BorderStroke(1.dp, BurgundyBorder),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Parchment),
              modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("qa_editor_close_button")
          ) {
            Text(tr("btnCloseHelp"))
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QaRowCard(
    row: QaAuditRow,
    isEditing: Boolean,
    draft: String,
    tr: (String) -> String,
    onStartEdit: () -> Unit,
    onDraftChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit
) {
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(if (row.hasIssue) Color(0xFF241212) else Color(0xFF150D0C))
          .border(1.dp, if (row.hasIssue) Crimson else BurgundyBorderSoft, RoundedCornerShape(8.dp))
          .padding(8.dp)
          .testTag("qa_row_${row.lineIndex}"),
      verticalArrangement = Arrangement.spacedBy(4.dp)
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
        Text(
            text = "#${row.lineIndex + 1}",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            color = GoldAccent
        )
        if (row.issues.isNotEmpty()) {
          FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            row.issues.forEach { issue ->
              val label = when (issue) {
                QaIssueType.MISSING_TOKEN -> "Missing: ${row.missingTokens.joinToString(",")}"
                QaIssueType.EXTRA_TOKEN -> "Extra: ${row.extraTokens.joinToString(",")}"
                QaIssueType.BROKEN_SEPARATOR -> "Separator!"
                QaIssueType.LEFT_MARKER -> "ZXQ Marker!"
                QaIssueType.UNTRANSLATED -> "Untranslated"
              }
              Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Crimson.copy(alpha = 0.35f),
                  border = BorderStroke(1.dp, Crimson)
              ) {
                Text(
                    text = label,
                    color = CrimsonText,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
              }
            }
          }
        }
      }

      if (!isEditing) {
        TextButton(
            onClick = onStartEdit,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.testTag("qa_edit_row_${row.lineIndex}")
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(tr("btnEdit"), color = GoldAccent, fontSize = 11.sp)
        }
      }
    }

    Text(
        text = row.originalLine,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
        color = ParchmentDim.copy(alpha = 0.85f)
    )

    if (!isEditing) {
      Text(
          text = row.translatedLine,
          style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
          color = ForestText
      )
    } else {
      OutlinedTextField(
          value = draft,
          onValueChange = onDraftChange,
          textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Parchment, fontSize = 11.sp),
          colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldAccent,
              unfocusedBorderColor = BurgundyBorder,
              focusedContainerColor = MahoganyInput,
              unfocusedContainerColor = MahoganyInput
          ),
          modifier = Modifier
              .fillMaxWidth()
              .testTag("qa_edit_input_${row.lineIndex}")
      )
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(onClick = onCancelEdit) {
          Text(tr("btnCancel"), color = ParchmentDim, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Button(
            onClick = onSaveEdit,
            colors = ButtonDefaults.buttonColors(containerColor = Forest, contentColor = ForestText),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("qa_save_row_${row.lineIndex}")
        ) {
          Text(tr("btnSave"), fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
private fun FilterChipButton(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
  Surface(
      shape = CircleShape,
      color = if (selected) Burgundy.copy(alpha = 0.6f) else Color(0xFF170E0D),
      border = BorderStroke(1.dp, if (selected) GoldAccent else BurgundyBorder),
      modifier = Modifier
          .minimumInteractiveComponentSize()
          .clickable { onClick() }
          .testTag(testTag)
  ) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
        color = if (selected) GoldAccent else ParchmentDim,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}
