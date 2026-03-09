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
            // 第一步：获取 tenant_access_token
            val tokenRequest = Request.Builder()
                .url("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal")
                .addHeader("Content-Type", "application/json")
                .post(
                    gson.toJson(
                        mapOf(
                            "app_id" to AppConfig.FEISHU_APP_ID,
                            "app_secret" to AppConfig.FEISHU_APP_SECRET
                        )
                    ).toRequestBody("application/json".toMediaType())
                )
                .build()

            val token = client.newCall(tokenRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException("获取token失败: HTTP ${response.code}"))
                }
                val tokenData = gson.fromJson(body, Map::class.java)
                tokenData["tenant_access_token"] as? String
                    ?: return Result.failure(IllegalStateException("token解析失败"))
            }

            // 第二步：发送消息到私聊（以机器人身份发送）
            val messageBody = gson.toJson(
                mapOf(
                    "receive_id" to "ou_d455238f2d901e2d86034c8adda408c3", // Jason 的 open_id
                    "msg_type" to "text",
                    "content" to gson.toJson(mapOf("text" to text))
                )
            )

            val messageRequest = Request.Builder()
                .url("https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=open_id")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(messageBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(messageRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException("发送消息失败: HTTP ${response.code}: $body"))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
