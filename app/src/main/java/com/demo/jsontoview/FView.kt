package com.demo.jsontoview

import Props
import TypeConfig
import ViewTypeConfig
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import com.demo.jsontoview.PropsLayout.PropsHandler
import com.demo.jsontoview.drawable.ButtonDrawable
import com.demo.jsontoview.drawable.IconDrawable
import com.demo.jsontoview.drawable.ImageDrawable
import com.demo.jsontoview.drawable.TextDrawable
import com.demo.jsontoview.handler.SizeMeasure
import com.demo.jsontoview.handler.TouchEventHandler
import com.demo.jsontoview.models.DrawableComponent
import com.demo.jsontoview.models.ViewComponent
import com.demo.jsontoview.pattern.HorizontalLayoutStrategy
import com.demo.jsontoview.pattern.LayoutStrategy
import com.demo.jsontoview.pattern.StackLayoutStrategy
import com.demo.jsontoview.pattern.VerticalLayoutStrategy
import com.google.gson.annotations.SerializedName

class FView(
    @SerializedName("viewType") val viewType: ViewTypeConfig,
    @SerializedName("props") val props: Props,
    @SerializedName("children") val children: MutableList<FView> = mutableListOf(),
) : ViewComponent {
    internal var mParent: FView? = null

    //var  context: Context? = null
    var customViewGroup: CustomViewGroup2? = null
        set(value) {
            field = value
            init()
        }

    private var drawableComponent: DrawableComponent? = null // text, image, button
    private var layoutStrategy: LayoutStrategy? = null

    var measureWidth: Int = 0 // total width
    var measureHeight: Int = 0 // total height

    var leftPosition: Int = 0
    var topPosition: Int = 0

    var leftTouch: Int = 0 // position to touch
    var topTouch: Int = 0

    var backgroundColor: String? = null
    var colorAnimator: ValueAnimator? = null
    var gapJustifyContent: Int = 0


    fun setParent(parent: FView) {
        mParent = parent
    }

    fun getDrawableComponent(): DrawableComponent {
        return drawableComponent!!
    }

    override fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int,indexComponent:Int) {

        var childWidthMeasureSpec = widthMeasureSpec
        var childHeightMeasureSpec = heightMeasureSpec

        // tính width height của drawable nếu có
        drawableComponent?.measure(
            widthMeasureSpec, heightMeasureSpec, this
        )

        // tính toán dựa trên layout width
        val sizeMeasure = SizeMeasure()

        sizeMeasure.measureWidth(widthMeasureSpec, drawableComponent?.width ?: 0, this)
            .let { (width, newWidth) ->
                measureWidth = width + props.margin.left + props.margin.right
                childWidthMeasureSpec = newWidth
            }

        sizeMeasure.measureHeight(heightMeasureSpec, drawableComponent?.height ?: 0, this)
            .let { (height, newHeight) ->
                measureHeight = height + props.margin.top + props.margin.bottom
                childHeightMeasureSpec = newHeight
            }

        // tính toán dựa trên layout
        layoutStrategy?.measureChildren(
            childWidthMeasureSpec,
            childHeightMeasureSpec,
            indexComponent
        )
    }

    override fun layout(left: Int, top: Int,indexComponent:Int) {
        drawableComponent?.layout(left, top, this)
        val propsHandler = PropsHandler()

        propsHandler.justifyContentHandler(this)

        layoutStrategy?.layoutChildren(
            left + props.margin.left + props.padding.left,
            top + props.margin.top + props.padding.top,
            indexComponent
        )


        val (l, t) = propsHandler.calculateGravityPositions(this)
        leftPosition = left + l
        topPosition = top + t
    }

    override fun draw(canvas: Canvas) {
        canvas.save()

        canvas.translate(
            leftPosition.toFloat() + props.margin.left,
            topPosition.toFloat() + props.margin.top
        )

        leftTouch = -canvas.getClipBounds().left
        topTouch = -canvas.getClipBounds().top


        if (props.background != null || backgroundColor != null) {
            val paint = Paint().apply {
                color = Color.parseColor(backgroundColor ?: props.background?.color)
            }

            val rect = RectF(
                0f,
                0f,
                measureWidth.toFloat() - props.margin.right - props.margin.left,
                measureHeight.toFloat() - props.margin.bottom - props.margin.top
            )
            if (props.radius != null) {
                canvas.drawRoundRect(rect, props.radius.toFloat(), props.radius.toFloat(), paint)
            } else
                canvas.drawRect(
                    rect,
                    paint
                )
        }

        canvas.translate(
            props.padding.left.toFloat(),
            props.padding.top.toFloat()
        )

        drawableComponent?.draw(
            canvas,
            props
        )

        canvas.restore()
        children.forEach {
            if (it.viewType == ViewTypeConfig.ViewGroup && it.props.isComponent != true) {
                it.draw(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): FView? {
        val fViewClicked = TouchEventHandler(event, this).onTouchEvent()
        for (child in children) {
            val fView = child.onTouchEvent(event)
            if (fView != null) {
                return fView
            }
        }

        return fViewClicked
    }


    private fun init() {
        measureHeight = 0;
        measureWidth = 0;

        for (child in children) {
//            child.customViewGroup = customViewGroup
            Log.e("FView", "init item1212: ${props.id} $this")
            if (!(child.viewType == ViewTypeConfig.ViewGroup && child.props.isComponent != true)) {
                val item = ViewGroupType().getView(customViewGroup!!.context!!, child)
                Log.e("FView", "init item11: ${item}")
                customViewGroup!!.addView(item)
            }else{
                child.customViewGroup=customViewGroup
            }
        }




            drawableComponent = when (props.drawable?.type) {
            TypeConfig.Text -> {
                TextDrawable()
            }

            TypeConfig.Image -> {
                ImageDrawable()
            }

            TypeConfig.Button -> {
                ButtonDrawable(customViewGroup!!.context)
            }

            TypeConfig.Icon -> {
                IconDrawable(customViewGroup!!.context)
            }


            else -> {
                null
            }
        }
        layoutStrategy = when (props.layoutType) {
            LayoutType.Continues -> {
                when (props.orientation) {
                    OrientationConfig.Vertical -> {
                        VerticalLayoutStrategy(this)
                    }

                    OrientationConfig.Horizontal -> {
                        HorizontalLayoutStrategy(this)
                    }
                }
            }

            LayoutType.Stack -> {
                StackLayoutStrategy(this)
            }

        }


        if (props.drawable?.type == TypeConfig.Image) {
            (drawableComponent as ImageDrawable).loadImageFromUrl(
                this,
                props.drawable.data
            )
        }
    }

//    fun setCustomViewGroup(customViewGroup: CustomViewGroup2) {
////        this.context = context
//        this.customViewGroup = customViewGroup
//        init()
//    }
}