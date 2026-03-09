package com.openclaw.voicewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TextWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_OPEN_TEXT_ENTRY) {
            val openIntent = Intent(context, TextEntryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(openIntent)
        }
    }

    companion object {
        private const val ACTION_OPEN_TEXT_ENTRY = "com.openclaw.voicewidget.action.OPEN_TEXT_ENTRY"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TextWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(componentName)
            ids.forEach { manager.updateAppWidget(it, buildRemoteViews(context)) }
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_text)
            val intent = Intent(context, TextWidgetProvider::class.java).apply {
                action = ACTION_OPEN_TEXT_ENTRY
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_text_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.tv_text_icon, pendingIntent)
            views.setOnClickPendingIntent(R.id.tv_text_label, pendingIntent)
            return views
        }
    }
}
