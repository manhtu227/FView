package com.manhtu.jsontoview.flat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.manhtu.jsontoview.RenderConfig
import com.manhtu.jsontoview.image.ImageTarget
import com.manhtu.jsontoview.model.Dimension
import com.manhtu.jsontoview.model.FNode
import com.manhtu.jsontoview.model.NodeKind
import com.manhtu.jsontoview.util.Dimens
import kotlin.math.max
import kotlin.math.min

/**
 * Flat canvas host: single [FrameLayout] that measure/layout/draws an [FNode] tree.
 * LIST roots embed a [RecyclerView] with one [FlatHostView] per item.
 */
class FlatHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private var root: FNode? = null
    private var layoutRoot: FlatLayoutNode? = null
    private var isListMode = false
    private var bindGeneration = 0
    private val pendingTargets = mutableListOf<ImageTarget>()

    var renderConfig: RenderConfig = RenderConfig()

    var lastMeasureNs: Long = 0L
        private set
    var lastLayoutNs: Long = 0L
        private set
    var lastDrawNs: Long = 0L
        private set

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val tmpRect = RectF()
    private val density = resources.displayMetrics.density

    init {
        setWillNotDraw(false)
        isClickable = true
    }

    fun bind(root: FNode) {
        cancelPendingLoads()
        bindGeneration++
        this.root = root
        layoutRoot = null
        isListMode = root.kind == NodeKind.LIST
        removeAllViews()
        if (isListMode) {
            setWillNotDraw(true)
            val rv = RecyclerView(context).apply {
                layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = FlatListAdapter(root.children, renderConfig)
                overScrollMode = OVER_SCROLL_NEVER
            }
            addView(rv)
        } else {
            setWillNotDraw(false)
        }
        requestLayout()
        invalidate()
    }

    fun clearTree() {
        cancelPendingLoads()
        bindGeneration++
        root = null
        layoutRoot = null
        isListMode = false
        removeAllViews()
        requestLayout()
        invalidate()
    }

    private fun cancelPendingLoads() {
        val loader = renderConfig.imageLoader
        pendingTargets.forEach { loader?.cancel(it) }
        pendingTargets.clear()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val t0 = System.nanoTime()
        if (isListMode) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            lastMeasureNs = System.nanoTime() - t0
            return
        }
        val node = root
        if (node == null) {
            setMeasuredDimension(
                resolveSize(0, widthMeasureSpec),
                resolveSize(0, heightMeasureSpec),
            )
            lastMeasureNs = System.nanoTime() - t0
            return
        }
        val parentW = MeasureSpec.getSize(widthMeasureSpec)
        val parentH = MeasureSpec.getSize(heightMeasureSpec)
        val measured = measureNode(node, parentW, parentH, widthMeasureSpec, heightMeasureSpec)
        layoutRoot = measured
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val finalW = when (wMode) {
            MeasureSpec.EXACTLY -> parentW
            MeasureSpec.AT_MOST -> min(measured.w, parentW)
            else -> measured.w
        }
        val finalH = when (hMode) {
            MeasureSpec.EXACTLY -> parentH
            MeasureSpec.AT_MOST -> min(measured.h, parentH)
            else -> measured.h
        }
        setMeasuredDimension(finalW, finalH)
        lastMeasureNs = System.nanoTime() - t0
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val t0 = System.nanoTime()
        if (isListMode) {
            super.onLayout(changed, left, top, right, bottom)
            lastLayoutNs = System.nanoTime() - t0
            return
        }
        layoutRoot?.let { rootLn ->
            rootLn.x = 0
            rootLn.y = 0
            layoutChildren(rootLn)
            requestImages(rootLn)
        }
        lastLayoutNs = System.nanoTime() - t0
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isListMode) return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            val handler = renderConfig.actionHandler
            val hit = layoutRoot?.let { findHit(it, event.x.toInt(), event.y.toInt()) }
            val action = hit?.node?.props?.action
            if (handler != null && hit != null && action != null) {
                handler.onAction(hit.node, action)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findHit(ln: FlatLayoutNode, x: Int, y: Int): FlatLayoutNode? {
        // Children first (top-most)
        for (i in ln.children.indices.reversed()) {
            findHit(ln.children[i], x, y)?.let { return it }
        }
        val inside = x >= ln.x && x < ln.x + ln.w && y >= ln.y && y < ln.y + ln.h
        return if (inside && ln.node.props.action != null) ln else null
    }

    private fun requestImages(ln: FlatLayoutNode) {
        val url = ln.node.props.imageUrl
        val loader = renderConfig.imageLoader
        if (!url.isNullOrBlank() && loader != null && ln.imageDrawable == null) {
            val gen = bindGeneration
            val target = object : ImageTarget {
                override fun onSuccess(drawable: Drawable) {
                    if (gen != bindGeneration) return
                    ln.imageDrawable = drawable
                    invalidate()
                }

                override fun onError() {
                    // keep placeholder path
                }
            }
            pendingTargets.add(target)
            loader.load(url, target)
        }
        ln.children.forEach { requestImages(it) }
    }

    override fun onDraw(canvas: Canvas) {
        val t0 = System.nanoTime()
        super.onDraw(canvas)
        if (!isListMode) {
            layoutRoot?.let { drawNode(canvas, it) }
        }
        lastDrawNs = System.nanoTime() - t0
    }

    private fun measureNode(
        node: FNode,
        parentW: Int,
        parentH: Int,
        widthSpec: Int,
        heightSpec: Int,
    ): FlatLayoutNode {
        val p = node.props
        val padL = Dimens.dp(context, p.padding.left)
        val padT = Dimens.dp(context, p.padding.top)
        val padR = Dimens.dp(context, p.padding.right)
        val padB = Dimens.dp(context, p.padding.bottom)
        val marL = Dimens.dp(context, p.margin.left)
        val marT = Dimens.dp(context, p.margin.top)
        val marR = Dimens.dp(context, p.margin.right)
        val marB = Dimens.dp(context, p.margin.bottom)
        val gap = Dimens.dp(context, p.gap)

        val availW = max(0, parentW - marL - marR)
        val availH = max(0, parentH - marT - marB)

        val resolvedW = Dimens.resolve(p.width, availW, density)
        val resolvedH = Dimens.resolve(p.height, availH, density)

        val ln = FlatLayoutNode(node)

        when (node.kind) {
            NodeKind.TEXT -> {
                val text = p.text.orEmpty()
                textPaint.textSize = p.textSizeSp * density
                textPaint.color = p.textColor
                val maxTextW = if (resolvedW > 0) max(0, resolvedW - padL - padR) else availW - padL - padR
                val layout = buildStaticLayout(text, max(0, maxTextW))
                val contentW = if (resolvedW == Dimension.WRAP.value || resolvedW < 0 && p.width.value == -2) {
                    layout.getLineWidth(0).toInt().coerceAtLeast(if (text.isEmpty()) 0 else layout.width)
                        .let { if (layout.lineCount > 0) {
                            (0 until layout.lineCount).maxOf { i -> layout.getLineWidth(i).toInt() }
                        } else 0 }
                } else if (resolvedW >= 0 && p.width.value != -2) {
                    max(0, resolvedW - padL - padR)
                } else {
                    layout.width
                }
                val contentH = layout.height
                ln.w = if (p.width.value == -1) availW
                else if (p.width.value == -2) contentW + padL + padR
                else resolvedW
                ln.h = if (p.height.value == -1) availH
                else if (p.height.value == -2) contentH + padT + padB
                else resolvedH
                ln.w = max(ln.w, padL + padR)
                ln.h = max(ln.h, padT + padB)
            }

            NodeKind.BOX -> {
                val minBox = Dimens.dp(context, 16)
                ln.w = when {
                    p.width.value == -1 -> availW
                    p.width.value == -2 -> minBox + padL + padR
                    else -> resolvedW
                }
                ln.h = when {
                    p.height.value == -1 -> availH
                    p.height.value == -2 -> minBox + padT + padB
                    else -> resolvedH
                }
            }

            NodeKind.ROW, NodeKind.COLUMN, NodeKind.STACK, NodeKind.LIST -> {
                val innerW = when {
                    p.width.value == -1 -> max(0, availW - padL - padR)
                    p.width.value == -2 -> Int.MAX_VALUE / 4
                    else -> max(0, resolvedW - padL - padR)
                }
                val innerH = when {
                    p.height.value == -1 -> max(0, availH - padT - padB)
                    p.height.value == -2 -> Int.MAX_VALUE / 4
                    else -> max(0, resolvedH - padT - padB)
                }

                val childNodes = node.children.map { child ->
                    measureNode(
                        child,
                        parentW = if (node.kind == NodeKind.ROW) Int.MAX_VALUE / 4 else innerW,
                        parentH = if (node.kind == NodeKind.COLUMN) Int.MAX_VALUE / 4 else innerH,
                        widthSpec = widthSpec,
                        heightSpec = heightSpec,
                    )
                }
                ln.children.addAll(childNodes)

                val contentW: Int
                val contentH: Int
                when (node.kind) {
                    NodeKind.ROW -> {
                        contentW = childNodes.sumOf { it.w } + gap * max(0, childNodes.size - 1)
                        contentH = childNodes.maxOfOrNull { it.h } ?: 0
                    }
                    NodeKind.COLUMN, NodeKind.LIST -> {
                        contentW = childNodes.maxOfOrNull { it.w } ?: 0
                        contentH = childNodes.sumOf { it.h } + gap * max(0, childNodes.size - 1)
                    }
                    NodeKind.STACK -> {
                        contentW = childNodes.maxOfOrNull { it.w } ?: 0
                        contentH = childNodes.maxOfOrNull { it.h } ?: 0
                    }
                    else -> {
                        contentW = 0
                        contentH = 0
                    }
                }

                ln.w = when {
                    p.width.value == -1 -> availW
                    p.width.value == -2 -> contentW + padL + padR
                    else -> resolvedW
                }
                ln.h = when {
                    p.height.value == -1 -> availH
                    p.height.value == -2 -> contentH + padT + padB
                    else -> resolvedH
                }
            }
        }

        // Outer size already includes padding for content; margins applied by parent layout.
        return ln
    }

    private fun layoutChildren(parent: FlatLayoutNode) {
        val p = parent.node.props
        val padL = Dimens.dp(context, p.padding.left)
        val padT = Dimens.dp(context, p.padding.top)
        val gap = Dimens.dp(context, p.gap)
        var cursorX = parent.x + padL
        var cursorY = parent.y + padT

        parent.children.forEachIndexed { index, child ->
            val marL = Dimens.dp(context, child.node.props.margin.left)
            val marT = Dimens.dp(context, child.node.props.margin.top)
            when (parent.node.kind) {
                NodeKind.ROW -> {
                    if (index > 0) cursorX += gap
                    child.x = cursorX + marL
                    child.y = cursorY + marT
                    cursorX += child.w + Dimens.dp(context, child.node.props.margin.right)
                }
                NodeKind.COLUMN, NodeKind.LIST -> {
                    if (index > 0) cursorY += gap
                    child.x = cursorX + marL
                    child.y = cursorY + marT
                    cursorY += child.h + Dimens.dp(context, child.node.props.margin.bottom)
                }
                NodeKind.STACK -> {
                    child.x = parent.x + padL + marL
                    child.y = parent.y + padT + marT
                }
                else -> {
                    child.x = cursorX + marL
                    child.y = cursorY + marT
                }
            }
            layoutChildren(child)
        }
    }

    private fun drawNode(canvas: Canvas, ln: FlatLayoutNode) {
        val p = ln.node.props
        tmpRect.set(ln.x.toFloat(), ln.y.toFloat(), (ln.x + ln.w).toFloat(), (ln.y + ln.h).toFloat())
        val image = ln.imageDrawable
        if (image != null) {
            image.setBounds(ln.x, ln.y, ln.x + ln.w, ln.y + ln.h)
            image.draw(canvas)
        } else if (!p.imageUrl.isNullOrBlank()) {
            fillPaint.color = p.backgroundColor ?: renderConfig.imagePlaceholderColor
            canvas.drawRect(tmpRect, fillPaint)
        } else {
            val bg = p.backgroundColor
            if (bg != null) {
                fillPaint.color = bg
                val radius = p.cornerRadius * density
                if (radius > 0f) {
                    canvas.drawRoundRect(tmpRect, radius, radius, fillPaint)
                } else {
                    canvas.drawRect(tmpRect, fillPaint)
                }
            }
        }

        if (ln.node.kind == NodeKind.TEXT) {
            val text = p.text.orEmpty()
            if (text.isNotEmpty()) {
                textPaint.textSize = p.textSizeSp * density
                textPaint.color = p.textColor
                val padL = Dimens.dp(context, p.padding.left)
                val padT = Dimens.dp(context, p.padding.top)
                val padR = Dimens.dp(context, p.padding.right)
                val maxW = max(0, ln.w - padL - padR)
                val layout = buildStaticLayout(text, maxW)
                canvas.save()
                canvas.translate((ln.x + padL).toFloat(), (ln.y + padT).toFloat())
                layout.draw(canvas)
                canvas.restore()
            }
        }

        for (child in ln.children) {
            drawNode(canvas, child)
        }
    }

    private fun buildStaticLayout(text: String, maxWidth: Int): StaticLayout {
        val width = max(0, maxWidth)
        return StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
    }

    private class FlatListAdapter(
        private val items: List<FNode>,
        private val config: RenderConfig,
    ) : RecyclerView.Adapter<FlatListAdapter.Holder>() {

        class Holder(val host: FlatHostView) : RecyclerView.ViewHolder(host)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val host = FlatHostView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
            return Holder(host)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.host.renderConfig = config
            holder.host.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }
}
