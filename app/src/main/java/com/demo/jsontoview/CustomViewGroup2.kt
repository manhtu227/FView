package com.demo.jsontoview

//import FView
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup

class CustomViewGroup2(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private var rootFView: FTree? = null

    init {
        setWillNotDraw(false)
    }

  fun setFViewTree(fView: FTree) {
        rootFView = fView
        rootFView!!.setCustomViewGroup(this, context)
//        requestLayout()
//        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        rootFView?.measure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(
            rootFView?.totalWidth ?: 0,
            rootFView?.totalHeight ?: 0
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        rootFView?.onTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // Bố trí root FView
        rootFView?.layout(0, 0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Vẽ root FView lên canvas
        rootFView?.draw(canvas)
    }
}
