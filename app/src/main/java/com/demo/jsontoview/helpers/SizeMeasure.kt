package com.demo.jsontoview.helpers

import Props
import android.view.ViewGroup
import android.view.View.MeasureSpec

class SizeMeasurer {

    fun measureWidth(layoutWidth: Int, widthMode: Int, widthSize: Int, widthDrawable: Int, props: Props): Int {
        return when (layoutWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                val width1 = widthSize - props.margin.left - props.margin.right
                when (widthMode) {
                    MeasureSpec.EXACTLY -> width1
                    MeasureSpec.AT_MOST -> minOf(width1, widthDrawable)
                    else -> widthDrawable
                }
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                when (widthMode) {
                    MeasureSpec.EXACTLY -> widthDrawable
                    MeasureSpec.AT_MOST -> minOf(widthDrawable, widthSize - props.margin.left - props.margin.right)
                    else -> widthDrawable
                }
            }
            else -> {
                if (props.width.unit == UnitConfig.Percent) {
                    MeasureSpec.makeMeasureSpec(widthDrawable - props.padding.left - props.padding.right, MeasureSpec.EXACTLY)
                    widthDrawable
                } else {
                    MeasureSpec.makeMeasureSpec(layoutWidth, MeasureSpec.EXACTLY)
                    layoutWidth + props.padding.left + props.padding.right
                }
            }
        }
    }

    fun measureHeight(layoutHeight: Int, heightMode: Int, heightSize: Int, heightDrawable: Int, props: Props): Int {
        return when (layoutHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                val height1 = heightSize - props.margin.left - props.margin.right
                when (heightMode) {
                    MeasureSpec.EXACTLY -> height1 + props.padding.top + props.padding.bottom
                    MeasureSpec.AT_MOST -> minOf(height1 + props.padding.top + props.padding.bottom, heightDrawable)
                    else -> heightDrawable
                }
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                when (heightMode) {
                    MeasureSpec.EXACTLY -> heightDrawable
                    MeasureSpec.AT_MOST -> minOf(heightDrawable, heightSize - props.margin.left - props.margin.right)
                    else -> heightDrawable
                }
            }
            else -> {
                MeasureSpec.makeMeasureSpec(layoutHeight, MeasureSpec.EXACTLY)
                layoutHeight + props.padding.top + props.padding.bottom
            }
        }
    }
}
