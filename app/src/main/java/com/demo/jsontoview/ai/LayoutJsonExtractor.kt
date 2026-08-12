package com.demo.jsontoview.ai

/**
 * Pulls a single JSON object from model text (plain JSON or markdown fences).
 */
object LayoutJsonExtractor {

    fun extractJsonObject(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("Empty model response")
        }

        // ```json ... ``` or ``` ... ```
        val fence = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val fenced = fence.find(trimmed)?.groupValues?.getOrNull(1)?.trim()
        val candidate = fenced ?: trimmed

        val start = candidate.indexOf('{')
        if (start < 0) {
            throw IllegalArgumentException("No JSON object found in model response")
        }
        var depth = 0
        var inString = false
        var escape = false
        for (i in start until candidate.length) {
            val c = candidate[i]
            if (inString) {
                when {
                    escape -> escape = false
                    c == '\\' -> escape = true
                    c == '"' -> inString = false
                }
                continue
            }
            when (c) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return candidate.substring(start, i + 1)
                    }
                }
            }
        }
        throw IllegalArgumentException("Unbalanced JSON braces in model response")
    }
}
