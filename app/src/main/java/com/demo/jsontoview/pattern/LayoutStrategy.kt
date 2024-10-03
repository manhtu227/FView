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
    val fView: FView
    fun layoutChildren(left: Int, top: Int, indexComponent: Int)
    fun measureChildren(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        indexComponent: Int,
    )
}


class VerticalLayoutStrategy(override val fView: FView) : LayoutStrategy {
    override fun layoutChildren(left: Int, top: Int, indexComponent: Int) {
        var topExpect = top
        val gap = fView.props.gap ?: fView.gapJustifyContent
        var indexC = indexComponent

        fView.children.forEach { child ->
            if (child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true) {
                child.setParent(fView)
                child.layout(left, topExpect, indexC)
                topExpect += child.measureHeight + gap
            } else {
//                val index = fView.customViewGroup!!.pendingIdViews.indexOf(child.props.id!!)
                val view = fView.customViewGroup!!.getChildAt(indexC)
                indexC++
                view?.let { it ->
                    it.layout(
                        left, topExpect, left + it.measuredWidth, topExpect + it.measuredHeight
                    )
                    topExpect += it.measuredHeight + gap
                }
            }
        }
    }


    override fun measureChildren(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        indexComponent: Int,
    ) {
        var height =
            fView.props.padding.top + fView.props.padding.bottom + fView.props.margin.top + fView.props.margin.bottom
        val totalGap = (fView.props.gap
            ?: 0) * (if (fView.children.size - 1 > 0) fView.children.size - 1 else 0)
        var index = indexComponent
        height += totalGap

        fView.children.forEach { child ->
            if (child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true) {
                child.measure(widthMeasureSpec, heightMeasureSpec, index)
                height += child.measureHeight
                fView.measureWidth = maxOf(
                    fView.measureWidth,
                    child.measureWidth + fView.props.padding.left + fView.props.padding.right
                )
            } else {
//                val index = fView.customViewGroup!!.pendingIdViews.indexOf(child.props.id!!)
                val view = fView.customViewGroup!!.getChildAt(index)
                index++
                // get key of view
                fView.customViewGroup?.measureChildPublic(
                    view!!, widthMeasureSpec, heightMeasureSpec
                )
                height += view!!.measuredHeight
            }

        }

        fView.measureHeight = max(fView.measureHeight, height)

    }


}

class HorizontalLayoutStrategy(override val fView: FView) : LayoutStrategy {
    override fun layoutChildren(left: Int, top: Int, indexComponent: Int) {
        var leftExpect = left
        var index = indexComponent

        fView.children.forEach { child ->
            val gap = fView.props.gap ?: 0

            if (child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true) {
                child.setParent(fView)
                child.layout(leftExpect, top, index)
                leftExpect += child.measureWidth + gap
            } else {
//                val indexView = fView.customViewGroup!!.pendingIdViews.indexOf(child.props.id!!)
                val view = fView.customViewGroup!!.getChildAt(index)
                index++
                view?.let { it ->
                    it.layout(
                        leftExpect, top, leftExpect + it.measuredWidth, top + it.measuredHeight
                    )
                    leftExpect += it.measuredWidth + gap
                }
            }
        }

    }


    override fun measureChildren(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        indexComponent: Int,
    ) {
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val gap = (fView.props.gap ?: 0)
        var index = indexComponent

        fView.children.forEach { child ->
            var childWidthExpect = widthMeasureSpec
            var height = 0
            var width = 0

            if (child.props.width.value == ViewGroup.LayoutParams.MATCH_PARENT || child.props.drawable?.type == TypeConfig.Text) {
                childWidthExpect = MeasureSpec.makeMeasureSpec(widthSize - gap, widthMode)
            }

            if (child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true) {
                child.measure(childWidthExpect, heightMeasureSpec,index)
                widthSize -= child.measureWidth
                width = child.measureWidth
                height =
                    child.measureHeight + fView.props.margin.top + fView.props.margin.bottom + fView.props.padding.top + fView.props.padding.bottom
            } else {
//                val indexView = fView.customViewGroup!!.pendingIdViews.indexOf(child.props.id!!)
                val view = fView.customViewGroup!!.getChildAt(index)
                index++

                // get key of view
                fView.customViewGroup?.measureChildPublic(
                    view!!, childWidthExpect, heightMeasureSpec
                )
                widthSize -= view!!.measuredWidth
                width = view.measuredWidth
                height =
                    view.measuredHeight + fView.props.margin.top + fView.props.margin.bottom + fView.props.padding.top + fView.props.padding.bottom
            }
            if (fView.props.width.value != ViewGroup.LayoutParams.MATCH_PARENT) {
                fView.measureWidth += width + gap
            }

            fView.measureHeight = maxOf(fView.measureHeight, height)
        }

    }


}

class StackLayoutStrategy(override val fView: FView) : LayoutStrategy {
    override fun layoutChildren(left: Int, top: Int, indexComponent: Int) {
        fView.children.forEach { child ->
            child.setParent(fView)
            child.layout(left, top, indexComponent)
        }
    }


    override fun measureChildren(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        indexComponent: Int,
    ) {
        fView.children.forEach { child ->
            child.measure(widthMeasureSpec, heightMeasureSpec, indexComponent)

            fView.measureWidth = maxOf(fView.measureWidth, child.measureWidth)
            fView.measureHeight = maxOf(fView.measureHeight, child.measureHeight)
        }
    }

}

