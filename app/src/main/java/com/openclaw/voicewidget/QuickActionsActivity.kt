package com.openclaw.voicewidget

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class QuickActionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_actions)

        findViewById<Button>(R.id.btnQuickText).setOnClickListener {
            startActivity(Intent(this, TextEntryActivity::class.java))
        }

        findViewById<Button>(R.id.btnVoiceDiary).setOnClickListener {
            startActivity(Intent(this, TextEntryActivity::class.java))
            finish()
        }
    }
}
