package com.manhtu.jsontoview.benchmark

import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.manhtu.jsontoview.flat.FlatHostView
import com.manhtu.jsontoview.model.NodeKind
import com.manhtu.jsontoview.model.TreeSpec
import com.manhtu.jsontoview.model.maxDepth
import com.manhtu.jsontoview.model.nodeCount
import com.manhtu.jsontoview.nested.NestedHost
import com.manhtu.jsontoview.util.HierarchyStats
import com.manhtu.jsontoview.util.Timing

/**
 * Runs flat/nested benchmarks on the main thread with warm-up + measured protocol.
 * Protocol: 3 warm-up + 5 measured → median (min/max logged).
 */
class BenchmarkRunner(
    private val container: ViewGroup,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) {
    companion object {
        private const val TAG = "FViewBench"
        private const val WARMUP = 3
        private const val MEASURED = 5
        private const val SCROLL_MS = 1000L
        private const val DROP_THRESHOLD_NS = 18_000_000L
    }

    fun run(
        spec: TreeSpec,
        mode: RenderMode,
        includeScroll: Boolean = true,
        onComplete: (BenchmarkReport) -> Unit,
    ) {
        require(Looper.myLooper() == Looper.getMainLooper()) {
            "BenchmarkRunner.run must be called on the main thread"
        }
        runIteration(spec, mode, includeScroll, warmupLeft = WARMUP, measured = mutableListOf(), onComplete)
    }

    private fun runIteration(
        spec: TreeSpec,
        mode: RenderMode,
        includeScroll: Boolean,
        warmupLeft: Int,
        measured: MutableList<Sample>,
        onComplete: (BenchmarkReport) -> Unit,
    ) {
        container.removeAllViews()
        val host: View = when (mode) {
            RenderMode.FLAT -> FlatHostView(container.context)
            RenderMode.NESTED -> NestedHost(container.context)
        }
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        val firstFrameStart = System.nanoTime()
        var firstFrameNs = 0L
        var firstFrameDone = false

        val onDrawListener = object : ViewTreeObserver.OnDrawListener {
            override fun onDraw() {
                if (!firstFrameDone) {
                    firstFrameNs = System.nanoTime() - firstFrameStart
                    firstFrameDone = true
                    host.viewTreeObserver.removeOnDrawListener(this)
                }
            }
        }
        host.viewTreeObserver.addOnDrawListener(onDrawListener)
        container.addView(host, lp)

        when (host) {
            is FlatHostView -> host.bind(spec.root)
            is NestedHost -> host.bind(spec.root)
        }

        val w = container.width.takeIf { it > 0 } ?: container.resources.displayMetrics.widthPixels
        val h = container.height.takeIf { it > 0 } ?: container.resources.displayMetrics.heightPixels
        val wSpec = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY)
        val hSpec = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)

        val measureStart = System.nanoTime()
        host.measure(wSpec, hSpec)
        val measureNsExternal = System.nanoTime() - measureStart

        val layoutStart = System.nanoTime()
        host.layout(0, 0, host.measuredWidth, host.measuredHeight)
        val layoutNsExternal = System.nanoTime() - layoutStart

        val measureNs = when (host) {
            is FlatHostView -> host.lastMeasureNs.takeIf { it > 0 } ?: measureNsExternal
            is NestedHost -> host.lastMeasureNs.takeIf { it > 0 } ?: measureNsExternal
            else -> measureNsExternal
        }
        val layoutNs = when (host) {
            is FlatHostView -> host.lastLayoutNs.takeIf { it > 0 } ?: layoutNsExternal
            is NestedHost -> host.lastLayoutNs.takeIf { it > 0 } ?: layoutNsExternal
            else -> layoutNsExternal
        }

        // Force a draw pass and capture draw duration via host timers + frame callback
        val drawStart = System.nanoTime()
        host.invalidate()
        Choreographer.getInstance().postFrameCallback {
            val drawNsExternal = System.nanoTime() - drawStart
            val drawNs = when (host) {
                is FlatHostView -> host.lastDrawNs.takeIf { it > 0 } ?: drawNsExternal
                is NestedHost -> host.lastDrawNs.takeIf { it > 0 } ?: drawNsExternal
                else -> drawNsExternal
            }
            if (!firstFrameDone) {
                firstFrameNs = System.nanoTime() - firstFrameStart
                firstFrameDone = true
                try {
                    host.viewTreeObserver.removeOnDrawListener(onDrawListener)
                } catch (_: Exception) {
                }
            }

            val hierarchy = HierarchyStats.snapshot(host)
            val sample = Sample(
                measureMs = Timing.nsToMs(measureNs),
                layoutMs = Timing.nsToMs(layoutNs),
                drawMs = Timing.nsToMs(drawNs),
                firstFrameMs = Timing.nsToMs(firstFrameNs),
                viewCount = hierarchy.viewCount,
                maxViewDepth = hierarchy.maxDepth,
            )

            if (warmupLeft > 0) {
                mainHandler.post {
                    runIteration(spec, mode, includeScroll, warmupLeft - 1, measured, onComplete)
                }
                return@postFrameCallback
            }

            measured.add(sample)
            if (measured.size < MEASURED) {
                mainHandler.post {
                    runIteration(spec, mode, includeScroll, warmupLeft = 0, measured, onComplete)
                }
                return@postFrameCallback
            }

            fun finish(scrollAvg: Double?, dropped: Int?) {
                val rt = Runtime.getRuntime()
                val heapMb = (rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0
                val pssMb = try {
                    Debug.getPss() / 1024.0
                } catch (_: Exception) {
                    null
                }
                val report = BenchmarkReport(
                    mode = mode,
                    treeName = spec.name,
                    treeNodeCount = spec.root.nodeCount(),
                    maxTreeDepth = spec.root.maxDepth(),
                    measureMs = Timing.median(measured.map { it.measureMs }),
                    layoutMs = Timing.median(measured.map { it.layoutMs }),
                    drawMs = Timing.median(measured.map { it.drawMs }),
                    firstFrameMs = Timing.median(measured.map { it.firstFrameMs }),
                    scrollAvgFrameMs = scrollAvg,
                    droppedFrames = dropped,
                    viewCount = measured.map { it.viewCount }.sorted()[measured.size / 2],
                    maxViewDepth = measured.map { it.maxViewDepth }.sorted()[measured.size / 2],
                    javaHeapMb = heapMb,
                    pssMb = pssMb,
                )
                Log.i(TAG, report.toString())
                Log.i(
                    TAG,
                    "raw measure min/max=${measured.minOf { it.measureMs }}/${measured.maxOf { it.measureMs }} " +
                        "layout min/max=${measured.minOf { it.layoutMs }}/${measured.maxOf { it.layoutMs }}",
                )
                onComplete(report)
            }

            val shouldScroll = includeScroll && (
                spec.root.kind == NodeKind.LIST ||
                    host is NestedHost && findRecyclerView(host) != null ||
                    host is FlatHostView && findRecyclerView(host) != null
                )
            if (shouldScroll) {
                measureScroll(host) { avg, drops -> finish(avg, drops) }
            } else {
                finish(null, null)
            }
        }
    }

    private fun measureScroll(host: View, onDone: (Double?, Int?) -> Unit) {
        val rv = findRecyclerView(host)
        if (rv == null) {
            onDone(null, null)
            return
        }
        val frameDeltas = mutableListOf<Long>()
        var lastFrameNs = 0L
        var dropped = 0
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameNs != 0L) {
                    val delta = frameTimeNanos - lastFrameNs
                    frameDeltas.add(delta)
                    if (delta > DROP_THRESHOLD_NS) dropped++
                }
                lastFrameNs = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(callback)
        rv.fling(0, 12_000)
        mainHandler.postDelayed({
            Choreographer.getInstance().removeFrameCallback(callback)
            if (frameDeltas.isEmpty()) {
                onDone(null, dropped)
            } else {
                val avgMs = frameDeltas.map { Timing.nsToMs(it) }.average()
                onDone(avgMs, dropped)
            }
        }, SCROLL_MS)
    }

    private fun findRecyclerView(view: View): RecyclerView? {
        if (view is RecyclerView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findRecyclerView(view.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }

    private data class Sample(
        val measureMs: Double,
        val layoutMs: Double,
        val drawMs: Double,
        val firstFrameMs: Double,
        val viewCount: Int,
        val maxViewDepth: Int,
    )
}
