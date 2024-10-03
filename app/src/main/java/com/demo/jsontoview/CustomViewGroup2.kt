package com.demo.jsontoview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.demo.jsontoview.handler.ViewEventManager
import android.view.ViewTreeObserver

class CustomViewGroup2(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private var rootFView: FView? = null

    fun getRootFView(): FView? {
        return rootFView
    }

    var startTime: Long = 0  // Thời gian bắt đầu

    init {
        setWillNotDraw(false)
//        startTime = System.currentTimeMillis()  // Bắt đầu đo thời gian
        // Đăng ký lắng nghe sự kiện khi View được vẽ xong
        Log.d("CustomCenterLayout", "Start ${rootFView?.props?.id} ${startTime} ms")
        viewTreeObserver.addOnGlobalLayoutListener {
            // Thời điểm layout và draw hoàn tất
            val endTime = System.currentTimeMillis()
            if (rootFView?.props?.id == "2222")
                Log.d(
                    "CustomCenterLayout",
                    "Time to fully render (layout + draw): ${rootFView?.props?.id}  ${endTime - startTime} ms"
                )

        }
    }

    fun setFViewTree(fView: FView) {
        if (fView.props.id == "2222") {
            startTime = System.currentTimeMillis()

        }
        Log.e("CustomViewGroup2", "setFViewTreer2342: ${fView.equals(rootFView)} ${fView.hashCode()} ${rootFView.hashCode()}")
        if (fView != rootFView) {
            this.removeAllViews()
        }

        rootFView = fView
        rootFView!!.customViewGroup = this


    }

    fun measureChildPublic(child: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val indexComponent=0
        rootFView?.measure(widthMeasureSpec, heightMeasureSpec,indexComponent)
        if (rootFView?.props?.test == "7")
            Log.e(
                "CustomViewGroup2",
                "onMeasure1: ${rootFView?.children!![1].props.test} ${rootFView?.measureHeight} "
            )
        setMeasuredDimension(
            rootFView?.measureWidth ?: 0,
            rootFView?.measureHeight ?: 0
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val fView = rootFView?.onTouchEvent(event)
        if (fView != null) {
            ViewEventManager(this, fView).handleClick()
        }
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        rootFView?.layout(0, 0,0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rootFView?.draw(canvas)

        if (rootFView?.props?.id == "2222") {
            val endTime = System.currentTimeMillis()
            Log.d(
                "CustomCenterLayout",
                "vao day di): ${rootFView?.props?.id}  ${endTime - startTime} ms"
            )
        }
    }

}
