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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.LocalWarbandPalette

@Composable
fun MedievalCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
  val pal = LocalWarbandPalette.current
  Column(
      modifier = modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(
              Brush.verticalGradient(
                  colors = listOf(pal.cardTop, pal.cardBottom)
              )
          )
          .border(1.dp, pal.border, RoundedCornerShape(14.dp))
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
  val pal = LocalWarbandPalette.current
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
            .background(pal.cardElevated)
            .border(1.dp, pal.primaryMuted, CircleShape),
        contentAlignment = Alignment.Center
    ) {
      Text(
          text = "?",
          color = pal.primaryAccent,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun ConnectionBadge(status: ConnectionStatus) {
  val pal = LocalWarbandPalette.current
  val (bg, border, textColor) = when (status.type) {
    ConnectionBadgeType.OK -> Triple(pal.successBg, pal.successBorder, pal.successText)
    ConnectionBadgeType.WARN -> Triple(pal.warningBg, pal.primaryMuted, pal.warningText)
    ConnectionBadgeType.ERROR -> Triple(pal.errorBg, pal.errorBorder, pal.errorText)
    ConnectionBadgeType.NEUTRAL -> Triple(pal.cardBottom, pal.border, pal.textSecondary)
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
  val pal = LocalWarbandPalette.current
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(pal.inputBg)
            .border(1.dp, pal.border, RoundedCornerShape(10.dp))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
          text = selectedLabel,
          color = pal.textPrimary,
          style = MaterialTheme.typography.bodySmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
      )
      Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = pal.primaryAccent,
          modifier = Modifier.size(18.dp)
      )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(pal.cardElevated)
    ) {
      items.forEach { item ->
        DropdownMenuItem(
            text = {
              Text(
                  text = itemLabel(item),
                  color = pal.textPrimary,
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
  val pal = LocalWarbandPalette.current
  val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrow")

  Column(
      modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(
              Brush.verticalGradient(colors = listOf(pal.cardTop, pal.cardBottom))
          )
          .border(1.dp, pal.border, RoundedCornerShape(14.dp))
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
          color = pal.primaryAccent,
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
          tint = pal.primaryAccent,
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
        HorizontalDivider(color = pal.borderSoft, thickness = 1.dp)
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
  val pal = LocalWarbandPalette.current

  AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = pal.cardTop,
      titleContentColor = pal.primaryAccent,
      textContentColor = pal.textSecondary,
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
                  .background(pal.actionBg.copy(alpha = 0.6f))
                  .border(1.dp, pal.primaryAccent, CircleShape),
              contentAlignment = Alignment.Center
          ) {
            Text(
                text = "?",
                color = pal.primaryAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
          }
          Text(
              text = topic.title,
              style = MaterialTheme.typography.titleMedium,
              color = pal.primaryAccent
          )
        }
      },
      text = {
        Column(
            modifier = Modifier
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          topic.paragraphs.forEach { para ->
            Text(
                text = para,
                style = MaterialTheme.typography.bodyMedium,
                color = pal.textSecondary
            )
          }
        }
      },
      confirmButton = {
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = pal.actionBg,
                contentColor = pal.textPrimary
            ),
            border = BorderStroke(1.dp, pal.primaryMuted),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("help_modal_close_button")
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
  val pal = LocalWarbandPalette.current

  AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = pal.cardTop,
      shape = RoundedCornerShape(18.dp),
      title = {
        Text(
            text = "$title ($fileCount)",
            style = MaterialTheme.typography.titleMedium,
            color = pal.primaryAccent
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
              text = description,
              style = MaterialTheme.typography.bodySmall,
              color = pal.textSecondary
          )

          Surface(
              onClick = onSaveToDownloads,
              shape = RoundedCornerShape(12.dp),
              color = pal.successBg,
              border = BorderStroke(1.dp, pal.successBorder),
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
                  tint = pal.successText
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = downloadsLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = pal.successText
                )
                Text(
                    text = downloadsSub,
                    style = MaterialTheme.typography.labelSmall,
                    color = pal.textSecondary
                )
              }
            }
          }

          Surface(
              onClick = onPickFolder,
              shape = RoundedCornerShape(12.dp),
              color = pal.actionBg.copy(alpha = 0.35f),
              border = BorderStroke(1.dp, pal.primaryMuted),
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
                  tint = pal.primaryAccent
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folderLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = pal.textPrimary
                )
                Text(
                    text = folderSub,
                    style = MaterialTheme.typography.labelSmall,
                    color = pal.textSecondary
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
            border = BorderStroke(1.dp, pal.border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = pal.textSecondary)
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
    modifier: Modifier = Modifier,
    resumeLabel: String = "Lanjutkan",
    retryLabel: String = "Coba Lagi",
    onResume: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null
) {
  val pal = LocalWarbandPalette.current
  AnimatedVisibility(visible = alert != null, modifier = modifier) {
    if (alert != null) {
      Surface(
          shape = RoundedCornerShape(12.dp),
          color = pal.errorBg,
          border = BorderStroke(1.5.dp, pal.errorBorder),
          shadowElevation = 8.dp,
          modifier = Modifier
              .fillMaxWidth()
              .testTag("error_alert_banner")
      ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = pal.errorText,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                if (alert.codeLabel.isNotBlank()) {
                  Surface(
                      shape = RoundedCornerShape(5.dp),
                      color = pal.warningBg,
                      border = BorderStroke(1.dp, pal.errorBorder)
                  ) {
                    Text(
                        text = alert.codeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = pal.warningText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = pal.errorText,
                    modifier = Modifier.weight(1f, fill = false)
                )
              }
              if (alert.lineInfo.isNotEmpty()) {
                Text(
                    text = alert.lineInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = pal.primaryAccent
                )
              }
              Text(
                  text = alert.detail,
                  style = MaterialTheme.typography.bodySmall,
                  color = pal.textSecondary
              )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("error_alert_dismiss_button")
            ) {
              Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = pal.errorText,
                  modifier = Modifier.size(18.dp)
              )
            }
          }

          if ((alert.canResume && onResume != null) || (alert.canRetry && onRetry != null)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (alert.canResume && onResume != null) {
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pal.actionBg,
                        contentColor = pal.textPrimary
                    ),
                    border = BorderStroke(1.dp, pal.primaryMuted),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("error_alert_resume_button")
                ) {
                  Text(text = resumeLabel, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
              }
              if (alert.canRetry && onRetry != null) {
                OutlinedButton(
                    onClick = onRetry,
                    border = BorderStroke(1.dp, pal.primaryAccent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = pal.warningBg,
                        contentColor = pal.primaryAccent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("error_alert_retry_button")
                ) {
                  Text(text = retryLabel, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
              }
            }
          }
        }
      }
    }
  }
}
