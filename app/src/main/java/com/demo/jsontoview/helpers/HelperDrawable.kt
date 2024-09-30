package com.demo.jsontoview.helpers

import DimensionConfig
import Props
import android.view.View
import android.view.View.MeasureSpec

open class HelperDrawable(private var props: Props, private var measureSpec: Int) {
    var value: Int = 0

    fun matchParent(widthDrawable: Int?) {
        val size = View.MeasureSpec.getSize(measureSpec)- props.margin.left - props.margin.right - props.padding.left - props.padding.right
        val mode = View.MeasureSpec.getMode(measureSpec)

        value = when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> minOf(size, widthDrawable ?: 0)
            else -> widthDrawable ?: 0
        }

    }

    fun wrapContent(widthDrawable: Int?) {
        val size = View.MeasureSpec.getSize(measureSpec)- props.margin.left - props.margin.right - props.padding.left - props.padding.right
        val mode = View.MeasureSpec.getMode(measureSpec)
        value = when (mode) {
            MeasureSpec.EXACTLY -> widthDrawable ?: 0
            MeasureSpec.AT_MOST -> minOf(widthDrawable ?: 0, size)
            else -> widthDrawable ?: 0
        }
    }

    fun unitPercent() {
        val size = View.MeasureSpec.getSize(measureSpec)- props.margin.left - props.margin.right - props.padding.left - props.padding.right
        value = size
    }


    // wrap content
    // match parent
    // unit percent
}