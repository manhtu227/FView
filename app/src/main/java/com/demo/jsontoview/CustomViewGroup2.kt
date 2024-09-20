package com.demo.jsontoview
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class CustomViewGroup2(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private var rootFView: FTree? = null

    init {
        setWillNotDraw(false)
    }

    fun setFViewTree(fView: FTree) {
        rootFView = fView

        rootFView!!.setCustomViewGroup(this, context)
    }

    fun measureChildPublic(child: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec)
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
       rootFView?.layout(0, 0,0,0) ?: Pair(0, 0)

//        layoutStrategy?.layoutChildComponent(
//            this,
//            leftPosition + (rootFView?.props?.margin?.left ?: 0) + (rootFView?.props?.padding?.left
//                ?: 0),
//            topPosition + (rootFView?.props?.padding?.top ?: 0),
//            (rootFView?.props?.padding?.right ?: 0) + (rootFView?.props?.margin?.right ?: 0)
//        )

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rootFView?.draw(canvas)
    }

}
