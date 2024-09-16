package com.demo.jsontoview.models

import android.graphics.Canvas
import Props
import com.demo.jsontoview.FTree

interface DrawableComponent {
    fun draw(canvas: Canvas, width: Int, height: Int, props: Props)
    fun layout(left: Int, top: Int, fView: FTree)
    fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FTree,
        props: Props,
    ): Pair<Int, Int>
}