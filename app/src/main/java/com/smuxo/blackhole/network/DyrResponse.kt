package com.smuxo.blackhole.network

import org.json.JSONObject

data class DyrResponse(
    val say: String,
    val animation: String,
    val moveToX: Float?,
    val moveToY: Float?,
    val action: String?,
    val value: String?
)

object DyrResponseParser {

    fun parse(raw: String): DyrResponse? {
        val cleaned = extractJson(raw) ?: return null
        return try {
            val obj = JSONObject(cleaned)
            val say = obj.optString("say", "")
            val animation = obj.optString("animation", "idle")
            var moveX: Float? = null
            var moveY: Float? = null
            val moveArr = obj.opt("move_to")
            if (moveArr is org.json.JSONArray && moveArr.length() >= 2) {
                moveX = moveArr.getDouble(0).toFloat()
                moveY = moveArr.getDouble(1).toFloat()
            }
            val actionRaw = obj.opt("action")
            val action = if (actionRaw == null || actionRaw == JSONObject.NULL) null else actionRaw.toString()
            val valueRaw = obj.opt("value")
            val value = if (valueRaw == null || valueRaw == JSONObject.NULL) null else valueRaw.toString()
            DyrResponse(say, animation, moveX, moveY, action, value)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractJson(raw: String): String? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start < 0 || end < 0 || end < start) return null
        return raw.substring(start, end + 1)
    }
}
