package com.demo.jsontoview.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Calls xAI (SpaceXAI) OpenAI-compatible chat completions to generate layout JSON.
 * Demo sample only — not part of the published json-to-view AAR.
 */
class LayoutAiClient(
    private val apiKey: String,
    private val baseUrl: String = "https://api.x.ai/v1",
    private val model: String = "grok-4.5",
    private val systemPrompt: String,
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson(),
) {
    /**
     * Blocking network call. Run on a background thread.
     * @return raw assistant message content
     */
    fun generateRaw(userPrompt: String): String {
        require(apiKey.isNotBlank()) { "XAI_API_KEY is empty" }

        val body = JsonObject().apply {
            addProperty("model", model)
            addProperty("stream", false)
            add(
                "messages",
                gson.toJsonTree(
                    listOf(
                        mapOf("role" to "system", "content" to systemPrompt),
                        mapOf(
                            "role" to "user",
                            "content" to buildString {
                                append("Create a mobile screen layout for:\n")
                                append(userPrompt.trim())
                                append("\n\nReturn only the JSON object.")
                            },
                        ),
                    ),
                ),
            )
        }

        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()

        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("xAI HTTP ${response.code}: ${raw.take(500)}")
            }
            val root = gson.fromJson(raw, JsonObject::class.java)
            val content = root
                .getAsJsonArray("choices")
                ?.get(0)
                ?.asJsonObject
                ?.getAsJsonObject("message")
                ?.get("content")
                ?.asString
            if (content.isNullOrBlank()) {
                throw IllegalStateException("Empty completion content")
            }
            return content
        }
    }

    /** Generate and extract a single JSON object string. */
    fun generateLayoutJson(userPrompt: String): String {
        val raw = generateRaw(userPrompt)
        return LayoutJsonExtractor.extractJsonObject(raw)
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
