package com.manhtu.jsontoview.benchmark

import com.manhtu.jsontoview.util.Timing
import org.junit.Assert.assertEquals
import org.junit.Test

class BenchmarkReportTest {
    @Test
    fun deltaPct_and_median_helpers() {
        assertEquals(100.0, deltaPct(nested = 20.0, flat = 10.0), 0.0)
        assertEquals(0.0, deltaPct(nested = 5.0, flat = 0.0), 0.0)
        assertEquals(-50.0, deltaPct(nested = 5.0, flat = 10.0), 0.0)
        assertEquals(3.0, Timing.median(listOf(1.0, 3.0, 5.0, 9.0, 2.0)), 0.0)
    }
}
