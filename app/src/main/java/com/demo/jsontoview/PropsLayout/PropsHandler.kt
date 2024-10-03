package com.demo.jsontoview.PropsLayout

import LayoutType
import android.util.Log
import android.view.Gravity
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser

class PropsHandler {

    fun calculateGravityPositions(
        tree: FView,
    ):Pair<Int,Int> {
        val props = tree.props
        val mParent = tree.mParent
        val widthCurrent = tree.measureWidth
        val totalHeight = tree.measureHeight
        var left=0
        var top=0

        if(mParent==null){
            return Pair(0,0)
        }

        val layoutGravity = Parser.parseGravityForView(props.layoutGravity)

        val parentWidth = (mParent?.measureWidth ?: 0) - (mParent?.props?.margin?.left
            ?: 0) - (mParent?.props?.margin?.right ?: 0) - (mParent?.props?.padding?.left
            ?: 0) - (mParent?.props?.padding?.right ?: 0)
        val parentHeight = (mParent?.measureHeight ?: 0) - (mParent?.props?.margin?.top
            ?: 0) - (mParent?.props?.margin?.bottom ?: 0) - (mParent?.props?.padding?.top
            ?: 0) - (mParent?.props?.padding?.bottom ?: 0)


        if (mParent?.props?.layoutType == LayoutType.Continues) {
            if (mParent.props.justifyContent != null)
                return Pair(0, 0)
            if (mParent.props.orientation == OrientationConfig.Horizontal) {
                when (layoutGravity) {
                    Gravity.CENTER -> {
                       top = ((parentHeight - totalHeight) / 2)
                    }


                    Gravity.CENTER_VERTICAL -> {
                        top = (parentHeight - totalHeight) / 2
                    }


                    Gravity.BOTTOM -> {
                        top = parentHeight - totalHeight

                    }

                }
            } else if (mParent.props.orientation == OrientationConfig.Vertical) {
                when (layoutGravity) {
                    Gravity.CENTER -> {
                       left = ((parentWidth - widthCurrent) / 2)
                    }


                    Gravity.CENTER_HORIZONTAL -> {
                        left = (parentWidth - widthCurrent) / 2
                    }

                    Gravity.END -> {
                        left = parentWidth - widthCurrent
                    }

                    Gravity.BOTTOM -> {
                        left = (parentWidth - widthCurrent) / 2

                    }

                }
            }
        } else {
            when (layoutGravity) {
                Gravity.CENTER -> {
                    left = ((parentWidth - widthCurrent) / 2)
                    top = ((parentHeight - totalHeight) / 2)
                }


                Gravity.CENTER_HORIZONTAL -> {
                    left = (parentWidth - widthCurrent) / 2
                }

                Gravity.CENTER_VERTICAL -> {
                    top = (parentHeight - totalHeight) / 2
                }

                Gravity.END -> {
                    left = parentWidth - widthCurrent
                }

                Gravity.BOTTOM -> {
                  left = (parentWidth - widthCurrent) / 2
                    top= parentHeight - totalHeight

                }

            }
        }

        return Pair(left,top)

    }

    fun justifyContentHandler(tree: FView) {
        val justifyContent = tree.props.justifyContent
        if (justifyContent == null || tree.props.layoutType == LayoutType.Stack)
            return
        val widthCurrent =
            tree.measureWidth - tree.props.margin.left - tree.props.margin.right - tree.props.padding.left - tree.props.padding.right
        val heightCurrent =
            tree.measureHeight - tree.props.margin.top - tree.props.margin.bottom - tree.props.padding.top - tree.props.padding.bottom
        var totalWidthChildren = 0
        var totalHeightChildren = 0
        for (child in tree.children) {
            totalWidthChildren += child.measureWidth
            totalHeightChildren += child.measureHeight
        }

        val widthSpace =
            if ((widthCurrent - totalWidthChildren) > 0) widthCurrent - totalWidthChildren else 0
        val heightSpace =
            if ((heightCurrent - totalHeightChildren) > 0) heightCurrent - totalHeightChildren else 0
        if (tree.props.orientation == OrientationConfig.Vertical) {
            tree.gapJustifyContent = heightSpace / (tree.children.size - 1)
        } else {
            tree.gapJustifyContent = widthSpace / (tree.children.size - 1)
        }
    }
}
