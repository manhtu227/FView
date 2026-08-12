package com.manhtu.jsontoview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.manhtu.jsontoview.flat.FlatHostView
import com.manhtu.jsontoview.model.FNode
import com.manhtu.jsontoview.nested.NestedHost
import com.manhtu.jsontoview.parse.JsonTreeParser

/**
 * Primary SDK entry: one host that can render an [FNode] tree with either backend.
 *
 * ```
 * val host = JsonToViewHost(context).apply {
 *     mode = JsonToViewHost.Mode.FLAT
 *     renderConfig = RenderConfig(imageLoader = myLoader, actionHandler = { n, a -> … })
 *     bindJson(json)
 * }
 * ```
 */
class JsonToViewHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    enum class Mode {
        FLAT,
        NESTED,
    }

    var mode: Mode = Mode.FLAT
        set(value) {
            if (field == value) return
            field = value
            rebuildHost()
            pendingRoot?.let { bind(it) }
        }

    var renderConfig: RenderConfig = RenderConfig()
        set(value) {
            field = value
            applyConfigToHost()
            pendingRoot?.let { bind(it) }
        }

    private var activeHost: View? = null
    private var pendingRoot: FNode? = null

    init {
        rebuildHost()
    }

    fun bind(root: FNode) {
        pendingRoot = root
        applyConfigToHost()
        when (val host = activeHost) {
            is FlatHostView -> host.bind(root)
            is NestedHost -> host.bind(root)
        }
        requestLayout()
        invalidate()
    }

    fun bindJson(json: String) {
        bind(JsonTreeParser.parse(json))
    }

    fun clear() {
        pendingRoot = null
        when (val host = activeHost) {
            is FlatHostView -> host.clearTree()
            is NestedHost -> host.clearTree()
        }
    }

    fun underlyingHost(): View? = activeHost

    val lastMeasureNs: Long
        get() = when (val host = activeHost) {
            is FlatHostView -> host.lastMeasureNs
            is NestedHost -> host.lastMeasureNs
            else -> 0L
        }

    val lastLayoutNs: Long
        get() = when (val host = activeHost) {
            is FlatHostView -> host.lastLayoutNs
            is NestedHost -> host.lastLayoutNs
            else -> 0L
        }

    val lastDrawNs: Long
        get() = when (val host = activeHost) {
            is FlatHostView -> host.lastDrawNs
            is NestedHost -> host.lastDrawNs
            else -> 0L
        }

    private fun applyConfigToHost() {
        when (val host = activeHost) {
            is FlatHostView -> host.renderConfig = renderConfig
            is NestedHost -> host.renderConfig = renderConfig
        }
    }

    private fun rebuildHost() {
        removeAllViews()
        val host: View = when (mode) {
            Mode.FLAT -> FlatHostView(context)
            Mode.NESTED -> NestedHost(context)
        }
        activeHost = host
        applyConfigToHost()
        addView(
            host,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
    }
}

object JsonToView {
    fun parse(json: String): FNode = JsonTreeParser.parse(json)

    fun parseTree(json: String, name: String = "tree") =
        JsonTreeParser.parseTreeSpec(json, name)
}
