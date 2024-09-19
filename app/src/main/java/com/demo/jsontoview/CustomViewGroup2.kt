package com.demo.jsontoview
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.demo.jsontoview.pattern.HorizontalLayoutStrategy
import com.demo.jsontoview.pattern.LayoutStrategy
import com.demo.jsontoview.pattern.StackLayoutStrategy
import com.demo.jsontoview.pattern.VerticalLayoutStrategy

class CustomViewGroup2(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private var rootFView: FTree? = null
    private var layoutStrategy: LayoutStrategy? = null

    init {
        setWillNotDraw(false)
    }

    private fun shouldAddView(fView: FTree): Boolean {
        if (fView.viewType == ViewTypeConfig.RecyclerView) {
            return true
        }
        fView.children.forEach {
            return shouldAddView(it)
        }
        return false
    }

    private fun handleChildViews(context: Context, fView: FTree): FTree {
        val children: MutableList<FTree> = mutableListOf()
        var shouldAddViewValue = false

        for (child in fView.children) {
            if (!shouldAddViewValue) {
                shouldAddViewValue = shouldAddView(child)
            }
            if (child.viewType == ViewTypeConfig.RecyclerView) {
                val view = ViewGroupType().viewGroup(context, child)
                this.addView(view)
            } else if (shouldAddViewValue) {
                val view = ViewGroupType().recyclerView(context, child)
                this.addView(view)
            } else {
                children.add(child)
            }
        }

        return FTree(
            viewType = fView.viewType,
            props = fView.props,
            children = children
        )
    }

    fun setFViewTree(fView: FTree) {
        rootFView = fView
//        layoutStrategy = when (rootFView?.props?.layoutType) {
//            LayoutType.Continues -> {
//                when (rootFView?.props?.orientation) {
//                    OrientationConfig.Vertical -> {
//                        VerticalLayoutStrategy()
//                    }
//
//                    OrientationConfig.Horizontal -> {
//                        HorizontalLayoutStrategy()
//                    }
//
//                    null -> null
//                }
//            }
//
//            LayoutType.Stack -> {
//                StackLayoutStrategy()
//            }
//
//            null -> null
//        }

        rootFView!!.setCustomViewGroup(this, context)
    }

    fun measureChildPublic(child: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        rootFView?.measure(widthMeasureSpec, heightMeasureSpec)

//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
//        var newWidthMeasure = widthMeasureSpec
//        var newHeightMeasure = heightMeasureSpec
//
//        if (widthMode == MeasureSpec.EXACTLY) {
//            newWidthMeasure = MeasureSpec.makeMeasureSpec(
//                widthSize - rootFView!!.props.margin.left - rootFView!!.props.margin.right,
//                MeasureSpec.EXACTLY
//            )
//
//        }
//        if (heightMode == MeasureSpec.EXACTLY) {
//            newHeightMeasure = MeasureSpec.makeMeasureSpec(
//                heightSize - rootFView!!.props.margin.top - rootFView!!.props.margin.bottom,
//                MeasureSpec.EXACTLY
//            )
//        }
//        if (rootFView != null) {
//            Log.e("CustomViewGroup2", "onMeasure124323423: ${rootFView?.props?.test} $widthMode ${MeasureSpec.getMode(newWidthMeasure)} ${MeasureSpec.getSize(newWidthMeasure)}")
//            layoutStrategy?.measureChildrenComponent(
//                rootFView!!,
//                newWidthMeasure,
//                newHeightMeasure
//            )
//        }

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
