package com.demo.jsontoview.PropsLayout

import LayoutType
import android.util.Log
import android.view.Gravity
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser

class PropsHandler {

    fun calculateGravityPositions(
        tree: FView,
    ) {
        val props = tree.props
        val mParent = tree.mParent
        val widthCurrent = tree.measureWidth
        val totalHeight = tree.measureHeight

        if(mParent==null){
            return
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
                return
            if (mParent.props.orientation == OrientationConfig.Horizontal) {
                when (layoutGravity) {
                    Gravity.CENTER -> {
                        tree.topPosition = ((parentHeight - totalHeight) / 2)
                    }


                    Gravity.CENTER_VERTICAL -> {
                        tree.topPosition = (parentHeight - totalHeight) / 2
                    }


                    Gravity.BOTTOM -> {
                        tree.topPosition = parentHeight - totalHeight

                    }

                }
            } else if (mParent.props.orientation == OrientationConfig.Vertical) {
                when (layoutGravity) {
                    Gravity.CENTER -> {
                        tree.leftPosition = ((parentWidth - widthCurrent) / 2)
                    }


                    Gravity.CENTER_HORIZONTAL -> {
                        tree.leftPosition = (parentWidth - widthCurrent) / 2
                    }

                    Gravity.END -> {
                        tree.leftPosition = parentWidth - widthCurrent
                    }

                    Gravity.BOTTOM -> {
                        tree.leftPosition = (parentWidth - widthCurrent) / 2

                    }

                }
            }
        } else {
            when (layoutGravity) {
                Gravity.CENTER -> {
                    tree.leftPosition = ((parentWidth - widthCurrent) / 2)
                    tree.topPosition = ((parentHeight - totalHeight) / 2)
                }


                Gravity.CENTER_HORIZONTAL -> {
                    tree.leftPosition = (parentWidth - widthCurrent) / 2
                }

                Gravity.CENTER_VERTICAL -> {
                    tree.topPosition = (parentHeight - totalHeight) / 2
                }

                Gravity.END -> {
                    tree.leftPosition = parentWidth - widthCurrent
                }

                Gravity.BOTTOM -> {
                    tree.leftPosition = (parentWidth - widthCurrent) / 2
                    tree.topPosition = parentHeight - totalHeight

                }

            }
        }


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
