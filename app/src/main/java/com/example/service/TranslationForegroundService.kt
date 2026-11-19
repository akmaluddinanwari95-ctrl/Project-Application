package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.SettingsDataStore
import com.example.domain.TranslationProgressState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TranslationForegroundService : Service() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var translationJob: Job? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_TOGGLE_PAUSE -> {
        TranslationCoordinator.togglePause()
        updateNotification(TranslationCoordinator.progressState.value)
      }
      ACTION_STOP -> {
        TranslationCoordinator.requestStop()
      }
      ACTION_START, null -> {
        if (translationJob?.isActive == true) {
          return START_NOT_STICKY
        }
        val initialNotif = buildNotification(TranslationCoordinator.progressState.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          startForeground(
              NOTIFICATION_ID,
              initialNotif,
              ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
          )
        } else {
          startForeground(NOTIFICATION_ID, initialNotif)
        }

        translationJob = serviceScope.launch {
          val settings = SettingsDataStore(applicationContext).settingsFlow.first()
          TranslationCoordinator.runBatchTranslation(
              context = applicationContext,
              settings = settings,
              onProgressUpdate = { progress ->
                updateNotification(progress)
              }
          )
          stopForeground(STOP_FOREGROUND_REMOVE)
          stopSelf()
        }
      }
    }
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    serviceScope.cancel()
    super.onDestroy()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = getSystemService(NotificationManager::class.java)
      val channel = NotificationChannel(
          CHANNEL_ID,
          getString(R.string.notif_channel_name),
          NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = getString(R.string.notif_channel_desc)
        setShowBadge(false)
      }
      manager.createNotificationChannel(channel)
    }
  }

  private fun updateNotification(progress: TranslationProgressState) {
    val manager = getSystemService(NotificationManager::class.java)
    manager.notify(NOTIFICATION_ID, buildNotification(progress))
  }

  private fun buildNotification(progress: TranslationProgressState): Notification {
    val openAppIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
        this,
        0,
        openAppIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val pauseIntent = Intent(this, TranslationForegroundService::class.java).apply {
      action = ACTION_TOGGLE_PAUSE
    }
    val pausePendingIntent = PendingIntent.getService(
        this,
        1,
        pauseIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, TranslationForegroundService::class.java).apply {
      action = ACTION_STOP
    }
    val stopPendingIntent = PendingIntent.getService(
        this,
        2,
        stopIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = if (progress.currentFileName.isNotEmpty()) {
      "[${progress.currentFileIndex}/${progress.totalPendingFiles}] ${progress.currentFileName}"
    } else {
      getString(R.string.app_name)
    }

    val statusText = if (progress.isPaused) {
      "Dijeda · ${progress.percentage}% (${progress.processedLines}/${progress.totalLines})"
    } else if (progress.totalLines > 0) {
      "${progress.percentage}% (${progress.processedLines}/${progress.totalLines})" +
          if (progress.etaText.isNotEmpty()) " · ${progress.etaText}" else ""
    } else {
      "Mempersiapkan terjemahan..."
    }

    val pauseLabel = if (progress.isPaused) "Lanjutkan" else "Jeda"

    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(statusText)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(contentPendingIntent)
        .setProgress(
            progress.totalLines.coerceAtLeast(1),
            progress.processedLines.coerceAtLeast(0),
            progress.totalLines == 0
        )
        .addAction(0, pauseLabel, pausePendingIntent)
        .addAction(0, "Berhenti", stopPendingIntent)
        .build()
  }

  companion object {
    const val CHANNEL_ID = "warband_translation_progress"
    const val NOTIFICATION_ID = 4102
    const val ACTION_START = "com.example.action.START_TRANSLATION"
    const val ACTION_TOGGLE_PAUSE = "com.example.action.TOGGLE_PAUSE"
    const val ACTION_STOP = "com.example.action.STOP_TRANSLATION"

    fun start(context: Context) {
      val intent = Intent(context, TranslationForegroundService::class.java).apply {
        action = ACTION_START
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }
  }
}
