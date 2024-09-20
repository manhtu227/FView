package com.demo.jsontoview.pattern

import PaddingConfig
import ViewTypeConfig
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec
import androidx.core.view.marginRight
import androidx.recyclerview.widget.RecyclerView
import com.demo.jsontoview.CustomViewGroup2
import com.demo.jsontoview.FTree
import com.demo.jsontoview.ViewGroupType
import kotlin.math.min


interface LayoutStrategy {
    fun layout(fView: FTree, width: Int, height: Int): Pair<Int, Int>

    fun measureChildren(
        parent: FTree,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    )

}


class VerticalLayoutStrategy : LayoutStrategy {
    private var view: View? = null
    override fun layout(
        fView: FTree,
        width: Int,
        height: Int,
    ): Pair<Int, Int> {
        var currentOffset = 0
        var height1 = height

        fView.children.forEachIndexed { index, child ->
            if (child.viewType == ViewTypeConfig.ViewGroup) {
                child.setParent(fView)
                child.layout(0, currentOffset, width, height1)
                height1 += child.totalHeight
                currentOffset += child.totalHeight
            } else {
                view = fView.pendingViews!![index]
                val indexView = fView.customViewGroup?.indexOfChild(view)
                if (indexView != null && indexView != -1) {
                    fView.customViewGroup?.getChildAt(indexView)?.let { it ->
                        it.layout(
                            width,
                            height1 ,
                            width + it.measuredWidth,
                            height1 + it.measuredHeight
                        )
                        height1 += it.measuredHeight
                        currentOffset += it.measuredHeight
                    }
                }
            }
        }

        return Pair(0, currentOffset)

    }


    override fun measureChildren(
        parent: FTree,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {

        parent.children.forEachIndexed { index, child ->
            if (child.viewType == ViewTypeConfig.ViewGroup) {
                child.setCustomViewGroup(parent.customViewGroup!!, parent.context!!)
                child.measure(widthMeasureSpec, heightMeasureSpec)
                parent.totalHeight += child.totalHeight
                parent.totalWidth = maxOf(
                    parent.totalWidth,
                    child.totalWidth + parent.props.padding.left + parent.props.padding.right
                )
            } else {
                Log.e(
                    "VerticalLayoutStrategy",
                    "measureChildren: ${parent.pendingViews} ${parent.pendingViews!!.containsKey(index)}"
                )
                view = if (parent.pendingViews!!.containsKey(index)) parent.pendingViews!![index]
                else {
                    val item = ViewGroupType().recyclerView(parent.context!!, child)
                    parent.customViewGroup?.addView(item)
                    parent.pendingViews!![index] = item
                    item
                }
                // get key of view
                parent.customViewGroup?.measureChildPublic(
                    view!!, widthMeasureSpec, heightMeasureSpec
                )
                parent.totalHeight += view!!.measuredHeight
            }


        }

        // for looop oarent.customViewGroup.childCount

    }


}

class HorizontalLayoutStrategy : LayoutStrategy {
    override fun layout(
        fView: FTree,
        width: Int,
        height: Int,
    ): Pair<Int, Int> {
        var currentOffset = 0
        fView.children.forEach { child ->
            child.setParent(fView)
            child.layout(currentOffset, 0, width, height)
            currentOffset += child.totalWidth + fView.props.gap
        }
        return Pair(currentOffset, 0)

    }


    override fun measureChildren(
        parent: FTree,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        parent.children.forEachIndexed { index, child ->
            child.setCustomViewGroup(parent.customViewGroup!!, parent.context!!)

            child.measure(widthMeasureSpec, heightMeasureSpec)

            if (parent.widthMode != MeasureSpec.EXACTLY) {
                parent.totalWidth += child.totalWidth
            }

            parent.totalHeight = maxOf(parent.totalHeight, child.totalHeight)
        }
    }


}

class StackLayoutStrategy : LayoutStrategy {
    override fun layout(
        fView: FTree,
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
        parent: FTree,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        parent.children.forEach { child ->
            child.setCustomViewGroup(parent.customViewGroup!!, parent.context!!)
            child.measure(widthMeasureSpec, heightMeasureSpec)

            parent.totalWidth = maxOf(parent.totalWidth, child.totalWidth)
            parent.totalHeight = maxOf(parent.totalHeight, child.totalHeight)
        }
    }

}

