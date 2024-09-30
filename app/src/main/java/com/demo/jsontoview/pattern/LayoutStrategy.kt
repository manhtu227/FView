package com.demo.jsontoview.pattern

import TypeConfig
import ViewTypeConfig
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import com.demo.jsontoview.FView
import com.demo.jsontoview.ViewGroupType
import kotlin.math.max


interface LayoutStrategy {
    fun layoutChildren(fView: FView, width: Int, height: Int): Pair<Int, Int>
    fun measureChildren(
        fView: FView,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    )
}


class VerticalLayoutStrategy : LayoutStrategy {
    private var view: View? = null
    override fun layoutChildren(
        fView: FView,
        width: Int,
        height: Int,
    ): Pair<Int, Int> {
        var currentOffset = 0
        var height1 = height
        val gap = fView.props.gap ?: fView.gapJustifyContent

        fView.children.forEach { child ->
            if (child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true) {
                child.setParent(fView)
                child.layout(0, currentOffset, width, height1)
                height1 += child.measureHeight + gap
                currentOffset += child.measureHeight + gap
            } else {
                view = fView.customViewGroup!!.pendingViews[child.props.id!!]
                val indexView = fView.customViewGroup?.indexOfChild(view)
                if (indexView != null && indexView != -1) {
                    fView.customViewGroup?.getChildAt(indexView)?.let { it ->
                        it.layout(
                            width,
                            height1,
                            width + it.measuredWidth,
                            height1 + it.measuredHeight
                        )
                        height1 += it.measuredHeight + gap
                        currentOffset += it.measuredHeight + gap
                    }
                }
            }
        }

        return Pair(0, currentOffset)

    }


    override fun measureChildren(
        parent: FView,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        var height =
            parent.props.padding.top + parent.props.padding.bottom + parent.props.margin.top + parent.props.margin.bottom
        val totalGap = (parent.props.gap
            ?: 0) * (if (parent.children.size - 1 > 0) parent.children.size - 1 else 0)
        height += totalGap

        parent.children.forEach { child ->
            Log.e(
                "VerticalLayoutStrategy",
                "measureChildren11111: ${child.viewType} ${child.props.isComponent} ${parent.measureHeight} ${height}"
            )
            if (child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true) {
                child.measure(widthMeasureSpec, heightMeasureSpec)
                height += child.measureHeight
                parent.measureWidth = maxOf(
                    parent.measureWidth,
                    child.measureWidth + parent.props.padding.left + parent.props.padding.right
                )
            } else {

                view =
                    if (parent.customViewGroup!!.pendingViews.containsKey(child.props.id!!)) parent.customViewGroup!!.pendingViews[child.props.id]
                    else {
                        val item = ViewGroupType().getView(parent.customViewGroup!!.context!!, child)
                        parent.customViewGroup?.addView(item)
                        parent.customViewGroup!!.pendingViews[child.props.id] = item
                        item
                    }
                // get key of view
                parent.customViewGroup?.measureChildPublic(
                    view!!, widthMeasureSpec, heightMeasureSpec
                )
                height += view!!.measuredHeight
            }

        }
        Log.e("VerticalLayoutStrategy", "measureChildren: ${parent.measureHeight} ${height}")

        parent.measureHeight = max(parent.measureHeight, height)
        // for looop oarent.customViewGroup.childCount

    }


}

class HorizontalLayoutStrategy : LayoutStrategy {
    override fun layoutChildren(
        fView: FView,
        width: Int,
        height: Int,
    ): Pair<Int, Int> {
        var currentOffset = 0
        var width1 = width

        fView.children.forEach { child ->
            val gap = fView.props.gap ?: 0

            if (child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true) {
                child.setParent(fView)
                child.layout(currentOffset, 0, width1, height)
                width1 += child.measureWidth + gap
                currentOffset += child.measureWidth + gap
            } else {
                val view = fView.customViewGroup!!.pendingViews[child.props.id]

                val indexView = fView.customViewGroup?.indexOfChild(view)
                if (indexView != null && indexView != -1) {
                    fView.customViewGroup?.getChildAt(indexView)?.let { it ->
                        it.layout(
                            width1,
                            height,
                            width1 + it.measuredWidth,
                            height + it.measuredHeight
                        )
                        width1 += it.measuredWidth + gap
                        currentOffset += it.measuredWidth + gap
                    }
                }
            }
        }
        return Pair(currentOffset, 0)

    }


    override fun measureChildren(
        parent: FView,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        Log.e("HorizontalLayoutStrategy", "widthSize: $widthSize")
        var gap = (parent.props.gap ?: 0)
        parent.children.forEachIndexed { index, child ->
            var childWidthExpect = widthMeasureSpec
            var height = 0
            var width = 0

            if (child.props.width.value == ViewGroup.LayoutParams.MATCH_PARENT || child.props.drawable?.type == TypeConfig.Text) {
                Log.e("HorizontalLayoutStrategy", "widthSize11: $widthSize")
                childWidthExpect = MeasureSpec.makeMeasureSpec(widthSize - gap, widthMode)
            }

            if (child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true) {
                child.measure(childWidthExpect, heightMeasureSpec)
                widthSize -= child.measureWidth
                width = child.measureWidth
                height =
                    child.measureHeight + parent.props.margin.top + parent.props.margin.bottom + parent.props.padding.top + parent.props.padding.bottom
            } else {
                val view =
                    if (parent.customViewGroup!!.pendingViews.containsKey(child.props.id!!)) parent.customViewGroup!!.pendingViews[child.props.id]
                    else {
                        val item = ViewGroupType().getView(parent.customViewGroup!!.context!!, child)
                        parent.customViewGroup?.addView(item)
                        parent.customViewGroup!!.pendingViews[child.props.id] = item
                        item
                    }

                // get key of view
                parent.customViewGroup?.measureChildPublic(
                    view!!, childWidthExpect, heightMeasureSpec
                )
                widthSize -= view!!.measuredWidth
                width = view.measuredWidth
                height =
                    view.measuredHeight + parent.props.margin.top + parent.props.margin.bottom + parent.props.padding.top + parent.props.padding.bottom
            }
            if (parent.props.width.value != ViewGroup.LayoutParams.MATCH_PARENT) {
                parent.measureWidth += width + gap
            }

            parent.measureHeight = maxOf(parent.measureHeight, height)
        }

//        if (parent.props.width.value != ViewGroup.LayoutParams.MATCH_PARENT) {
//            parent.measureWidth += gap
//        }

    }


}

class StackLayoutStrategy : LayoutStrategy {
    override fun layoutChildren(
        fView: FView,
        width: Int,
        height: Int,
    ): Pair<Int, Int> {
        fView.children.forEach { child ->
            child.setParent(fView)
            child.layout(0, 0, width, height)
        }
        return Pair(0, 0)
    }


    override fun measureChildren(
        parent: FView,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        parent.children.forEach { child ->
            child.measure(widthMeasureSpec, heightMeasureSpec)

            parent.measureWidth = maxOf(parent.measureWidth, child.measureWidth)
            parent.measureHeight = maxOf(parent.measureHeight, child.measureHeight)
        }
    }

}

