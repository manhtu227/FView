package com.demo.jsontoview.models

import android.graphics.Canvas
import android.content.Context
import android.view.MotionEvent
import com.demo.jsontoview.CustomViewGroup2
import com.demo.jsontoview.FView

interface ViewComponent {
    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    fun layout(left: Int, top: Int, width: Int,  height: Int)
    fun draw(canvas: Canvas)
    fun onTouchEvent(event: MotionEvent): FView?
}