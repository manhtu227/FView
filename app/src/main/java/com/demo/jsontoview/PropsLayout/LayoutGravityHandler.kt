package com.demo.jsontoview.PropsLayout

import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import com.demo.jsontoview.FTree
import com.demo.jsontoview.Parser

class LayoutGravityHandler {


    fun calculateGravityPositions(
        tree: FTree,
        leftPosition: Int,
        topPosition: Int,
    ): Pair<Int, Int> {
        val props = tree.props
        val mParent = tree.mParent
        val totalWidth = tree.totalWidth
        val totalHeight = tree.totalHeight
        var leftPosition = leftPosition
        var topPosition = topPosition

        val layoutGravity = Parser.parseGravityForView(props.layoutGravity)

        if (mParent?.widthMode == MeasureSpec.EXACTLY) {
            val widthLayoutGravity = totalWidth - props.margin.left - props.margin.right
            val parentWidth = (mParent.totalWidth ?: 0) - (mParent?.props?.margin?.left
                ?: 0) - (mParent.props?.margin?.right ?: 0)

            when (layoutGravity) {
                Gravity.CENTER -> {
                    leftPosition = ((parentWidth - widthLayoutGravity) / 2)
                }


                Gravity.CENTER_HORIZONTAL -> {
                    leftPosition = (parentWidth - widthLayoutGravity) / 2
                }

                Gravity.END -> {
                    leftPosition = parentWidth - widthLayoutGravity
                }

                Gravity.BOTTOM -> {
                    leftPosition = (parentWidth - widthLayoutGravity) / 2

                }

                Gravity.START -> {
                    leftPosition = 0
                }
            }
        }
        if (mParent?.heightMode == MeasureSpec.EXACTLY) {
            val heightLayoutGravity = totalHeight - props.margin.top - props.margin.bottom
            val parentHeight = (mParent.totalHeight ?: 0) - (mParent?.props?.margin?.top
                ?: 0) - (mParent?.props?.margin?.bottom ?: 0)

            Log.e("FTree", "layout new: ${parentHeight} ${heightLayoutGravity}");
            when (layoutGravity) {
                Gravity.CENTER -> {
                    topPosition = (parentHeight - heightLayoutGravity) / 2
                }

                Gravity.CENTER_VERTICAL -> {
                    topPosition = (parentHeight - heightLayoutGravity) / 2
                }

                Gravity.BOTTOM -> {
                    topPosition = parentHeight - heightLayoutGravity
                }

                Gravity.START -> {
                    topPosition = 0
                }
            }
        }
        return Pair(leftPosition, topPosition)
    }
}
