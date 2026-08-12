package com.manhtu.jsontoview.parity

import com.manhtu.jsontoview.model.NodeKind
import com.manhtu.jsontoview.parse.JsonTreeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parser/model parity for schema fixtures (same FNode regardless of renderer).
 */
class ParityModelTest {

    @Test
    fun hello_fixture() {
        val json = read("hello.json")
        val root = JsonTreeParser.parse(json)
        assertEquals(NodeKind.COLUMN, root.kind)
        assertTrue(root.children.any { it.kind == NodeKind.TEXT && it.props.action != null })
    }

    @Test
    fun card_fixture_has_image_and_action() {
        // card is under docs; embed minimal equivalent
        val json = """
            {
              "type": "column",
              "props": {
                "width": { "value": -1, "unit": "dp" },
                "height": { "value": -2, "unit": "dp" },
                "action": { "type": "navigate", "payload": "card/1" }
              },
              "children": [
                {
                  "type": "box",
                  "props": {
                    "width": { "value": -1, "unit": "dp" },
                    "height": { "value": 160, "unit": "dp" },
                    "imageUrl": "https://example.com/x.jpg"
                  },
                  "children": []
                }
              ]
            }
        """.trimIndent()
        val root = JsonTreeParser.parse(json)
        assertEquals(NodeKind.COLUMN, root.kind)
        assertNotNull(root.props.action)
        assertEquals(NodeKind.BOX, root.children[0].kind)
        assertEquals("https://example.com/x.jpg", root.children[0].props.imageUrl)
    }

    private fun read(name: String): String =
        javaClass.classLoader!!.getResourceAsStream(name)!!.reader().readText()
}
