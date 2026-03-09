package com.openclaw.voicewidget

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOpenQuickActions).setOnClickListener {
            startActivity(Intent(this, QuickActionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnOpenTextEntry).setOnClickListener {
            startActivity(Intent(this, TextEntryActivity::class.java))
        }

        findViewById<TextView>(R.id.tvWebhookUrl).text = AppConfig.WIDGET_API_URL
        findViewById<TextView>(R.id.tvVoiceApi).text = "Jason 中转 API（Bearer 已内置）"
    }
}
