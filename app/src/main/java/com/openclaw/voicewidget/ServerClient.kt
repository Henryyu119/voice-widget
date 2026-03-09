package com.openclaw.voicewidget

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ServerClient(private val context: Context) {

    companion object {
        // 服务器配置
        private const val SERVER_URL = "http://43.163.97.77:3002"
        private const val UPLOAD_ENDPOINT = "/api/diary/voice"
    }

    private val client = OkHttpClient()

    /**
     * 上传语音到服务器
     */
    fun uploadVoice(audioFile: File): Boolean {
        return try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "audio",
                    audioFile.name,
                    audioFile.asRequestBody("audio/m4a".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url("$SERVER_URL$UPLOAD_ENDPOINT")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
