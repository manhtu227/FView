package com.demo.jsontoview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.demo.jsontoview.handler.ViewEventManager

class CustomViewGroup2(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private var rootFView: FView? = null
    var pendingViews: MutableMap<String, View> = mutableMapOf()

    fun getRootFView(): FView? {
        return rootFView
    }

    init {
        setWillNotDraw(false)
    }

    fun setFViewTree(fView: FView) {
//        if (fView.props.test == "7") {
//            Log.e("CustomViewGroup2", "setFViewTree: ${rootFView} ${fView.props}")
//        }
        if(rootFView != null) {
            pendingViews.clear()
            this.removeAllViews()
        }

        rootFView = fView
        rootFView!!.customViewGroup=this


    }

    fun measureChildPublic(child: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)



        rootFView?.measure(widthMeasureSpec, heightMeasureSpec)
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
        rootFView?.layout(0, 0, 0, 0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rootFView?.draw(canvas)
    }

}
