package com.openclaw.voicewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class VoiceWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_RECORD_TOGGLE = "com.openclaw.voicewidget.RECORD_TOGGLE"
        const val PREFS_NAME = "VoiceWidgetPrefs"
        const val PREF_IS_RECORDING = "is_recording"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_RECORD_TOGGLE) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isRecording = prefs.getBoolean(PREF_IS_RECORDING, false)
            
            if (isRecording) {
                // 停止录音并发送
                RecordingService.stopRecording(context)
            } else {
                // 开始录音
                RecordingService.startRecording(context)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isRecording = prefs.getBoolean(PREF_IS_RECORDING, false)
        
        val views = RemoteViews(context.packageName, R.layout.widget_voice)
        
        // 设置状态
        if (isRecording) {
            views.setTextViewText(R.id.btn_record, "🔴")
            views.setTextViewText(R.id.tv_status, context.getString(R.string.tap_to_stop))
        } else {
            views.setTextViewText(R.id.btn_record, "🎤")
            views.setTextViewText(R.id.tv_status, context.getString(R.string.tap_to_record))
        }
        
        // 设置点击事件
        val intent = Intent(context, VoiceWidget::class.java).apply {
            action = ACTION_RECORD_TOGGLE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_record, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    fun updateWidgetState(context: Context, isRecording: Boolean, duration: String = "") {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            android.content.ComponentName(context, VoiceWidget::class.java)
        )
        
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_voice)
            
            if (isRecording) {
                views.setTextViewText(R.id.btn_record, "🔴")
                views.setTextViewText(R.id.tv_status, context.getString(R.string.tap_to_stop))
                views.setTextViewText(R.id.tv_duration, duration)
                views.setViewVisibility(R.id.tv_duration, android.view.View.VISIBLE)
            } else {
                views.setTextViewText(R.id.btn_record, "🎤")
                views.setTextViewText(R.id.tv_status, context.getString(R.string.tap_to_record))
                views.setViewVisibility(R.id.tv_duration, android.view.View.GONE)
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
