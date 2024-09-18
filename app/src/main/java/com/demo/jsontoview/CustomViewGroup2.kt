package com.demo.jsontoview

//import FView
import MyCustomAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.jsontoview.pattern.HorizontalLayoutStrategy
import com.demo.jsontoview.pattern.StackLayoutStrategy
import com.demo.jsontoview.pattern.VerticalLayoutStrategy

class CustomViewGroup2(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private var rootFView: FTree? = null
    private var checked111 = false

    init {
        setWillNotDraw(false)
    }

    fun setFViewTree(fView: FTree) {
        Log.e("CustomViewGroup2", "setFViewTree $checked111")
        var checked = false
        var children: MutableList<FTree> = mutableListOf()
        var x = 0;
        for (child in fView.children) {
            if (child.viewType == ViewTypeConfig.RecyclerView) {
                checked = true
                val view = handleRecyclerView(context!!, child)
                x++;
                this.addView(view)
            } else if (checked) {
                val view = handleViewGroup(context!!, child)
                x++;
                this.addView(view)
            } else {
                children.add(child)
            }
        }

        rootFView = FTree(
            viewType = fView.viewType,
            props = fView.props,
            children = children,
            context1 = context,
            customViewGroup1 = this
        )
        rootFView!!.setCustomViewGroup(this, context)
    }

    fun measureChildPublic(child: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Gọi measureChild từ lớp cha ViewGroup
        measureChild(child, widthMeasureSpec, heightMeasureSpec)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val layoutStrategy = when (rootFView?.props?.layoutType) {
            LayoutType.Continues -> {
                when (rootFView?.props?.orientation) {
                    OrientationConfig.Vertical -> {
                        VerticalLayoutStrategy()
                    }

                    OrientationConfig.Horizontal -> {
                        HorizontalLayoutStrategy()
                    }

                    null -> null
                }
            }

            LayoutType.Stack -> {
                StackLayoutStrategy()
            }

            null -> null
        }


        rootFView?.measure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var newWidthMeasure = widthMeasureSpec
        var newHeightMeasure = heightMeasureSpec

        if (widthMode == MeasureSpec.EXACTLY) {
            newWidthMeasure = MeasureSpec.makeMeasureSpec(
                widthSize - rootFView!!.props.margin.left - rootFView!!.props.margin.right,
                MeasureSpec.EXACTLY
            )

        }
        if (heightMode == MeasureSpec.EXACTLY) {
            newHeightMeasure = MeasureSpec.makeMeasureSpec(
                heightSize - rootFView!!.props.margin.top - rootFView!!.props.margin.bottom,
                MeasureSpec.EXACTLY
            )
        }
        if (rootFView != null)
            layoutStrategy?.measureChildrenComponent(
                rootFView!!,
                newWidthMeasure,
                newHeightMeasure
            )

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
        val (leftPosition, topPosition) = rootFView?.layout(0, 0) ?: Pair(0, 0)

        val layoutStrategy = when (rootFView?.props?.layoutType) {
            LayoutType.Continues -> {
                when (rootFView?.props?.orientation) {
                    OrientationConfig.Vertical -> {
                        VerticalLayoutStrategy()
                    }

                    OrientationConfig.Horizontal -> {
                        HorizontalLayoutStrategy()
                    }

                    null -> null
                }
            }

            LayoutType.Stack -> {
                StackLayoutStrategy()
            }

            null -> null
        }

        layoutStrategy?.layoutChildComponent(
            this,
            leftPosition + (rootFView?.props?.margin?.left ?: 0) + (rootFView?.props?.padding?.left
                ?: 0),
            topPosition + (rootFView?.props?.padding?.top ?: 0),
            (rootFView?.props?.padding?.right ?: 0) + (rootFView?.props?.margin?.right ?: 0)

        )

    }

    override fun onDraw(canvas: Canvas) {
        checked111 = true

        super.onDraw(canvas)
        rootFView?.draw(canvas)
    }

    private fun handleViewGroup(context: Context, layout: FTree): android.view.View {
        val viewGroup = CustomViewGroup2(context).apply {
            setFViewTree(layout)
        }
        return viewGroup
    }

    private fun handleRecyclerView(context: Context, layout: FTree): android.view.View {
        val recyclerView = RecyclerView(context).apply {
            layoutManager = if (layout.props.orientation == OrientationConfig.Vertical) {
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            } else {
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            layoutParams = ViewGroup.LayoutParams(
                Parser.parseDimension(layout.props.width),
                Parser.parseDimension(layout.props.height)
            ).apply {
                setPadding(
                    layout.props.padding.left,
                    layout.props.padding.top,
                    layout.props.padding.right,
                    layout.props.padding.bottom
                )
            }
            if (layout.props.background != null)
                setBackgroundColor(Color.parseColor(layout.props.background.color))

            adapter = MyCustomAdapter(layout.children)

        }

        return recyclerView
    }
}
