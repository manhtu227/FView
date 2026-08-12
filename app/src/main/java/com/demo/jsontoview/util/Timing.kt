package com.demo.jsontoview.util

object Timing {
    fun median(values: List<Double>): Double {
        require(values.isNotEmpty()) { "median of empty list" }
        val sorted = values.sorted()
        val n = sorted.size
        return if (n % 2 == 1) {
            sorted[n / 2]
        } else {
            (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        }
    }

    fun nsToMs(ns: Long): Double = ns / 1_000_000.0
}
