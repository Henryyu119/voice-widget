package com.openclaw.voicewidget

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FeishuClient {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun sendText(text: String): Result<Unit> {
        return try {
            val token = getTenantAccessToken().getOrThrow()
            val contentJson = gson.toJson(mapOf("text" to text))
            val bodyJson = gson.toJson(
                mapOf(
                    "receive_id" to AppConfig.FEISHU_CHAT_ID,
                    "msg_type" to "text",
                    "content" to contentJson
                )
            )

            val request = Request.Builder()
                .url("https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.e("FeishuClient", "sendText failed: ${response.code} $body")
                    return Result.failure(IllegalStateException("HTTP ${response.code}: $body"))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e("FeishuClient", "sendText exception", e)
            Result.failure(e)
        }
    }

    private fun getTenantAccessToken(): Result<String> {
        return try {
            val bodyJson = gson.toJson(
                mapOf(
                    "app_id" to AppConfig.FEISHU_APP_ID,
                    "app_secret" to AppConfig.FEISHU_APP_SECRET
                )
            )

            val request = Request.Builder()
                .url("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException("HTTP ${response.code}: $body"))
                }

                val parsed = gson.fromJson(body, TenantTokenResponse::class.java)
                if (parsed.code != 0 || parsed.tenant_access_token.isNullOrBlank()) {
                    return Result.failure(IllegalStateException("Feishu error ${parsed.code}: ${parsed.msg}"))
                }
                Result.success(parsed.tenant_access_token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class TenantTokenResponse(
        val code: Int = -1,
        val msg: String? = null,
        val tenant_access_token: String? = null,
        val expire: Int? = null
    )
}
