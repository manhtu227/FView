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
    fun layout(fView: FTree, children: List<FTree>): Pair<Int, Int>
    fun layoutChildComponent(
        viewGroup: CustomViewGroup2,
        left: Int,
        top: Int,
        marginPaddingRight: Int,
    )

    fun measureChildren(
        parent: FTree,
        context: Context,
        viewGroup: CustomViewGroup2,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    )

    fun measureChildrenComponent(fView: FTree, widthMeasureSpec: Int, heightMeasureSpec: Int)
}


class VerticalLayoutStrategy : LayoutStrategy {
    private var view: View? = null
    override fun layout(fView: FTree, children: List<FTree>): Pair<Int, Int> {
        var currentOffset = 0


        children.forEachIndexed { index, child ->
            if(fView.props.test=="test111") {
                Log.e("CustomViewGroup2", "layout eeeee: ${child.viewType} $index")
            }
            if (child.viewType == ViewTypeConfig.ViewGroup) {

                child.setParent(fView)
                child.layout(0, currentOffset)
                currentOffset += child.totalHeight
            } else {
                fView.customViewGroup?.addView(view)

                fView.customViewGroup?.getChildAt(fView.customViewGroup?.childCount!! - 1)
                    ?.let { it ->
                        it.layout(
                            0,
                            currentOffset,
                            it.measuredWidth,
                            currentOffset + it.measuredHeight
                        )
                        currentOffset += it.measuredHeight
                    }
            }
        }

        return Pair(0, currentOffset)

    }

    override fun layoutChildComponent(
        viewGroup: CustomViewGroup2,
        left: Int,
        top: Int,
        marginPaddingRight: Int,
    ) {
        var top = top

        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            child.layout(
                left, top, min(
                    left + child.measuredWidth,
                    Resources.getSystem().displayMetrics.widthPixels - marginPaddingRight
                ), top + child.measuredHeight
            )
            top += child.measuredHeight
        }

    }

    override fun measureChildren(
        parent: FTree,
        context: Context,
        viewGroup: CustomViewGroup2,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {

        parent.children.forEachIndexed { index, child ->
            Log.e("CustomViewGroup2", "measureChildren ne: ${child.viewType}")
            if (child.viewType == ViewTypeConfig.ViewGroup) {
                child.setCustomViewGroup(viewGroup, context)
                child.measure(widthMeasureSpec, heightMeasureSpec)
                parent.totalHeight += child.totalHeight
                parent.totalWidth = maxOf(
                    parent.totalWidth,
                    child.totalWidth + parent.props.padding.left + parent.props.padding.right
                )
            } else {
                view = ViewGroupType().recyclerView(context, child)
                parent.customViewGroup?.measureChildPublic(
                    view!!,
                    widthMeasureSpec,
                    heightMeasureSpec
                )
                parent.totalHeight += view!!.measuredHeight
            }


        }

        // for looop oarent.customViewGroup.childCount

    }

    override fun measureChildrenComponent(
        fView: FTree,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,

        ) {
        for (i in 0 until (fView.customViewGroup?.childCount ?: 0)) {
            val child = fView.customViewGroup!!.getChildAt(i)
            child.measure(widthMeasureSpec, heightMeasureSpec)
            fView.customViewGroup?.measureChildPublic(child, widthMeasureSpec, heightMeasureSpec)
            if (i == 0) {
                Log.e("CustomViewGroup2", "measureChildrenComponent: $child ${child.height}")
            }
            fView.totalHeight += child?.measuredHeight ?: 0
            fView.totalWidth = maxOf(
                fView.totalWidth,
                child.measuredWidth + fView.props.padding.left + fView.props.padding.right
            )
        }

    }
}

class HorizontalLayoutStrategy : LayoutStrategy {
    override fun layout(fView: FTree, children: List<FTree>): Pair<Int, Int> {
        var currentOffset = 0
        children.forEach { child ->
            child.setParent(fView)
            child.layout(currentOffset, 0)
            currentOffset += child.totalWidth + fView.props.gap
        }
        return Pair(currentOffset, 0)

    }

    override fun layoutChildComponent(
        viewGroup: CustomViewGroup2,
        left: Int,
        top: Int,
        marginPaddingRight: Int,
    ) {

    }

    override fun measureChildren(
        parent: FTree,
        context: Context,
        viewGroup: CustomViewGroup2,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        parent.children.forEachIndexed { index, child ->
            child.setCustomViewGroup(viewGroup, context)

            child.measure(widthMeasureSpec, heightMeasureSpec)

            if (parent.widthMode != MeasureSpec.EXACTLY) {
                parent.totalWidth += child.totalWidth
            }

            parent.totalHeight = maxOf(parent.totalHeight, child.totalHeight)
        }
    }

    override fun measureChildrenComponent(
        fView: FTree,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {

    }
}

class StackLayoutStrategy : LayoutStrategy {
    override fun layout(fView: FTree, children: List<FTree>): Pair<Int, Int> {
        children.forEach { child ->
            child.setParent(fView)
            child.layout(0, 0)
        }
        return Pair(0, 0)
    }

    override fun layoutChildComponent(
        viewGroup: CustomViewGroup2,
        left: Int,
        top: Int,
        marginPaddingRight: Int,
    ) {

    }

    override fun measureChildren(
        parent: FTree,
        context: Context,
        viewGroup: CustomViewGroup2,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        parent.children.forEach { child ->
            child.setCustomViewGroup(viewGroup, context)
            child.measure(widthMeasureSpec, heightMeasureSpec)

            parent.totalWidth = maxOf(parent.totalWidth, child.totalWidth)
            parent.totalHeight = maxOf(parent.totalHeight, child.totalHeight)
        }
    }

    override fun measureChildrenComponent(
        fView: FTree,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {

    }
}

