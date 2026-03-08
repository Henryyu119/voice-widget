package com.openclaw.voicewidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordingService : Service() {

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "recording_channel"
        
        fun startRecording(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = "START_RECORDING"
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopRecording(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = "STOP_RECORDING"
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_RECORDING" -> startRecordingInternal()
            "STOP_RECORDING" -> stopRecordingInternal()
        }
        return START_NOT_STICKY
    }

    private fun startRecordingInternal() {
        try {
            // 创建音频文件
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            audioFile = File(cacheDir, "voice_$timestamp.m4a")
            
            // 初始化 MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            
            startTime = System.currentTimeMillis()
            
            // 更新状态
            updatePrefs(true)
            startForeground(NOTIFICATION_ID, createNotification("录音中..."))
            
            // 开始更新时长
            startDurationUpdates()
            
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun stopRecordingInternal() {
        try {
            // 停止录音
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            // 停止时长更新
            stopDurationUpdates()
            
            // 更新状态
            updatePrefs(false)
            updateWidget(false)
            
            // 发送到飞书
            audioFile?.let { file ->
                sendToFeishu(file)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stopForeground(true)
            stopSelf()
        }
    }

    private fun startDurationUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                val duration = (System.currentTimeMillis() - startTime) / 1000
                val durationText = String.format("%02d:%02d", duration / 60, duration % 60)
                updateWidget(true, durationText)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun stopDurationUpdates() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    private fun sendToFeishu(audioFile: File) {
        // 显示发送中状态
        val notification = createNotification("发送中...")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val feishuClient = FeishuClient(this@RecordingService)
                val success = feishuClient.sendVoiceMessage(audioFile)
                
                val message = if (success) "发送成功" else "发送失败"
                val finalNotification = createNotification(message)
                notificationManager.notify(NOTIFICATION_ID, finalNotification)
                
                // 2秒后关闭通知
                handler.postDelayed({
                    notificationManager.cancel(NOTIFICATION_ID)
                    audioFile.delete()
                }, 2000)
                
            } catch (e: Exception) {
                e.printStackTrace()
                val errorNotification = createNotification("发送失败: ${e.message}")
                notificationManager.notify(NOTIFICATION_ID, errorNotification)
            }
        }
    }

    private fun updatePrefs(isRecording: Boolean) {
        getSharedPreferences(VoiceWidget.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(VoiceWidget.PREF_IS_RECORDING, isRecording)
            .apply()
    }

    private fun updateWidget(isRecording: Boolean, duration: String = "") {
        VoiceWidget().updateWidgetState(this, isRecording, duration)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "录音服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "录音服务通知"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("快速录音")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
