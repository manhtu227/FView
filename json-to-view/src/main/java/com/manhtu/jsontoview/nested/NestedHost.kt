package com.manhtu.jsontoview.nested

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.manhtu.jsontoview.model.FNode

/**
 * Container that holds a Nested [FNode] tree built by [NestedTreeBuilder].
 */
class NestedHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    var lastMeasureNs: Long = 0L
        private set
    var lastLayoutNs: Long = 0L
        private set
    var lastDrawNs: Long = 0L
        private set

    fun bind(root: FNode) {
        clearTree()
        val tree = NestedTreeBuilder.build(context, root)
        val lp = tree.layoutParams as? LayoutParams
            ?: LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        if (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT && root.kind.name == "LIST") {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        addView(tree, lp)
        requestLayout()
        invalidate()
    }

    fun clearTree() {
        removeAllViews()
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val t0 = System.nanoTime()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        lastMeasureNs = System.nanoTime() - t0
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val t0 = System.nanoTime()
        super.onLayout(changed, left, top, right, bottom)
        lastLayoutNs = System.nanoTime() - t0
    }

    override fun dispatchDraw(canvas: android.graphics.Canvas) {
        val t0 = System.nanoTime()
        super.dispatchDraw(canvas)
        lastDrawNs = System.nanoTime() - t0
    }
}
