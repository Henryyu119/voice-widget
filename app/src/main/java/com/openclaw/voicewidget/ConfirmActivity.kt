package com.openclaw.voicewidget

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import kotlin.concurrent.thread

class ConfirmActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_AUDIO_PATH = "extra_audio_path"
        const val EXTRA_CAN_FALLBACK_UPLOAD = "extra_can_fallback_upload"
        const val EXTRA_ERROR = "extra_error"
    }

    private val repository = VoiceRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)

        val editText = findViewById<EditText>(R.id.editRecognizedText)
        val errorText = findViewById<TextView>(R.id.textError)
        val sendButton = findViewById<Button>(R.id.btnSend)
        val backupButton = findViewById<Button>(R.id.btnBackup)
        val cancelButton = findViewById<Button>(R.id.btnCancel)

        val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
        val audioPath = intent.getStringExtra(EXTRA_AUDIO_PATH)
        val canFallbackUpload = intent.getBooleanExtra(EXTRA_CAN_FALLBACK_UPLOAD, true)
        val error = intent.getStringExtra(EXTRA_ERROR)

        editText.setText(text)
        errorText.visibility = if (error.isNullOrBlank()) View.GONE else View.VISIBLE
        errorText.text = error.orEmpty()
        backupButton.visibility = if (canFallbackUpload && !audioPath.isNullOrBlank()) View.VISIBLE else View.GONE

        sendButton.setOnClickListener {
            val finalText = editText.text?.toString()?.trim().orEmpty()
            if (finalText.isBlank()) {
                Toast.makeText(this, "请先确认或输入文字", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setButtonsEnabled(false, sendButton, backupButton, cancelButton)
            thread {
                val result = repository.sendRecognizedText(finalText)
                runOnUiThread {
                    setButtonsEnabled(true, sendButton, backupButton, cancelButton)
                    if (result.isSuccess) {
                        Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "发送失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        backupButton.setOnClickListener {
            val file = audioPath?.let { File(it) }
            if (file == null || !file.exists()) {
                Toast.makeText(this, "找不到录音文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setButtonsEnabled(false, sendButton, backupButton, cancelButton)
            thread {
                val ok = repository.uploadVoiceBackup(file)
                runOnUiThread {
                    setButtonsEnabled(true, sendButton, backupButton, cancelButton)
                    Toast.makeText(this, if (ok) "备份上传成功" else "备份上传失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cancelButton.setOnClickListener { finish() }
    }

    private fun setButtonsEnabled(enabled: Boolean, vararg buttons: Button) {
        buttons.forEach { it.isEnabled = enabled }
    }
}
