package com.demo.jsontoview

import Props
import ViewTypeConfig
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.demo.jsontoview.PropsLayout.LayoutGravityHandler
import com.demo.jsontoview.drawable.ButtonDrawable
import com.demo.jsontoview.drawable.ImageDrawable
import com.demo.jsontoview.drawable.TextDrawable
import com.demo.jsontoview.helpers.SizeMeasurer
import com.demo.jsontoview.models.DrawableComponent
import com.demo.jsontoview.models.ViewComponent
import com.demo.jsontoview.pattern.HorizontalLayoutStrategy
import com.demo.jsontoview.pattern.LayoutStrategy
import com.demo.jsontoview.pattern.StackLayoutStrategy
import com.demo.jsontoview.pattern.VerticalLayoutStrategy
import com.google.gson.annotations.SerializedName

class FTree(
    @SerializedName("viewType") val viewType: ViewTypeConfig,
    @SerializedName("props") val props: Props,
    @SerializedName("children") val children: List<FTree> = emptyList(),
) : ViewComponent {

    internal var mParent: FTree? = null

    var context: Context? = null
    var customViewGroup: CustomViewGroup2? = null

    private var drawableComponent: DrawableComponent? = null
    private var layoutStrategy: LayoutStrategy? = null

    var pendingViews: MutableMap<Int, View>? = null;

    var measureWidth: Int = 0
    var measureHeight: Int = 0

    var leftPosition: Int = 0
    var topPosition: Int = 0

    private var leftView: Int = 0
    private var topView: Int = 0

    private var backgroundColor: String? = "#bc594a"
    private var colorAnimator: ValueAnimator? = null


    fun setParent(parent: FTree) {
        mParent = parent
    }

    override fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // tính width height của drawable
        drawableComponent?.measure(
            widthMeasureSpec, heightMeasureSpec, this
        )

        var newWidthMeasureSpec = widthMeasureSpec
        var newHeightMeasureSpec = heightMeasureSpec

        // tính toán dựa trên layout width
        val sizeMeasure = SizeMeasurer()
        sizeMeasure.measureWidth(widthMeasureSpec, drawableComponent?.width ?: 0, this)
            .let { (width, newWidth) ->
                measureWidth = width + props.margin.left + props.margin.right
                newWidthMeasureSpec = newWidth
            }

        sizeMeasure.measureHeight(heightMeasureSpec, drawableComponent?.height ?: 0, this)
            .let { (height, newHeight) ->
                measureHeight = height + props.margin.top + props.margin.bottom
                newHeightMeasureSpec = newHeight
            }


        layoutStrategy?.measureChildren(
            this,
            newWidthMeasureSpec,
            newHeightMeasureSpec
        )

    }

    override fun layout(left: Int, top: Int, width: Int, height: Int) {
        leftPosition = left
        topPosition = top

        drawableComponent?.layout(left, top, this)

        layoutStrategy?.layout(
            this,
            width + props.margin.left + props.padding.left,
            height + props.margin.top + props.padding.top
        )

        LayoutGravityHandler().calculateGravityPositions(this)

    }

    override fun draw(canvas: Canvas) {
        canvas.save()

        canvas.translate(
            leftPosition.toFloat() + props.margin.left,
            topPosition.toFloat() + props.margin.top
        )

        leftView = -canvas.getClipBounds().left
        topView = -canvas.getClipBounds().top

        // Vẽ nền của FView
//        backgroundColor="#bc594a"
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

        children.forEach {
            if (it.viewType == ViewTypeConfig.ViewGroup) {
                it.draw(canvas)
            }
        }

        canvas.restore()

    }

    private fun animateBackgroundColor(startColor: Int, endColor: Int) {
        colorAnimator?.cancel() // Hủy bỏ animation trước đó nếu có
        colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor).apply {
            duration = 300 // Thời gian animation, có thể điều chỉnh
            addUpdateListener { animator ->
                backgroundColor =
                    String.format("#%06X", 0xFFFFFF and (animator.animatedValue as Int))
                customViewGroup?.invalidate() // Vẽ lại view để cập nhật màu nền
            }
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        if (props.clickAction != null) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (checkClick(x, y)) {
                    animateBackgroundColor(
                        Color.parseColor(props.background?.color ?: "#FFFFFF"),
                        Color.LTGRAY
                    )

                }
                customViewGroup?.invalidate()
            }
            if (event.action == MotionEvent.ACTION_UP) {
                if (checkClick(x, y)) {
                    animateBackgroundColor(
                        Color.LTGRAY,
                        Color.parseColor(props.background?.color ?: "#FFFFFF")
                    )

                }
            }
        }
        children.forEach {
            it.onTouchEvent(event)
        }

        return true
    }

    private fun checkClick(x: Float, y: Float): Boolean {
        if (x >= leftView && x <= leftView + measureWidth && y >= topView && y <= topView + measureHeight) {
            return true
        }
        return false
    }

    private fun init() {
        measureHeight = 0;
        measureWidth = 0;
        if (pendingViews == null) {
            pendingViews = mutableMapOf()
        }

        drawableComponent = when (props.drawable?.type) {
            TypeConfig.Text -> {
                TextDrawable()
            }

            TypeConfig.Image -> {
                ImageDrawable()
            }

            TypeConfig.Button -> {
                ButtonDrawable(context!!)
            }

            else -> {
                null
            }
        }
        layoutStrategy = when (props.layoutType) {
            LayoutType.Continues -> {
                when (props.orientation) {
                    OrientationConfig.Vertical -> {
                        VerticalLayoutStrategy()
                    }

                    OrientationConfig.Horizontal -> {
                        HorizontalLayoutStrategy()
                    }
                }
            }

            LayoutType.Stack -> {
                StackLayoutStrategy()
            }

        }

        if (props.drawable?.type == TypeConfig.Image) {
            (drawableComponent as ImageDrawable).loadImageFromUrl(
                context!!,
                this,
                props.drawable.data
            ) {
                customViewGroup?.requestLayout()
                customViewGroup?.invalidate()
            }
        }
    }

    override fun setCustomViewGroup(customViewGroup: CustomViewGroup2, context: Context) {
       if(this.context == null) {
           this.context = context
           this.customViewGroup = customViewGroup
           init()
       }
    }
}