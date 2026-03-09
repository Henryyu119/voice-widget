package com.openclaw.voicewidget

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class TextEntryActivity : AppCompatActivity() {
    private val widgetApiClient = WidgetApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_entry)

        val editText = findViewById<EditText>(R.id.editTextContent)
        val sendButton = findViewById<Button>(R.id.btnSendText)
        val cancelButton = findViewById<Button>(R.id.btnCancelText)

        editText.requestFocus()
        editText.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        sendButton.setOnClickListener {
            val text = editText.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendButton.isEnabled = false
            thread {
                val result = widgetApiClient.sendText(text)
                runOnUiThread {
                    sendButton.isEnabled = true
                    if (result.isSuccess) {
                        editText.setText("")
                        Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val msg = result.exceptionOrNull()?.message.orEmpty()
                        val tip = when {
                            msg.contains("429") -> "发送太快，请稍后"
                            msg.contains("401") -> "认证失败"
                            msg.contains("Failed to connect") || msg.contains("timeout", true) -> "网络不可用"
                            else -> "服务异常：$msg"
                        }
                        Toast.makeText(this, tip, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        cancelButton.setOnClickListener { finish() }
    }
}
