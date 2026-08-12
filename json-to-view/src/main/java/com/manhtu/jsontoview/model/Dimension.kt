package com.manhtu.jsontoview.model

enum class DimUnit {
    DP,
    PX,
    PERCENT,
}

data class Dimension(
    val value: Int,
    val unit: DimUnit = DimUnit.DP,
) {
    companion object {
        val MATCH = Dimension(-1)
        val WRAP = Dimension(-2)
    }
}

data class EdgeInsets(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
) {
    companion object {
        val ZERO = EdgeInsets()
    }
}
