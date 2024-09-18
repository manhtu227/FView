//package com.demo.jsontoview
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Paint
//import android.graphics.RectF
//import android.util.AttributeSet
//import android.util.Log
//import android.view.ViewGroup
//import android.widget.TextView
//
//class CustomViewGroup3(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {
//    init {
//        setWillNotDraw(false)
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        this.addView(TextView(context).apply {
//            text = "Hello cho"
//        })
//        for (i in 0 until childCount) {
//            val child = getChildAt(i)
//            this.measureChildWithMargins(child, widthMeasureSpec, heightMeasureSpec)
//            Log.e(
//                "CustomViewGroup3",
//                "Child width: $childCount ${child.measuredWidth}, child height: ${child.measuredHeight}"
//            )
//        }
//
//        setMeasuredDimension(
//            1080,2000
//        )
//
//
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//    }
//
//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        for (i in 0 until childCount) {
//            val child = getChildAt(i)
//            child.layout(0, 0, 110, child.measuredHeight)
//        }
//    }
//
//    override fun onDraw(canvas: Canvas) { // Đặt đối tượng Paint
//        val paint = Paint().apply {
//            textSize = 24f
//            color = android.graphics.Color.RED
//        }
//        val rect = RectF(
//            0f,
//            0f,
//            100F,
//            100F
//        )
//        canvas.drawRect(
//            rect,
//            paint
//        )
//        // Vẽ văn bản
//        canvas.drawText("sao nao", 10f, 24f, paint)
//        super.onDraw(canvas)
//        // Vẽ root FView lên canvas
//    }
//}