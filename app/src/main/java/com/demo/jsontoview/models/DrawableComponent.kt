package com.demo.jsontoview.models

import android.graphics.Canvas
import Props
import com.demo.jsontoview.FView

interface DrawableComponent {
    var width: Int
    var height: Int

    fun draw(canvas: Canvas, props: Props)
    fun layout(left: Int, top: Int, fView: FView)
    fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
    )
}