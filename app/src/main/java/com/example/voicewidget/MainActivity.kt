package com.example.voicewidget

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private lateinit var recordButton: Button
    private lateinit var statusText: TextView

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val SERVER_URL = "http://43.163.97.77:3000/upload"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordButton = findViewById(R.id.recordButton)
        statusText = findViewById(R.id.statusText)

        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    private fun startRecording() {
        updateStatus("准备录音...")
        try {
            audioFile = File(externalCacheDir, "voice_${System.currentTimeMillis()}.m4a")
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            recordButton.text = "停止录音"
            updateStatus("正在录音...")
        } catch (e: Exception) {
            updateStatus("录音失败: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        updateStatus("停止录音...")
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            recordButton.text = "开始录音"
            
            audioFile?.let { file ->
                updateStatus("准备上传: ${file.length()} 字节")
                uploadAudio(file)
            }
        } catch (e: Exception) {
            updateStatus("停止录音失败: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun uploadAudio(file: File) {
        updateStatus("开始上传到服务器...")
        
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "audio",
                file.name,
                file.asRequestBody("audio/m4a".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody)
            .build()

        updateStatus("发送请求到: $SERVER_URL")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    updateStatus("上传失败: ${e.message}")
                    Toast.makeText(this@MainActivity, "上传失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        updateStatus("上传成功! 状态码: ${response.code}")
                        Toast.makeText(this@MainActivity, "上传成功！", Toast.LENGTH_SHORT).show()
                    } else {
                        updateStatus("上传失败: HTTP ${response.code}")
                        Toast.makeText(this@MainActivity, "上传失败: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            statusText.text = message
        }
    }
}
