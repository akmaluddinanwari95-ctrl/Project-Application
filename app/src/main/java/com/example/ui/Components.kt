package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ConnectionBadgeType
import com.example.domain.ConnectionStatus
import com.example.domain.ErrorAlert
import com.example.domain.HelpTopicContent
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
import com.example.ui.theme.MahoganyCardBottom
import com.example.ui.theme.MahoganyCardTop
import com.example.ui.theme.MahoganyInput
import com.example.ui.theme.MahoganyLight
import com.example.ui.theme.Obsidian
import com.example.ui.theme.Parchment
import com.example.ui.theme.ParchmentDim

@Composable
fun MedievalCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
  Column(
      modifier = modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(
              Brush.verticalGradient(
                  colors = listOf(MahoganyCardTop, MahoganyCardBottom)
              )
          )
          .border(1.dp, BurgundyBorder, RoundedCornerShape(14.dp))
          .padding(16.dp),
      content = content
  )
}

@Composable
fun HelpIconButton(
    topicKey: String,
    contentDescription: String,
    onOpenHelp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  Box(
      modifier = modifier
          .minimumInteractiveComponentSize()
          .clip(CircleShape)
          .clickable { onOpenHelp(topicKey) }
          .testTag("help_btn_$topicKey"),
      contentAlignment = Alignment.Center
  ) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color(0xFF2B1816))
            .border(1.dp, GoldMuted, CircleShape),
        contentAlignment = Alignment.Center
    ) {
      Text(
          text = "?",
          color = GoldAccent,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun ConnectionBadge(status: ConnectionStatus) {
  val (bg, border, textColor) = when (status.type) {
    ConnectionBadgeType.OK -> Triple(Color(0xFF0E2914), Color(0xFF2E7D32), ForestText)
    ConnectionBadgeType.WARN -> Triple(AmberBg, GoldMuted, AmberText)
    ConnectionBadgeType.ERROR -> Triple(Color(0xFF331015), Crimson, CrimsonText)
    ConnectionBadgeType.NEUTRAL -> Triple(Color(0xFF1F1312), BurgundyBorder, ParchmentDim)
  }

  Surface(
      shape = CircleShape,
      color = bg,
      border = BorderStroke(1.dp, border)
  ) {
    Text(
        text = status.text,
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun <T> MedievalDropdown(
    selectedLabel: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(MahoganyInput)
            .border(1.dp, BurgundyBorder, RoundedCornerShape(10.dp))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
          text = selectedLabel,
          color = Parchment,
          style = MaterialTheme.typography.bodySmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
      )
      Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = GoldAccent,
          modifier = Modifier.size(18.dp)
      )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MahoganyLight)
    ) {
      items.forEach { item ->
        DropdownMenuItem(
            text = {
              Text(
                  text = itemLabel(item),
                  color = Parchment,
                  style = MaterialTheme.typography.bodySmall
              )
            },
            onClick = {
              expanded = false
              onSelect(item)
            }
        )
      }
    }
  }
}

@Composable
fun MedievalAccordionCard(
    title: String,
    leadingEmoji: String,
    helpTopicKey: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenHelp: (String) -> Unit,
    headerTestTag: String,
    trailingBadge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
  val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrow")

  Column(
      modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(
              Brush.verticalGradient(colors = listOf(MahoganyCardTop, MahoganyCardBottom))
          )
          .border(1.dp, BurgundyBorder, RoundedCornerShape(14.dp))
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(headerTestTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = leadingEmoji, fontSize = 16.sp)
      Spacer(modifier = Modifier.width(6.dp))
      Text(
          text = title.uppercase(),
          style = MaterialTheme.typography.titleMedium,
          color = GoldAccent,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
      )
      if (trailingBadge != null) {
        Spacer(modifier = Modifier.width(6.dp))
        trailingBadge()
      }
      HelpIconButton(
          topicKey = helpTopicKey,
          contentDescription = title,
          onOpenHelp = onOpenHelp
      )
      Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = GoldAccent,
          modifier = Modifier
              .size(22.dp)
              .rotate(rotation)
      )
    }

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
      ) {
        HorizontalDivider(color = BurgundyBorderSoft, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))
        content()
      }
    }
  }
}

@Composable
fun HelpModalDialog(
    topic: HelpTopicContent?,
    closeButtonLabel: String,
    onDismiss: () -> Unit
) {
  if (topic == null) return

  AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = Color(0xFF1D1110),
      titleContentColor = GoldAccent,
      textContentColor = ParchmentDim,
      shape = RoundedCornerShape(18.dp),
      title = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
              modifier = Modifier
                  .size(26.dp)
                  .clip(CircleShape)
                  .background(Burgundy.copy(alpha = 0.6f))
                  .border(1.dp, GoldAccent, CircleShape),
              contentAlignment = Alignment.Center
          ) {
            Text(
                text = "?",
                color = GoldAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
          }
          Text(
              text = topic.title,
              style = MaterialTheme.typography.titleMedium,
              color = GoldAccent
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          topic.paragraphs.forEach { para ->
            Text(
                text = para,
                style = MaterialTheme.typography.bodyMedium,
                color = ParchmentDim
            )
          }
        }
      },
      confirmButton = {
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = Burgundy,
                contentColor = Parchment
            ),
            border = BorderStroke(1.dp, GoldMuted),
            modifier = Modifier.testTag("help_modal_close_button")
        ) {
          Text(text = closeButtonLabel)
        }
      }
  )
}

@Composable
fun SaveDestinationDialog(
    visible: Boolean,
    fileCount: Int,
    title: String,
    description: String,
    downloadsLabel: String,
    downloadsSub: String,
    folderLabel: String,
    folderSub: String,
    cancelLabel: String,
    onSaveToDownloads: () -> Unit,
    onPickFolder: () -> Unit,
    onDismiss: () -> Unit
) {
  if (!visible) return

  AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = Color(0xFF1D1110),
      shape = RoundedCornerShape(18.dp),
      title = {
        Text(
            text = "$title ($fileCount)",
            style = MaterialTheme.typography.titleMedium,
            color = GoldAccent
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
              text = description,
              style = MaterialTheme.typography.bodySmall,
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
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(
                  imageVector = Icons.Default.Download,
                  contentDescription = downloadsLabel,
                  tint = ForestText
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = downloadsLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = ForestText
                )
                Text(
                    text = downloadsSub,
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
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(
                  imageVector = Icons.Default.FolderOpen,
                  contentDescription = folderLabel,
                  tint = GoldAccent
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folderLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = Parchment
                )
                Text(
                    text = folderSub,
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentDim
                )
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        OutlinedButton(
            onClick = onDismiss,
            border = BorderStroke(1.dp, BurgundyBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentDim)
        ) {
          Text(cancelLabel)
        }
      }
  )
}

@Composable
fun ErrorAlertBanner(
    alert: ErrorAlert?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
  AnimatedVisibility(visible = alert != null, modifier = modifier) {
    if (alert != null) {
      Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF2E1115),
          border = BorderStroke(1.5.dp, Crimson),
          shadowElevation = 8.dp,
          modifier = Modifier.fillMaxWidth()
      ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = CrimsonText,
              modifier = Modifier.size(20.dp)
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.labelMedium,
                color = CrimsonText
            )
            if (alert.lineInfo.isNotEmpty()) {
              Text(
                  text = alert.lineInfo,
                  style = MaterialTheme.typography.labelSmall,
                  color = GoldAccent
              )
            }
            Text(
                text = alert.detail,
                style = MaterialTheme.typography.bodySmall,
                color = ParchmentDim
            )
          }
          IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(32.dp)
          ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = CrimsonText,
                modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
