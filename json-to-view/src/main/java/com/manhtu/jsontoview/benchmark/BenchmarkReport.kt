package com.manhtu.jsontoview.benchmark

data class BenchmarkReport(
    val mode: RenderMode,
    val treeName: String,
    val treeNodeCount: Int,
    val maxTreeDepth: Int,
    val measureMs: Double,
    val layoutMs: Double,
    val drawMs: Double,
    val firstFrameMs: Double,
    val scrollAvgFrameMs: Double?,
    val droppedFrames: Int?,
    val viewCount: Int,
    val maxViewDepth: Int,
    val javaHeapMb: Double,
    val pssMb: Double?,
) {
    override fun toString(): String =
        "BenchmarkReport(mode=$mode, tree=$treeName, nodes=$treeNodeCount, depth=$maxTreeDepth, " +
            "measure=${"%.3f".format(measureMs)}ms, layout=${"%.3f".format(layoutMs)}ms, " +
            "draw=${"%.3f".format(drawMs)}ms, firstFrame=${"%.3f".format(firstFrameMs)}ms, " +
            "scrollAvg=${scrollAvgFrameMs?.let { "%.3f".format(it) }}, dropped=$droppedFrames, " +
            "views=$viewCount, viewDepth=$maxViewDepth, heap=${"%.2f".format(javaHeapMb)}MB, " +
            "pss=${pssMb?.let { "%.2f".format(it) }})"
}

fun deltaPct(nested: Double, flat: Double): Double =
    if (flat == 0.0) 0.0 else (nested - flat) / flat * 100.0
