package com.smuxo.blackhole.network

import android.content.Context

data class ModelConfig(
    val name: String,
    val provider: String,
    val model: String,
    val apiBase: String,
    val apiKey: String
)

data class AppConfig(
    val name: String,
    val version: String,
    val schema: String,
    val models: List<ModelConfig>
)

object ConfigLoader {

    fun load(context: Context, assetName: String = "config.yaml"): AppConfig {
        val text = context.assets.open(assetName).bufferedReader().use { it.readText() }
        return parse(text)
    }

    private fun parse(text: String): AppConfig {
        var name = ""
        var version = ""
        var schema = "v1"
        val models = mutableListOf<ModelConfig>()

        var curName = ""
        var curProvider = ""
        var curModel = ""
        var curApiBase = ""
        var curApiKey = ""
        var inModels = false
        var hasCurrent = false

        fun flush() {
            if (hasCurrent) {
                models.add(ModelConfig(curName, curProvider, curModel, curApiBase, curApiKey))
            }
            curName = ""
            curProvider = ""
            curModel = ""
            curApiBase = ""
            curApiKey = ""
            hasCurrent = false
        }

        text.lines().forEach { rawLine ->
            val line = rawLine.trimEnd()
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            if (!inModels) {
                when {
                    trimmed.startsWith("name:") -> name = valueOf(trimmed)
                    trimmed.startsWith("version:") -> version = valueOf(trimmed)
                    trimmed.startsWith("schema:") -> schema = valueOf(trimmed)
                    trimmed.startsWith("models:") -> inModels = true
                }
            } else {
                if (trimmed.startsWith("- name:")) {
                    flush()
                    hasCurrent = true
                    curName = valueOf(trimmed.removePrefix("-").trim())
                } else if (trimmed.startsWith("name:")) {
                    curName = valueOf(trimmed)
                } else if (trimmed.startsWith("provider:")) {
                    curProvider = valueOf(trimmed)
                } else if (trimmed.startsWith("model:")) {
                    curModel = valueOf(trimmed)
                } else if (trimmed.startsWith("apiBase:")) {
                    curApiBase = valueOf(trimmed)
                } else if (trimmed.startsWith("apiKey:")) {
                    curApiKey = valueOf(trimmed)
                }
            }
        }
        flush()

        return AppConfig(name, version, schema, models)
    }

    private fun valueOf(line: String): String {
        val idx = line.indexOf(':')
        if (idx < 0) return ""
        var v = line.substring(idx + 1).trim()
        if (v.startsWith("\"") && v.endsWith("\"") && v.length >= 2) {
            v = v.substring(1, v.length - 1)
        }
        return v
    }
}
