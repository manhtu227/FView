package com.manhtu.jsontoview.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntheticTreeFactoryTest {
    @Test
    fun deep_hasExpectedDepth() {
        val t = SyntheticTreeFactory.deep()
        assertEquals(20, t.root.maxDepth())
        assertTrue(t.root.nodeCount() in 20..80)
        assertEquals(TreeSource.SYNTHETIC, t.source)
    }

    @Test
    fun wide_isShallowButManyNodes() {
        val t = SyntheticTreeFactory.wide()
        assertTrue(t.root.maxDepth() <= 3)
        assertTrue(t.root.nodeCount() >= 180)
    }
}
