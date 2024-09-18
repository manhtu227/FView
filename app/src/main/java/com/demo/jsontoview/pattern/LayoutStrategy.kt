package com.demo.jsontoview.pattern

import PaddingConfig
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View.MeasureSpec
import androidx.core.view.marginRight
import androidx.recyclerview.widget.RecyclerView
import com.demo.jsontoview.CustomViewGroup2
import com.demo.jsontoview.FTree
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
    override fun layout(fView: FTree, children: List<FTree>): Pair<Int, Int> {
        var currentOffset = 0

        children.forEachIndexed { index, child ->
            child.setParent(fView)
            child.layout(0, currentOffset)
            currentOffset += child.totalHeight
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
        Log.e("CustomViewGroup2", "layoutChildComponent: ${viewGroup.childCount} $top")
//        for (i in viewGroup.childCount - 1 downTo 0) {
//            val child = viewGroup.getChildAt(i)
//            top -= child.measuredHeight
//            child.layout(left, top, child.measuredWidth, top + child.measuredHeight)
//        }
        Log.e("CustomViewGroup2", "Resources.getSystem().displayMetrics.widthPixels : ${Resources.getSystem().displayMetrics.widthPixels } ${marginPaddingRight} $top")
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            child.layout(
                left, top, min(
                    left + child.measuredWidth,
                    Resources.getSystem().displayMetrics.widthPixels  - marginPaddingRight
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
            child.setCustomViewGroup(viewGroup, context)

            child.measure(widthMeasureSpec, heightMeasureSpec)

            parent.totalHeight += child.totalHeight
            parent.totalWidth = maxOf(
                parent.totalWidth,
                child.totalWidth + parent.props.padding.left + parent.props.padding.right
            )

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
            fView.customViewGroup?.measureChildPublic(child, widthMeasureSpec, heightMeasureSpec)
            if (child is RecyclerView) {
                Log.e("CustomViewGroup2", "measureChildrenComponent: ${child.measuredHeight}")
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
            currentOffset += child.totalWidth
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

