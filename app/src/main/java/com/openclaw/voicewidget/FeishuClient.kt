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

    private val client = OkHttpClient()

    /**
     * 上传语音到 VPS，由 VPS 转发到飞书
     */
    suspend fun sendVoiceMessage(audioFile: File): Boolean {
        return try {
            android.util.Log.d("FeishuClient", "Uploading to VPS: ${audioFile.name}")
            
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

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                android.util.Log.d("FeishuClient", "Upload response: $responseBody")
                
                if (!response.isSuccessful) {
                    android.util.Log.e("FeishuClient", "Upload failed: ${response.code}")
                }
                
                return response.isSuccessful
            }
            
        } catch (e: Exception) {
            android.util.Log.e("FeishuClient", "Error uploading to VPS", e)
            e.printStackTrace()
            false
        }
    }
}
