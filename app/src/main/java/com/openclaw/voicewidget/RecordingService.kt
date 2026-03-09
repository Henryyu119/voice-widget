package com.openclaw.voicewidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class RecordingService : Service() {

    companion object {
        const val ACTION_START = "com.openclaw.voicewidget.action.START"
        const val ACTION_STOP = "com.openclaw.voicewidget.action.STOP"
        const val ACTION_STATUS_CHANGED = "com.openclaw.voicewidget.action.STATUS_CHANGED"

        const val EXTRA_IS_RECORDING = "extra_is_recording"
        const val EXTRA_STATUS_TEXT = "extra_status_text"

        private const val CHANNEL_ID = "voice_widget_recording"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        var isRecordingNow: Boolean = false
            private set
    }

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (isRecordingNow) return

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("正在录音..."))

        try {
            audioFile = File(externalCacheDir, "voice_${System.currentTimeMillis()}.m4a")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            isRecordingNow = true
            broadcastStatus(true, "录音中，点击停止")
        } catch (e: Exception) {
            isRecordingNow = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            broadcastStatus(false, "录音失败: ${e.message}")
        }
    }

    private fun stopRecording() {
        if (!isRecordingNow) {
            stopSelf()
            return
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
        } finally {
            mediaRecorder = null
            isRecordingNow = false
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        broadcastStatus(false, "录音结束，准备上传")

        val file = audioFile
        Thread {
            val ok = file?.let { ServerClient().uploadVoice(it) } ?: false
            val status = if (ok) "上传成功" else "上传失败"
            broadcastStatus(false, status)
        }.start()
        stopSelf()
    }

    private fun broadcastStatus(isRecording: Boolean, status: String) {
        sendBroadcast(Intent(ACTION_STATUS_CHANGED).apply {
            putExtra(EXTRA_IS_RECORDING, isRecording)
            putExtra(EXTRA_STATUS_TEXT, status)
        })
        VoiceWidget.updateAllWidgets(this, isRecording, status)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "快速录音",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val stopIntent = Intent(this, RecordingService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("快速录音")
            .setContentText(text)
            .setOngoing(true)
            .addAction(0, "停止", stopPendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
