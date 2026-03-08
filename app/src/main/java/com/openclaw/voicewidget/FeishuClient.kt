package com.openclaw.voicewidget

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class FeishuClient(private val context: Context) {

    companion object {
        // 飞书配置
        private const val APP_ID = "cli_a927a647fb381bc4"
        private const val APP_SECRET = "h1DkoMOlbHZpggRtxR1ILf2cbXakF81y"
        private const val USER_ID = "ou_0ae310fc167977f6add924cb8da58b20"  // 你的飞书用户 ID
        
        // API 端点
        private const val TOKEN_URL = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal"
        private const val UPLOAD_URL = "https://open.feishu.cn/open-apis/im/v1/files"
        private const val DRIVE_UPLOAD_URL = "https://open.feishu.cn/open-apis/drive/v1/medias/upload_all"
        private const val MESSAGE_URL = "https://open.feishu.cn/open-apis/im/v1/messages"
    }

    private val client = OkHttpClient()
    private var tenantAccessToken: String? = null

    /**
     * 发送语音消息到飞书
     */
    suspend fun sendVoiceMessage(audioFile: File): Boolean {
        return try {
            android.util.Log.d("FeishuClient", "Starting to send voice message: ${audioFile.name}")
            
            // 1. 获取 tenant_access_token
            val token = getTenantAccessToken()
            if (token == null) {
                android.util.Log.e("FeishuClient", "Failed to get tenant access token")
                return false
            }
            android.util.Log.d("FeishuClient", "Got tenant access token")
            
            // 2. 上传音频文件
            val fileKey = uploadFile(token, audioFile)
            if (fileKey == null) {
                android.util.Log.e("FeishuClient", "Failed to upload file")
                return false
            }
            android.util.Log.d("FeishuClient", "File uploaded, file_key: $fileKey")
            
            // 3. 发送消息
            val success = sendMessage(token, fileKey)
            android.util.Log.d("FeishuClient", "Send message result: $success")
            
            success
            
        } catch (e: Exception) {
            android.util.Log.e("FeishuClient", "Error sending voice message", e)
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取 tenant_access_token
     */
    private fun getTenantAccessToken(): String? {
        val json = """
            {
                "app_id": "$APP_ID",
                "app_secret": "$APP_SECRET"
            }
        """.trimIndent()

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json
        )

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            
            val responseBody = response.body?.string() ?: return null
            // 简单解析 JSON (生产环境建议使用 Gson)
            val tokenMatch = Regex(""""tenant_access_token":"([^"]+)"""").find(responseBody)
            tenantAccessToken = tokenMatch?.groupValues?.get(1)
            return tenantAccessToken
        }
    }

    /**
     * 上传文件到飞书云盘
     */
    private fun uploadToDrive(token: String, file: File): Boolean {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file_name", file.name)
            .addFormDataPart("parent_type", "explorer")
            .addFormDataPart("parent_node", "0")  // 0 表示根目录
            .addFormDataPart("size", file.length().toString())
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("audio/mpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(DRIVE_UPLOAD_URL)
            .header("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            android.util.Log.d("FeishuClient", "Drive upload response: $responseBody")
            return response.isSuccessful
        }
    }

    /**
     * 上传文件到飞书
     */
    private fun uploadFile(token: String, file: File): String? {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file_type", "opus")
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("audio/mpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .header("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            android.util.Log.d("FeishuClient", "Upload response: $responseBody")
            
            if (!response.isSuccessful) {
                android.util.Log.e("FeishuClient", "Upload failed: ${response.code}")
                return null
            }
            
            // 解析 file_key
            val fileKeyMatch = Regex(""""file_key":"([^"]+)"""").find(responseBody)
            return fileKeyMatch?.groupValues?.get(1)
        }
    }

    /**
     * 发送消息
     */
    private fun sendMessage(token: String, fileKey: String): Boolean {
        val json = """
            {
                "receive_id": "$USER_ID",
                "msg_type": "audio",
                "content": "{\"file_key\":\"$fileKey\"}"
            }
        """.trimIndent()

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json
        )

        val request = Request.Builder()
            .url("$MESSAGE_URL?receive_id_type=open_id")
            .header("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            android.util.Log.d("FeishuClient", "Send message response: $responseBody")
            
            if (!response.isSuccessful) {
                android.util.Log.e("FeishuClient", "Send message failed: ${response.code}")
            }
            
            return response.isSuccessful
        }
    }
}
