package com.demo.jsontoview.pattern

import PaddingConfig
import android.content.Context
import android.util.Log
import android.view.View.MeasureSpec
import com.demo.jsontoview.CustomViewGroup2
import com.demo.jsontoview.FTree


interface LayoutStrategy {
    fun layout(fView: FTree, children: List<FTree>)
    fun measureChildren(
        parent: FTree,
        context: Context,
        viewGroup: CustomViewGroup2,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    )
}



class VerticalLayoutStrategy : LayoutStrategy {
    override fun layout(fView: FTree, children: List<FTree>) {
        var currentOffset = 0
        children.forEachIndexed { index, child ->
            child.setParent(fView)
            child.layout(0, currentOffset)
            currentOffset += child.totalHeight
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
    }
}

class HorizontalLayoutStrategy : LayoutStrategy {
    override fun layout(fView: FTree, children: List<FTree>) {
        var currentOffset = 0
        children.forEach { child ->
            child.setParent(fView)
            child.layout(currentOffset, 0)
            currentOffset += child.totalWidth
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

            if (parent.widthMode != MeasureSpec.EXACTLY) {
                parent.totalWidth += child.totalWidth
            }

            parent.totalHeight = maxOf(parent.totalHeight, child.totalHeight)
        }
    }
}

class StackLayoutStrategy : LayoutStrategy {
    override fun layout(fView: FTree, children: List<FTree>) {
        children.forEach { child ->
            child.setParent(fView)
            child.layout(0, 0)
        }
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
}

