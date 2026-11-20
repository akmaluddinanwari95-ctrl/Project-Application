package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DailyProductivityStat
import com.example.domain.SessionSummaryReport
import com.example.ui.theme.LocalWarbandPalette
import kotlin.math.max

@Composable
fun SessionSummaryDashboardCard(
    summary: SessionSummaryReport,
    dailyStats: List<DailyProductivityStat>,
    tr: (String) -> String,
    onDismiss: () -> Unit,
    onOpenHelp: (String) -> Unit
) {
  val pal = LocalWarbandPalette.current
  val ratioPct = if (summary.originalLines > 0) {
    ((summary.translatedLines.toFloat() / summary.originalLines) * 100).toInt().coerceIn(0, 100)
  } else {
    0
  }
  val durationText = formatDurationMs(summary.totalDurationMs)

  MedievalCard(modifier = Modifier.testTag("session_summary_dashboard_card")) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = tr("secSummary"),
            tint = pal.primaryAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = tr("secSummary").uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = pal.primaryAccent,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        HelpIconButton(
            topicKey = "secSummary",
            contentDescription = tr("secSummary"),
            onOpenHelp = onOpenHelp
        )
      }

      IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("dismiss_summary_button")
      ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = tr("btnCancel"),
            tint = pal.textSecondary,
            modifier = Modifier.size(18.dp)
        )
      }
    }

    Text(
        text = tr("summarySub"),
        style = MaterialTheme.typography.bodySmall,
        color = pal.textSecondary
    )

    HorizontalDivider(
        color = pal.borderSoft,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 10.dp)
    )

    // 4 KPI Cards
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SummaryMetricTile(
          label = tr("summaryTransVsOrig"),
          value = "${summary.translatedLines} / ${summary.originalLines} ($ratioPct%)",
          accent = pal.successText,
          modifier = Modifier
              .weight(1f)
              .testTag("summary_lines_ratio_tile")
      )
      SummaryMetricTile(
          label = tr("summaryErrors"),
          value = "${summary.errorsEncountered}",
          accent = if (summary.errorsEncountered > 0) pal.errorText else pal.successText,
          modifier = Modifier
              .weight(1f)
              .testTag("summary_errors_tile")
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SummaryMetricTile(
          label = tr("summaryDuration"),
          value = durationText,
          accent = pal.primaryAccent,
          modifier = Modifier
              .weight(1f)
              .testTag("summary_duration_tile")
      )
      SummaryMetricTile(
          label = tr("summaryWords"),
          value = "${summary.wordsProcessed}",
          accent = pal.textPrimary,
          modifier = Modifier
              .weight(1f)
              .testTag("summary_words_tile")
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Embedded Productivity Chart (Words/day + Success vs Failed files)
    ProductivityChartsContent(
        dailyStats = dailyStats,
        totalDoneOverride = summary.filesSucceeded,
        totalFailedOverride = summary.filesFailed,
        tr = tr
    )
  }
}

@Composable
fun ProductivityChartsCard(
    dailyStats: List<DailyProductivityStat>,
    totalDone: Int,
    totalFailed: Int,
    tr: (String) -> String
) {
  val pal = LocalWarbandPalette.current
  MedievalCard(modifier = Modifier.testTag("productivity_charts_card")) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
          imageVector = Icons.Default.Assessment,
          contentDescription = tr("secProductivity"),
          tint = pal.primaryAccent,
          modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
          text = tr("secProductivity").uppercase(),
          style = MaterialTheme.typography.titleMedium,
          color = pal.primaryAccent
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProductivityChartsContent(
        dailyStats = dailyStats,
        totalDoneOverride = totalDone,
        totalFailedOverride = totalFailed,
        tr = tr
    )
  }
}

@Composable
private fun ProductivityChartsContent(
    dailyStats: List<DailyProductivityStat>,
    totalDoneOverride: Int,
    totalFailedOverride: Int,
    tr: (String) -> String
) {
  val pal = LocalWarbandPalette.current
  val maxWords = max(10, dailyStats.maxOfOrNull { it.wordsCount } ?: 10)
  val totalFiles = (totalDoneOverride + totalFailedOverride).coerceAtLeast(0)
  val successRatio = if (totalFiles > 0) totalDoneOverride.toFloat() / totalFiles else 1f

  Column(
      modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(pal.deepSurface)
          .border(1.dp, pal.borderSoft, RoundedCornerShape(12.dp))
          .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
        text = tr("prodWordsPerDay"),
        style = MaterialTheme.typography.labelMedium,
        color = pal.primaryAccent
    )

    if (dailyStats.isEmpty()) {
      Text(
          text = "-",
          style = MaterialTheme.typography.bodySmall,
          color = pal.textSecondary
      )
    } else {
      val barColor = pal.primaryAccent
      val gridColor = pal.borderSoft
      Canvas(
          modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
              .testTag("productivity_words_bar_canvas")
      ) {
        val w = size.width
        val h = size.height
        val padBottom = 8f
        val usableH = (h - padBottom - 8f).coerceAtLeast(1f)

        // Gridline at 50%
        drawLine(
            color = gridColor,
            start = Offset(0f, usableH * 0.5f),
            end = Offset(w, usableH * 0.5f),
            strokeWidth = 1.dp.toPx()
        )

        val count = dailyStats.size
        val slotW = w / count.coerceAtLeast(1)
        val barW = (slotW * 0.54f).coerceIn(10f, 44f)

        dailyStats.forEachIndexed { idx, stat ->
          val ratio = (stat.wordsCount.toFloat() / maxWords.toFloat()).coerceIn(0f, 1f)
          val barH = max(4f, usableH * ratio)
          val x = idx * slotW + (slotW - barW) / 2f
          val y = usableH - barH
          drawRoundRect(
              color = if (stat.wordsCount > 0) barColor else gridColor.copy(alpha = 0.5f),
              topLeft = Offset(x, y),
              size = Size(barW, barH),
              cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
          )
        }
      }

      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
      ) {
        dailyStats.forEach { stat ->
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(
                text = if (stat.wordsCount >= 1000) "${stat.wordsCount / 1000}k" else "${stat.wordsCount}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                ),
                color = if (stat.wordsCount > 0) pal.primaryAccent else pal.textSecondary.copy(alpha = 0.6f)
            )
            Text(
                text = stat.dayLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = pal.textSecondary
            )
          }
        }
      }
    }

    HorizontalDivider(color = pal.borderSoft, thickness = 0.8.dp)

    // Successful vs Failed file donut + chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      val okColor = pal.successText
      val failColor = pal.errorText
      val trackColor = pal.borderSoft
      Box(
          modifier = Modifier.size(56.dp),
          contentAlignment = Alignment.Center
      ) {
        Canvas(
            modifier = Modifier
                .size(52.dp)
                .testTag("productivity_donut_canvas")
        ) {
          val strokeW = 7.dp.toPx()
          if (totalFiles == 0) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
          } else {
            val okSweep = 360f * successRatio
            val failSweep = 360f - okSweep
            drawArc(
                color = okColor,
                startAngle = -90f,
                sweepAngle = okSweep,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Butt)
            )
            if (failSweep > 0.5f) {
              drawArc(
                  color = failColor,
                  startAngle = -90f + okSweep,
                  sweepAngle = failSweep,
                  useCenter = false,
                  style = Stroke(width = strokeW, cap = StrokeCap.Butt)
              )
            }
          }
        }
        Text(
            text = "${(successRatio * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = pal.textPrimary
        )
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = tr("prodPassVsFail"),
            style = MaterialTheme.typography.labelMedium,
            color = pal.textPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(
              shape = CircleShape,
              color = pal.successBg,
              border = BorderStroke(1.dp, pal.successBorder)
          ) {
            Text(
                text = "${tr("prodSucceeded")}: $totalDoneOverride",
                color = pal.successText,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
          Surface(
              shape = CircleShape,
              color = pal.errorBg,
              border = BorderStroke(1.dp, pal.errorBorder)
          ) {
            Text(
                text = "${tr("prodFailed")}: $totalFailedOverride",
                color = pal.errorText,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryMetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
  val pal = LocalWarbandPalette.current
  Column(
      modifier = modifier
          .clip(RoundedCornerShape(10.dp))
          .background(pal.deepSurface)
          .border(1.dp, pal.borderSoft, RoundedCornerShape(10.dp))
          .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = pal.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = value,
        style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        ),
        color = accent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
  }
}

private fun formatDurationMs(durationMs: Long): String {
  val totalSec = (durationMs / 1000L).coerceAtLeast(0L)
  val mins = totalSec / 60L
  val secs = totalSec % 60L
  return if (mins > 0L) {
    "${mins}m ${secs}s"
  } else {
    val tenths = ((durationMs % 1000L) / 100L).coerceAtLeast(0L)
    "${secs}.${tenths}s"
  }
}
