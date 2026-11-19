package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TranslationHistoryEntity
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
import com.example.ui.theme.Parchment
import com.example.ui.theme.ParchmentDim
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreenContent(
    historyEntries: List<TranslationHistoryEntity>,
    isRunning: Boolean,
    tr: (String) -> String,
    onReopenEntry: (TranslationHistoryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearAll: () -> Unit,
    onOpenHelp: (String) -> Unit
) {
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
                tint = GoldAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tr("secHistory").uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent,
                modifier = Modifier.weight(1f, fill = false)
            )
            HelpIconButton(
                topicKey = "secHistory",
                contentDescription = tr("secHistory"),
                onOpenHelp = onOpenHelp
            )
          }

          if (historyEntries.isNotEmpty()) {
            OutlinedButton(
                onClick = onClearAll,
                border = BorderStroke(1.dp, Crimson),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Crimson.copy(alpha = 0.25f),
                    contentColor = CrimsonText
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
            color = ParchmentDim
        )
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
                    .background(Burgundy.copy(alpha = 0.3f))
                    .border(1.dp, GoldMuted, CircleShape),
                contentAlignment = Alignment.Center
            ) {
              Icon(
                  imageVector = Icons.Default.History,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(28.dp)
              )
            }
            Text(
                text = tr("historyEmptyTitle"),
                style = MaterialTheme.typography.titleSmall,
                color = Parchment,
                textAlign = TextAlign.Center
            )
            Text(
                text = tr("historyEmptySub"),
                style = MaterialTheme.typography.bodySmall,
                color = ParchmentDim,
                textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(historyEntries, key = { it.id }) { entry ->
        val (badgeBg, badgeBorder, badgeText, statusLabel) = when (entry.status) {
          "DONE" -> HistoryStatusStyle(Color(0xFF0E2914), Forest, ForestText, tr("histStatusDone"))
          "STOPPED" -> HistoryStatusStyle(AmberBg, GoldMuted, AmberText, tr("histStatusStopped"))
          else -> HistoryStatusStyle(Color(0xFF331015), Crimson, CrimsonText, tr("histStatusError"))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1A100F))
                .border(1.dp, BurgundyBorder, RoundedCornerShape(14.dp))
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
                  color = Parchment,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
              )
              Text(
                  text = dateFormat.format(Date(entry.timestamp)),
                  style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                  color = ParchmentDim
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

          HorizontalDivider(color = BurgundyBorderSoft, thickness = 0.8.dp)

          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
                text = "${entry.processedLines}/${entry.totalLines} ${tr("cacheUnit")} · ${entry.sourceLang}→${entry.targetLang} · ${entry.engineName}",
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentDim,
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
                      containerColor = Burgundy,
                      contentColor = Parchment
                  ),
                  border = BorderStroke(1.dp, GoldMuted),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier
                      .minimumInteractiveComponentSize()
                      .testTag("reopen_history_${entry.id}")
              ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
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

private data class HistoryStatusStyle(
    val bg: Color,
    val border: Color,
    val text: Color,
    val label: String
)
