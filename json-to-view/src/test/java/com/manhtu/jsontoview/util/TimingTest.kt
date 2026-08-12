package com.manhtu.jsontoview.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimingTest {
    @Test
    fun median_odd_and_even() {
        assertEquals(2.0, Timing.median(listOf(3.0, 1.0, 2.0)), 0.0)
        assertEquals(2.5, Timing.median(listOf(1.0, 2.0, 3.0, 4.0)), 0.0)
    }
}
