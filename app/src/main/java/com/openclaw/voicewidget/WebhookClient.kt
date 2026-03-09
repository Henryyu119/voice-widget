package com.openclaw.voicewidget

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class WebhookClient {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun sendText(text: String): Result<Unit> {
        return try {
            // 自动 @Jason (使用 user_id，飞书会自动显示正确的名字)
            val bodyJson = gson.toJson(
                mapOf(
                    "msg_type" to "text",
                    "content" to mapOf(
                        "text" to "<at user_id=\"ou_d455238f2d901e2d86034c8adda408c3\"></at> $text"
                    )
                )
            )

            val request = Request.Builder()
                .url(AppConfig.FEISHU_WEBHOOK_URL)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException("HTTP ${response.code}: $body"))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
