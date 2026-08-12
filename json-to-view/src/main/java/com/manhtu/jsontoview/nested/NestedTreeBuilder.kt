package com.manhtu.jsontoview.nested

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.manhtu.jsontoview.RenderConfig
import com.manhtu.jsontoview.image.ImageTarget
import com.manhtu.jsontoview.model.Dimension
import com.manhtu.jsontoview.model.FNode
import com.manhtu.jsontoview.model.NodeKind
import com.manhtu.jsontoview.util.Dimens

/**
 * Builds a real Android [View] tree from an [FNode] model.
 * Every node allocates ≥1 View so hierarchy depth tracks tree depth.
 */
object NestedTreeBuilder {

    fun build(
        context: Context,
        node: FNode,
        config: RenderConfig = RenderConfig(),
    ): View {
        val view = when (node.kind) {
            NodeKind.ROW -> buildLinear(context, node, LinearLayout.HORIZONTAL, config)
            NodeKind.COLUMN -> buildLinear(context, node, LinearLayout.VERTICAL, config)
            NodeKind.STACK -> buildStack(context, node, config)
            NodeKind.TEXT -> buildText(context, node)
            NodeKind.BOX -> buildBox(context, node, config)
            NodeKind.LIST -> buildList(context, node, config)
        }
        applyPadding(view, node)
        if (node.props.imageUrl.isNullOrBlank()) {
            applyBackground(view, node)
        }
        applyAction(view, node, config)
        node.props.contentDescription?.let { view.contentDescription = it }
        view.layoutParams = createLayoutParams(context, node, view)
        return view
    }

    private fun buildLinear(
        context: Context,
        node: FNode,
        orientation: Int,
        config: RenderConfig,
    ): LinearLayout {
        val layout = LinearLayout(context).apply {
            this.orientation = orientation
        }
        val gap = node.props.gap
        node.children.forEachIndexed { index, child ->
            val childView = build(context, child, config)
            val lp = childView.layoutParams as? ViewGroup.MarginLayoutParams
                ?: ViewGroup.MarginLayoutParams(childView.layoutParams)
            if (index > 0 && gap > 0) {
                if (orientation == LinearLayout.HORIZONTAL) {
                    lp.leftMargin += Dimens.dp(context, gap)
                } else {
                    lp.topMargin += Dimens.dp(context, gap)
                }
            }
            childView.layoutParams = lp
            layout.addView(childView)
        }
        return layout
    }

    private fun buildStack(context: Context, node: FNode, config: RenderConfig): FrameLayout {
        val layout = FrameLayout(context)
        for (child in node.children) {
            layout.addView(build(context, child, config))
        }
        return layout
    }

    private fun buildText(context: Context, node: FNode): TextView {
        val p = node.props
        return TextView(context).apply {
            text = p.text.orEmpty()
            textSize = p.textSizeSp
            setTextColor(p.textColor)
        }
    }

    private fun buildBox(context: Context, node: FNode, config: RenderConfig): View {
        val url = node.props.imageUrl
        if (!url.isNullOrBlank()) {
            val imageView = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(node.props.backgroundColor ?: config.imagePlaceholderColor)
            }
            val loader = config.imageLoader
            if (loader != null) {
                val target = object : ImageTarget {
                    override fun onSuccess(drawable: Drawable) {
                        imageView.setImageDrawable(drawable)
                        imageView.background = null
                    }

                    override fun onError() {
                        // keep placeholder background
                    }
                }
                imageView.tag = target
                loader.load(url, target)
            }
            return imageView
        }
        return View(context)
    }

    private fun buildList(context: Context, node: FNode, config: RenderConfig): RecyclerView {
        return RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = NestedListAdapter(node.children, config)
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun applyAction(view: View, node: FNode, config: RenderConfig) {
        val action = node.props.action ?: return
        val handler = config.actionHandler ?: return
        view.isClickable = true
        view.setOnClickListener { handler.onAction(node, action) }
    }

    private fun applyPadding(view: View, node: FNode) {
        val p = node.props.padding
        view.setPadding(
            Dimens.dp(view.context, p.left),
            Dimens.dp(view.context, p.top),
            Dimens.dp(view.context, p.right),
            Dimens.dp(view.context, p.bottom),
        )
    }

    private fun applyBackground(view: View, node: FNode) {
        val color = node.props.backgroundColor ?: return
        val radius = node.props.cornerRadius * view.resources.displayMetrics.density
        view.background = GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    private fun createLayoutParams(
        context: Context,
        node: FNode,
        view: View,
    ): ViewGroup.MarginLayoutParams {
        val density = context.resources.displayMetrics.density
        val w = toLayoutParamSize(node.props.width, density)
        val h = toLayoutParamSize(node.props.height, density)
        val lp = when (view) {
            is LinearLayout -> LinearLayout.LayoutParams(w, h)
            is FrameLayout -> FrameLayout.LayoutParams(w, h)
            is RecyclerView -> RecyclerView.LayoutParams(w, h)
            is ImageView -> ViewGroup.MarginLayoutParams(w, h)
            else -> ViewGroup.MarginLayoutParams(w, h)
        }
        val m = node.props.margin
        lp.setMargins(
            Dimens.dp(context, m.left),
            Dimens.dp(context, m.top),
            Dimens.dp(context, m.right),
            Dimens.dp(context, m.bottom),
        )
        return lp
    }

    private fun toLayoutParamSize(dim: Dimension, density: Float): Int {
        return when (dim.value) {
            -1 -> ViewGroup.LayoutParams.MATCH_PARENT
            -2 -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> Dimens.resolve(dim, parentSize = 0, density = density).let {
                if (dim.value > 0 && it == 0 && dim.unit.name == "PERCENT") {
                    ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    it
                }
            }
        }
    }

    private class NestedListAdapter(
        private val items: List<FNode>,
        private val config: RenderConfig,
    ) : RecyclerView.Adapter<NestedListAdapter.Holder>() {

        class Holder(val root: View) : RecyclerView.ViewHolder(root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val placeholder = FrameLayout(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
            return Holder(placeholder)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val container = holder.root as FrameLayout
            container.removeAllViews()
            val child = build(container.context, items[position], config)
            val lp = child.layoutParams as? ViewGroup.MarginLayoutParams
                ?: ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
            container.addView(child, lp)
        }

        override fun getItemCount(): Int = items.size
    }
}
