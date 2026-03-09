package com.openclaw.voicewidget

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class WidgetApiClient {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun sendText(text: String): Result<String> {
        return try {
            val bodyJson = gson.toJson(
                mapOf(
                    "text" to text,
                    "source" to "widget"
                )
            )

            val request = Request.Builder()
                .url(AppConfig.WIDGET_API_URL)
                .addHeader("Authorization", "Bearer ${AppConfig.WIDGET_TOKEN}")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException("HTTP ${response.code}: $body"))
                }
                Result.success(body)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
