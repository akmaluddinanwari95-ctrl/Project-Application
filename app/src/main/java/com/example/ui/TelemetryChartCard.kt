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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.TelemetrySample
import com.example.domain.TranslationProgressState
import com.example.ui.theme.LocalWarbandPalette
import java.util.Locale
import kotlin.math.max

@Composable
fun TelemetryChartCard(
    progress: TranslationProgressState,
    tr: (String) -> String,
    onOpenHelp: (String) -> Unit
) {
  if (!progress.isRunning && progress.telemetrySamples.isEmpty() && progress.totalLines == 0) {
    return
  }

  val pal = LocalWarbandPalette.current
  val samples = progress.telemetrySamples
  val speedColor = Color(0xFF4ADE80)
  val progressColor = pal.primaryAccent
  val curSpeedStr = String.format(Locale.US, "%.1f", progress.currentSpeedLinesPerSec)
  val peakSpeedStr = String.format(Locale.US, "%.1f", progress.peakSpeedLinesPerSec)

  MedievalCard(modifier = Modifier.testTag("realtime_telemetry_chart_card")) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(
            imageVector = Icons.Default.ShowChart,
            contentDescription = tr("secTelemetry"),
            tint = pal.primaryAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = tr("secTelemetry").uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = pal.primaryAccent,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        HelpIconButton(
            topicKey = "secTelemetry",
            contentDescription = tr("secTelemetry"),
            onOpenHelp = onOpenHelp
        )
      }

      Surface(
          shape = CircleShape,
          color = pal.successBg,
          border = BorderStroke(1.dp, pal.successBorder)
      ) {
        Text(
            text = "$curSpeedStr ${tr("kpiUnitRate")}",
            color = pal.successText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }

    Text(
        text = tr("telemetryHint"),
        style = MaterialTheme.typography.labelSmall,
        color = pal.textSecondary
    )

    HorizontalDivider(
        color = pal.borderSoft,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 10.dp)
    )

    // Legend row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
      LegendDot(color = speedColor, label = tr("legendSpeed"))
      LegendDot(color = progressColor, label = tr("legendProgress"))
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Real-time dual-series Canvas graph
    val maxSpeed = max(
        2f,
        (samples.maxOfOrNull { it.speedLinesPerSec } ?: 0f) * 1.2f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(pal.deepSurface)
            .border(1.dp, pal.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
      if (samples.size < 2) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
              text = tr("telemetryEmpty"),
              style = MaterialTheme.typography.bodySmall,
              color = pal.textSecondary.copy(alpha = 0.7f)
          )
        }
      } else {
        TelemetryCanvasGraph(
            samples = samples,
            maxSpeed = maxSpeed,
            speedColor = speedColor,
            progressColor = progressColor,
            gridColor = pal.primaryMuted.copy(alpha = 0.22f),
            modifier = Modifier.fillMaxSize()
        )
      }

      // Axis scale labels in corners
      Text(
          text = "100%",
          style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
          color = progressColor.copy(alpha = 0.75f),
          modifier = Modifier.align(Alignment.TopEnd)
      )
      Text(
          text = String.format(Locale.US, "%.1f/s", maxSpeed),
          style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
          color = speedColor.copy(alpha = 0.75f),
          modifier = Modifier.align(Alignment.TopStart)
      )
      Text(
          text = "0",
          style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
          color = pal.textSecondary.copy(alpha = 0.55f),
          modifier = Modifier.align(Alignment.BottomStart)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // KPI summary row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      TelemetryKpiChip(
          label = tr("kpiCurSpeed"),
          value = "$curSpeedStr ${tr("kpiUnitRate")}",
          valueColor = speedColor,
          modifier = Modifier.weight(1f)
      )
      TelemetryKpiChip(
          label = tr("kpiPeakSpeed"),
          value = "$peakSpeedStr ${tr("kpiUnitRate")}",
          valueColor = pal.textPrimary,
          modifier = Modifier.weight(1f)
      )
      TelemetryKpiChip(
          label = tr("legendProgress"),
          value = "${progress.percentage}%",
          valueColor = pal.primaryAccent,
          modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  val pal = LocalWarbandPalette.current
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    Box(
        modifier = Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(color)
    )
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = pal.textSecondary
    )
  }
}

@Composable
private fun TelemetryKpiChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
  val pal = LocalWarbandPalette.current
  Column(
      modifier = modifier
          .clip(RoundedCornerShape(8.dp))
          .background(pal.deepSurface)
          .border(1.dp, pal.borderSoft, RoundedCornerShape(8.dp))
          .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = pal.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = value,
        style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        ),
        color = valueColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun TelemetryCanvasGraph(
    samples: List<TelemetrySample>,
    maxSpeed: Float,
    speedColor: Color,
    progressColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f || samples.size < 2) return@Canvas

    val padTop = 10f
    val padBottom = 10f
    val usableH = (h - padTop - padBottom).coerceAtLeast(1f)
    val stepX = w / (samples.size - 1).coerceAtLeast(1)

    // Horizontal dashed gridlines at 25%, 50%, 75%
    val gridEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    listOf(0.25f, 0.50f, 0.75f).forEach { ratio ->
      val y = padTop + usableH * (1f - ratio)
      drawLine(
          color = gridColor,
          start = Offset(0f, y),
          end = Offset(w, y),
          strokeWidth = 1.dp.toPx(),
          pathEffect = gridEffect
      )
    }

    val progressPath = Path()
    val progressFill = Path()
    val speedPath = Path()
    val speedFill = Path()

    var lastProgressPt = Offset.Zero
    var lastSpeedPt = Offset.Zero

    samples.forEachIndexed { idx, sample ->
      val x = idx * stepX
      val normProg = (sample.progressPercent / 100f).coerceIn(0f, 1f)
      val normSpeed = (sample.speedLinesPerSec / maxSpeed).coerceIn(0f, 1f)

      val yProg = padTop + usableH * (1f - normProg)
      val ySpeed = padTop + usableH * (1f - normSpeed)

      if (idx == 0) {
        progressPath.moveTo(x, yProg)
        progressFill.moveTo(x, padTop + usableH)
        progressFill.lineTo(x, yProg)

        speedPath.moveTo(x, ySpeed)
        speedFill.moveTo(x, padTop + usableH)
        speedFill.lineTo(x, ySpeed)
      } else {
        val prevX = (idx - 1) * stepX
        val midX = (prevX + x) / 2f

        progressPath.cubicTo(midX, lastProgressPt.y, midX, yProg, x, yProg)
        progressFill.cubicTo(midX, lastProgressPt.y, midX, yProg, x, yProg)

        speedPath.cubicTo(midX, lastSpeedPt.y, midX, ySpeed, x, ySpeed)
        speedFill.cubicTo(midX, lastSpeedPt.y, midX, ySpeed, x, ySpeed)
      }

      lastProgressPt = Offset(x, yProg)
      lastSpeedPt = Offset(x, ySpeed)
    }

    progressFill.lineTo(lastProgressPt.x, padTop + usableH)
    progressFill.close()
    speedFill.lineTo(lastSpeedPt.x, padTop + usableH)
    speedFill.close()

    drawPath(
        path = progressFill,
        brush = Brush.verticalGradient(
            colors = listOf(progressColor.copy(alpha = 0.22f), Color.Transparent),
            startY = padTop,
            endY = padTop + usableH
        )
    )
    drawPath(
        path = speedFill,
        brush = Brush.verticalGradient(
            colors = listOf(speedColor.copy(alpha = 0.20f), Color.Transparent),
            startY = padTop,
            endY = padTop + usableH
        )
    )

    drawPath(
        path = progressPath,
        color = progressColor,
        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = speedPath,
        color = speedColor,
        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Latest point indicators
    drawCircle(color = progressColor.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = lastProgressPt)
    drawCircle(color = progressColor, radius = 3.2.dp.toPx(), center = lastProgressPt)

    drawCircle(color = speedColor.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = lastSpeedPt)
    drawCircle(color = speedColor, radius = 3.2.dp.toPx(), center = lastSpeedPt)
  }
}
