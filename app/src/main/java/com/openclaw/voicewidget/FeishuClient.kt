package com.openclaw.voicewidget

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FeishuClient(private val context: Context) {

    companion object {
        // VPS 接收服务地址
        private const val UPLOAD_URL = "http://43.163.97.77:3003/upload"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * 上传语音到 VPS，由 VPS 转发到飞书
     */
    suspend fun sendVoiceMessage(audioFile: File): Boolean {
        return try {
            android.util.Log.d("FeishuClient", "=== Starting upload ===")
            android.util.Log.d("FeishuClient", "File: ${audioFile.name}, Size: ${audioFile.length()} bytes")
            android.util.Log.d("FeishuClient", "URL: $UPLOAD_URL")
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "audio",
                    audioFile.name,
                    audioFile.asRequestBody("audio/mpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            android.util.Log.d("FeishuClient", "Sending request...")
            
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                android.util.Log.d("FeishuClient", "Response code: ${response.code}")
                android.util.Log.d("FeishuClient", "Response body: $responseBody")
                
                if (!response.isSuccessful) {
                    android.util.Log.e("FeishuClient", "Upload failed: ${response.code} ${response.message}")
                    return false
                }
                
                android.util.Log.d("FeishuClient", "Upload successful!")
                return true
            }
            
        } catch (e: Exception) {
            android.util.Log.e("FeishuClient", "Exception during upload: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
