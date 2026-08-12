package com.manhtu.jsontoview.parse

import com.manhtu.jsontoview.model.NodeKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonTreeParserImageActionTest {

    @Test
    fun stable_type_text_with_action() {
        val json = javaClass.classLoader!!.getResourceAsStream("hello.json")!!.reader().readText()
        val root = JsonTreeParser.parse(json)
        assertEquals(NodeKind.COLUMN, root.kind)
        assertEquals(1, root.children.size)
        val text = root.children[0]
        assertEquals(NodeKind.TEXT, text.kind)
        assertEquals("Hello", text.props.text)
        assertNotNull(text.props.action)
        assertEquals("open_url", text.props.action!!.type)
        assertEquals("https://example.com", text.props.action!!.payload)
    }

    @Test
    fun stable_box_with_imageUrl() {
        val json = """
            {
              "type": "box",
              "props": {
                "width": { "value": 100, "unit": "dp" },
                "height": { "value": 80, "unit": "dp" },
                "imageUrl": "https://example.com/a.jpg"
              },
              "children": []
            }
        """.trimIndent()
        val root = JsonTreeParser.parse(json)
        assertEquals(NodeKind.BOX, root.kind)
        assertEquals("https://example.com/a.jpg", root.props.imageUrl)
    }

    @Test
    fun legacy_column_still_parses() {
        val json = javaClass.classLoader!!.getResourceAsStream("sample_tree.json")!!.reader().readText()
        val root = JsonTreeParser.parse(json)
        assertEquals(NodeKind.COLUMN, root.kind)
        assertTrue(root.children.isNotEmpty())
        assertEquals(NodeKind.TEXT, root.children[0].kind)
    }
}
