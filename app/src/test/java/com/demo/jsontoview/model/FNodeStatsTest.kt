package com.demo.jsontoview.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FNodeStatsTest {
    @Test
    fun nodeCount_and_maxDepth() {
        val leaf = FNode(NodeKind.TEXT, NodeProps(width = Dimension.WRAP, height = Dimension.WRAP, text = "a"))
        val row = FNode(NodeKind.ROW, NodeProps(width = Dimension.MATCH, height = Dimension.WRAP), listOf(leaf, leaf))
        val root = FNode(NodeKind.COLUMN, NodeProps(width = Dimension.MATCH, height = Dimension.MATCH), listOf(row))
        assertEquals(4, root.nodeCount())
        assertEquals(3, root.maxDepth())
    }
}
