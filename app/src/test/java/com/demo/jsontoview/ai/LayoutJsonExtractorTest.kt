package com.demo.jsontoview.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutJsonExtractorTest {

    @Test
    fun extracts_plain_object() {
        val json = """{"type":"column","props":{},"children":[]}"""
        assertEquals(json, LayoutJsonExtractor.extractJsonObject(json))
    }

    @Test
    fun extracts_fenced_json() {
        val raw = """
            Here you go:
            ```json
            {"type":"text","props":{"text":"Hi"},"children":[]}
            ```
        """.trimIndent()
        val out = LayoutJsonExtractor.extractJsonObject(raw)
        assertTrue(out.contains("\"type\":\"text\""))
        assertTrue(out.startsWith("{") && out.endsWith("}"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun fails_without_braces() {
        LayoutJsonExtractor.extractJsonObject("no json here")
    }
}
