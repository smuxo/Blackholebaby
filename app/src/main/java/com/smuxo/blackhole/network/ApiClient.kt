package com.smuxo.blackhole.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ApiClient(
    private val config: ModelConfig,
    private val screenWidthPx: Int,
    private val screenHeightPx: Int
) {

    companion object {
        const val BASE_PROMPT = "Ты — маленький карапуз-чёрная дыра по имени Дыр. Живёшь на экране телефона. Дружелюбный, но слегка ворчливый: делаешь всё, что просят, но с коротким комментарием. Говоришь коротко, 1-2 предложения, по-русски. Отвечай ТОЛЬКО валидным JSON без лишнего текста:\n{ \"say\": \"что сказать\", \"animation\": \"idle|run|jump|sleep|happy|surprise|action|celebrate\", \"move_to\": [x, y], \"action\": \"open_url|open_app|set_timer|smalltalk|null\", \"value\": \"параметр\" }"
    }

    private fun buildSystemPrompt(): String {
        return BASE_PROMPT + "\nРазмер экрана телефона: " + screenWidthPx + "x" + screenHeightPx + " пикселей. Координаты move_to указывай строго в этих пределах, x от 0 до " + screenWidthPx + ", y от 0 до " + screenHeightPx + "."
    }

    suspend fun send(userText: String): DyrResponse? = withContext(Dispatchers.IO) {
        try {
            val url = URL(config.apiBase.trimEnd('/') + "/chat/completions")
            val connection = url.openConnection()
            val http: HttpURLConnection = when (connection) {
                is HttpsURLConnection -> connection
                is HttpURLConnection -> connection
                else -> return@withContext null
            }
            http.requestMethod = "POST"
            http.setRequestProperty("Content-Type", "application/json")
            http.setRequestProperty("Authorization", "Bearer " + config.apiKey)
            http.doOutput = true
            http.connectTimeout = 15000
            http.readTimeout = 20000

            val messages = JSONArray()
            val systemMsg = JSONObject()
            systemMsg.put("role", "system")
            systemMsg.put("content", buildSystemPrompt())
            messages.put(systemMsg)
            val userMsg = JSONObject()
            userMsg.put("role", "user")
            userMsg.put("content", userText)
            messages.put(userMsg)

            val body = JSONObject()
            body.put("model", config.model)
            body.put("messages", messages)
            body.put("temperature", 0.7)

            http.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = http.responseCode
            val stream = if (code in 200..299) http.inputStream else http.errorStream
            val responseText = stream.bufferedReader().use { it.readText() }
            http.disconnect()

            if (code !in 200..299) return@withContext null

            val root = JSONObject(responseText)
            val choices = root.optJSONArray("choices") ?: return@withContext null
            if (choices.length() == 0) return@withContext null
            val messageObj = choices.getJSONObject(0).optJSONObject("message") ?: return@withContext null
            val content = messageObj.optString("content", "")
            DyrResponseParser.parse(content)
        } catch (e: Exception) {
            null
        }
    }
}
