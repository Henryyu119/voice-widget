package com.openclaw.voicewidget

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SpeechRecognizerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUDIO_PATH = "extra_audio_path"
        const val EXTRA_FALLBACK_UPLOAD = "extra_fallback_upload"
    }

    private var recognizer: SpeechRecognizer? = null
    private var audioPath: String? = null
    private var fallbackUpload = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioPath = intent.getStringExtra(EXTRA_AUDIO_PATH)
        fallbackUpload = intent.getBooleanExtra(EXTRA_FALLBACK_UPLOAD, true)

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "系统语音识别不可用，进入兜底上传", Toast.LENGTH_LONG).show()
            openConfirmScreen("", true, "系统语音识别不可用")
            return
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onError(error: Int) {
                openConfirmScreen("", fallbackUpload, "识别失败，错误码: $error")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty()
                openConfirmScreen(text, fallbackUpload, null)
            }
        })

        startRecognition()
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话")
        }
        recognizer?.startListening(intent)
    }

    private fun openConfirmScreen(text: String, canFallbackUpload: Boolean, error: String?) {
        val intent = Intent(this, ConfirmActivity::class.java).apply {
            putExtra(ConfirmActivity.EXTRA_TEXT, text)
            putExtra(ConfirmActivity.EXTRA_AUDIO_PATH, audioPath)
            putExtra(ConfirmActivity.EXTRA_CAN_FALLBACK_UPLOAD, canFallbackUpload)
            putExtra(ConfirmActivity.EXTRA_ERROR, error)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        recognizer?.destroy()
        recognizer = null
        super.onDestroy()
    }
}
