package com.demo.jsontoview.models
import android.graphics.Canvas
import android.content.Context
import android.view.MotionEvent
import com.demo.jsontoview.CustomViewGroup2

interface ViewComponent {
    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    fun layout(left: Int, top: Int)
    fun draw(canvas: Canvas)
    fun onTouchEvent(event: MotionEvent): Boolean
    fun setCustomViewGroup(customViewGroup: CustomViewGroup2, context: Context)
}