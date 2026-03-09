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

        findViewById<TextView>(R.id.tvWebhookUrl).text = AppConfig.FEISHU_WEBHOOK_URL
        findViewById<TextView>(R.id.tvVoiceApi).text = AppConfig.VPS_BASE_URL + AppConfig.VPS_UPLOAD_ENDPOINT
    }
}
