package com.manhtu.jsontoview.parse

import com.manhtu.jsontoview.model.NodeKind
import org.junit.Assert.assertEquals
import org.junit.Test

class JsonTreeParserTest {
    @Test
    fun parses_column_with_text_child() {
        val json = javaClass.classLoader!!.getResourceAsStream("sample_tree.json")!!.reader().readText()
        val root = JsonTreeParser.parse(json)
        assertEquals(NodeKind.COLUMN, root.kind)
        assertEquals(1, root.children.size)
        assertEquals(NodeKind.TEXT, root.children[0].kind)
        assertEquals("Hello", root.children[0].props.text)
    }
}
