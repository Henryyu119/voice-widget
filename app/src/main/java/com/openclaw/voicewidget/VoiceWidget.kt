package com.openclaw.voicewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class VoiceWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            val views = buildRemoteViews(context, RecordingService.isRecordingNow, if (RecordingService.isRecordingNow) "录音中，点击停止" else context.getString(R.string.tap_to_record))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_RECORDING -> toggleRecording(context)
            RecordingService.ACTION_STATUS_CHANGED -> {
                val isRecording = intent.getBooleanExtra(RecordingService.EXTRA_IS_RECORDING, false)
                val status = intent.getStringExtra(RecordingService.EXTRA_STATUS_TEXT) ?: context.getString(R.string.tap_to_record)
                updateAllWidgets(context, isRecording, status)
            }
        }
    }

    private fun toggleRecording(context: Context) {
        val serviceIntent = Intent(context, RecordingService::class.java).apply {
            action = if (RecordingService.isRecordingNow) RecordingService.ACTION_STOP else RecordingService.ACTION_START
        }
        context.startForegroundService(serviceIntent)
    }

    companion object {
        private const val ACTION_TOGGLE_RECORDING = "com.openclaw.voicewidget.action.TOGGLE_RECORDING"

        fun updateAllWidgets(context: Context, isRecording: Boolean, status: String) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, VoiceWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            appWidgetIds.forEach { appWidgetId ->
                val views = buildRemoteViews(context, isRecording, status)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun buildRemoteViews(context: Context, isRecording: Boolean, status: String): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_voice)
            views.setTextViewText(R.id.btn_record, if (isRecording) "🔴" else "🎤")
            views.setTextViewText(R.id.tv_status, status)
            views.setTextViewText(R.id.tv_duration, if (isRecording) "点击再次停止" else "")
            views.setViewVisibility(R.id.tv_duration, if (isRecording) android.view.View.VISIBLE else android.view.View.GONE)

            val toggleIntent = Intent(context, VoiceWidget::class.java).apply { action = ACTION_TOGGLE_RECORDING }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_record, pendingIntent)
            views.setOnClickPendingIntent(R.id.tv_status, pendingIntent)
            return views
        }
    }
}
