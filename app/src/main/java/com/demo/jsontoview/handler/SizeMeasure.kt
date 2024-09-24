package com.demo.jsontoview.helpers

import android.util.Log
import android.view.ViewGroup
import android.view.View.MeasureSpec
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser

class SizeMeasurer {

    fun measureWidth(widthMeasureSpec: Int, widthDrawable: Int, fView: FView): Pair<Int, Int> {
        val props = fView.props
        val layoutWidth = Parser.parseDimension(props.width)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var widthMode = MeasureSpec.getMode(widthMeasureSpec)

        var widthCurrent =
            widthSize - props.margin.left - props.margin.right - props.padding.left - props.padding.right

        val helperWidth = HelperDrawable(props, widthMeasureSpec)
        val measureWidth = when (layoutWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                helperWidth.matchParent(widthDrawable)
                helperWidth.value
            }

            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                helperWidth.wrapContent(widthDrawable)
                helperWidth.value
            }

            else -> {
                widthMode = MeasureSpec.EXACTLY
                if (props.width.unit == UnitConfig.Percent) {
                    widthCurrent = widthDrawable
                    widthDrawable
                } else {
                    widthCurrent = layoutWidth
                    layoutWidth
                }
            }
        }

        val totalGap =
            if (fView.props.orientation == OrientationConfig.Horizontal)  ((props.gap
                ?: 0) * (if (fView.children.size - 1 > 0) fView.children.size - 1 else 0)) else 0


        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
            widthCurrent - totalGap,
            widthMode
        )

        return Pair(measureWidth + props.padding.left + props.padding.right, newWidthMeasureSpec)
    }


    fun measureHeight(
        heightMeasureSpec: Int,
        heightDrawable: Int,
        fView: FView,
    ): Pair<Int, Int> {
        val props = fView.props
        val layoutHeight = Parser.parseDimension(props.height)

        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var heightMode = MeasureSpec.getMode(heightMeasureSpec)


        var heightCurrent =
            heightSize - props.margin.left - props.margin.right - props.padding.left - props.padding.right

        val helperHeight = HelperDrawable(props, heightMeasureSpec)
        val measureHeight = when (layoutHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                helperHeight.matchParent(heightDrawable)
                helperHeight.value
            }

            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                helperHeight.wrapContent(heightDrawable)
                helperHeight.value
            }

            else -> {
                heightMode = MeasureSpec.EXACTLY
                if (props.width.unit == UnitConfig.Percent) {
                    heightCurrent = heightDrawable
                    heightDrawable
                } else {
                    Log.e("SizeMeasurer", "measureHeight: $layoutHeight")
                    heightCurrent = layoutHeight
                    layoutHeight
                }
            }
        }
        val totalGap =
            if (fView.props.orientation == OrientationConfig.Vertical) ((props.gap
                ?: 0) * (if (fView.children.size - 1 > 0) fView.children.size - 1 else 0)) else 0

        val newHeightMeasureSpec =
            MeasureSpec.makeMeasureSpec(
                heightCurrent - totalGap,
                heightMode
            )

        return Pair(measureHeight + props.padding.top + props.padding.bottom+totalGap, newHeightMeasureSpec)
    }
}
