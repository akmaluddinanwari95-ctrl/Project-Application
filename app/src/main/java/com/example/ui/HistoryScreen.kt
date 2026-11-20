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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TranslationHistoryEntity
import com.example.domain.DailyProductivityStat
import com.example.ui.theme.LocalWarbandPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreenContent(
    historyEntries: List<TranslationHistoryEntity>,
    isRunning: Boolean,
    tr: (String) -> String,
    onReopenEntry: (TranslationHistoryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearAll: () -> Unit,
    onOpenHelp: (String) -> Unit,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    statusFilter: String = "ALL",
    onStatusFilterChange: (String) -> Unit = {},
    dailyStats: List<DailyProductivityStat> = emptyList(),
    totalDoneCount: Int = historyEntries.count { it.status == "DONE" },
    totalFailedCount: Int = historyEntries.count { it.status != "DONE" }
) {
  val pal = LocalWarbandPalette.current
  val dateFormat = remember { SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault()) }

  LazyColumn(
      modifier = Modifier
          .fillMaxSize()
          .testTag("history_screen_content"),
      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      MedievalCard(modifier = Modifier.widthIn(max = 720.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = tr("secHistory"),
                tint = pal.primaryAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tr("secHistory").uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = pal.primaryAccent,
                modifier = Modifier.weight(1f, fill = false)
            )
            HelpIconButton(
                topicKey = "secHistory",
                contentDescription = tr("secHistory"),
                onOpenHelp = onOpenHelp
            )
          }

          if (historyEntries.isNotEmpty() || searchQuery.isNotEmpty() || statusFilter != "ALL") {
            OutlinedButton(
                onClick = onClearAll,
                border = BorderStroke(1.dp, pal.errorBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = pal.errorBg,
                    contentColor = pal.errorText
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("clear_history_button")
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(tr("btnClearHistory"), fontSize = 11.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tr("historyDesc"),
            style = MaterialTheme.typography.bodySmall,
            color = pal.textSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null, tint = pal.primaryAccent, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                  Icon(Icons.Default.Close, contentDescription = tr("btnCancel"), tint = pal.textSecondary, modifier = Modifier.size(16.dp))
                }
              }
            },
            placeholder = {
              Text(
                  text = tr("historySearchPlaceholder"),
                  style = MaterialTheme.typography.bodySmall,
                  color = pal.textSecondary.copy(alpha = 0.6f)
              )
            },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = pal.textPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = pal.primaryAccent,
                unfocusedBorderColor = pal.border,
                focusedContainerColor = pal.inputBg,
                unfocusedContainerColor = pal.inputBg,
                cursorColor = pal.primaryAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status filter chips
        val filterOptions = listOf(
            "ALL" to tr("histFilterAll"),
            "DONE" to tr("histFilterDone"),
            "STOPPED" to tr("histFilterStopped"),
            "ERROR" to tr("histFilterError")
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          filterOptions.forEach { (code, label) ->
            val selected = statusFilter == code
            Surface(
                shape = CircleShape,
                color = if (selected) pal.actionBg.copy(alpha = 0.7f) else pal.deepSurface,
                border = BorderStroke(1.dp, if (selected) pal.primaryAccent else pal.border),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { onStatusFilterChange(code) }
                    .testTag("history_filter_$code")
            ) {
              Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (selected) pal.primaryAccent else pal.textSecondary,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }

    if (dailyStats.isNotEmpty() && (totalDoneCount + totalFailedCount) > 0) {
      item {
        Box(modifier = Modifier.widthIn(max = 720.dp)) {
          ProductivityChartsCard(
              dailyStats = dailyStats,
              totalDone = totalDoneCount,
              totalFailed = totalFailedCount,
              tr = tr
          )
        }
      }
    }

    if (historyEntries.isEmpty()) {
      item {
        MedievalCard(modifier = Modifier.widthIn(max = 720.dp)) {
          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(pal.actionBg.copy(alpha = 0.3f))
                    .border(1.dp, pal.primaryMuted, CircleShape),
                contentAlignment = Alignment.Center
            ) {
              Icon(
                  imageVector = Icons.Default.History,
                  contentDescription = null,
                  tint = pal.primaryAccent,
                  modifier = Modifier.size(28.dp)
              )
            }
            Text(
                text = tr("historyEmptyTitle"),
                style = MaterialTheme.typography.titleSmall,
                color = pal.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = tr("historyEmptySub"),
                style = MaterialTheme.typography.bodySmall,
                color = pal.textSecondary,
                textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(historyEntries, key = { it.id }) { entry ->
        val (badgeBg, badgeBorder, badgeText, statusLabel) = when (entry.status) {
          "DONE" -> HistoryStatusStyle(pal.successBg, pal.successBorder, pal.successText, tr("histStatusDone"))
          "STOPPED" -> HistoryStatusStyle(pal.warningBg, pal.primaryMuted, pal.warningText, tr("histStatusStopped"))
          else -> HistoryStatusStyle(pal.errorBg, pal.errorBorder, pal.errorText, tr("histStatusError"))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(pal.cardBottom)
                .border(1.dp, pal.border, RoundedCornerShape(14.dp))
                .padding(14.dp)
                .testTag("history_item_${entry.id}"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                  text = entry.fileName,
                  style = MaterialTheme.typography.titleSmall,
                  color = pal.textPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
              )
              Text(
                  text = dateFormat.format(Date(entry.timestamp)),
                  style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                  color = pal.textSecondary
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeBg,
                border = BorderStroke(1.dp, badgeBorder)
            ) {
              Text(
                  text = statusLabel,
                  color = badgeText,
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          HorizontalDivider(color = pal.borderSoft, thickness = 0.8.dp)

          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
                text = "${entry.processedLines}/${entry.totalLines} ${tr("cacheUnit")} · ${entry.sourceLang}→${entry.targetLang} · ${entry.engineName}",
                style = MaterialTheme.typography.labelSmall,
                color = pal.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Button(
                  onClick = { onReopenEntry(entry) },
                  enabled = !isRunning,
                  colors = ButtonDefaults.buttonColors(
                      containerColor = pal.actionBg,
                      contentColor = pal.textPrimary
                  ),
                  border = BorderStroke(1.dp, pal.primaryMuted),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier
                      .minimumInteractiveComponentSize()
                      .testTag("reopen_history_${entry.id}")
              ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = pal.primaryAccent, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(tr("btnReopenFile"), fontSize = 12.sp)
              }

              IconButton(
                  onClick = { onDeleteEntry(entry.id) },
                  modifier = Modifier.testTag("delete_history_${entry.id}")
              ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = pal.textSecondary,
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

private data class HistoryStatusStyle(
    val bg: Color,
    val border: Color,
    val text: Color,
    val label: String
)
