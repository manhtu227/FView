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
import android.view.View.MeasureSpec
import android.view.ViewGroup
import com.demo.jsontoview.PropsLayout.LayoutGravityHandler
import com.demo.jsontoview.drawable.ButtonDrawable
import com.demo.jsontoview.drawable.ImageArrayDrawable
import com.demo.jsontoview.drawable.ImageDrawable
import com.demo.jsontoview.drawable.TextDrawable
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
    var leftPosition: Int = 0
    var topPosition: Int = 0

    internal var mParent: FTree? = null

    var measuredWidthDrawable: Int = 0
    var measuredHeightDrawable: Int = 0

    var context: Context? = null
    var customViewGroup: CustomViewGroup2? = null
    private var drawableComponent: DrawableComponent? = null
    private var layoutStrategy: LayoutStrategy? = null

    var totalWidth: Int = 0
    var totalHeight: Int = 0

    var internalWidth: Int = 0
    var internalHeight: Int = 0

    var widthMode: Int = 0
    var heightMode: Int = 0

    var widthSize: Int = 0
    var heightSize: Int = 0

    private var leftView: Int = 0
    private var topView: Int = 0

    private var backgroundColor: String? = null
    private var colorAnimator: ValueAnimator? = null

    var imageBitmaps: MutableList<Bitmap?> = mutableListOf()
    var imageBitmap: Bitmap? = null
    var paint = Paint(Paint.ANTI_ALIAS_FLAG)


    fun setParent(parent: FTree) {
        mParent = parent
    }


    override fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutWidth = Parser.parseDimension(props.width)
        val layoutHeight = Parser.parseDimension(props.height)

        val (widthDrawable, heightDrawable) = drawableComponent?.measure(
            widthMeasureSpec,
            heightMeasureSpec,
            this,
            props
        ) ?: Pair(
            props.padding.left + props.padding.right,
            props.padding.top + props.padding.bottom
        )

        internalWidth = widthDrawable
        internalHeight = heightDrawable

        var newWidthMeasure = widthMeasureSpec
        var newHeightMeasure = heightMeasureSpec

        widthMode = MeasureSpec.getMode(widthMeasureSpec)
        heightMode = MeasureSpec.getMode(heightMeasureSpec)

        widthSize = MeasureSpec.getSize(widthMeasureSpec)
        heightSize = MeasureSpec.getSize(heightMeasureSpec)

        newWidthMeasure = MeasureSpec.makeMeasureSpec(
            widthSize - props.margin.left - props.margin.right - props.padding.left - props.padding.right,
            widthMode
        )


        measuredWidthDrawable = when (layoutWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                val width1 =
                    widthSize - props.margin.left - props.margin.right
                when (widthMode) {
                    MeasureSpec.EXACTLY -> {
                        width1
                    }

                    MeasureSpec.AT_MOST -> {
                        minOf(width1, widthDrawable)
                    }

                    else -> widthDrawable
                }
            }

            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                when (widthMode) {
                    MeasureSpec.EXACTLY -> widthDrawable
                    MeasureSpec.AT_MOST -> minOf(
                        widthDrawable,
                        widthSize - props.margin.left - props.margin.right
                    )

                    else -> widthDrawable
                }
            }

            else -> {
                if (props.width.unit == UnitConfig.Percent) {
                    newWidthMeasure = MeasureSpec.makeMeasureSpec(
                        widthDrawable - props.padding.left - props.padding.right,
                        MeasureSpec.EXACTLY
                    )
                    widthDrawable
                } else {
                    newWidthMeasure = MeasureSpec.makeMeasureSpec(layoutWidth, MeasureSpec.EXACTLY)
                    layoutWidth + props.padding.left + props.padding.right
                }
            }
        }

        measuredHeightDrawable = when (layoutHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                val height1 =
                    heightSize - props.margin.left - props.margin.right
                when (heightMode) {
                    MeasureSpec.EXACTLY -> height1 + props.padding.top + props.padding.bottom
                    MeasureSpec.AT_MOST -> minOf(
                        height1 + props.padding.top + props.padding.bottom,
                        heightDrawable
                    )

                    else -> heightDrawable
                }
            }

            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                when (heightMode) {
                    MeasureSpec.EXACTLY -> heightDrawable
                    MeasureSpec.AT_MOST -> minOf(
                        heightDrawable,
                        heightSize - props.margin.left - props.margin.right
                    )

                    else -> heightDrawable
                }
            }

            else -> {
                newHeightMeasure = MeasureSpec.makeMeasureSpec(layoutHeight, MeasureSpec.EXACTLY)
                layoutHeight + props.padding.top + props.padding.bottom
            }
        }

        totalWidth = measuredWidthDrawable + props.margin.left + props.margin.right
        totalHeight = measuredHeightDrawable + props.margin.top + props.margin.bottom

        layoutStrategy?.measureChildren(
            this,
            context!!,
            customViewGroup!!,
            newWidthMeasure,
            newHeightMeasure
        )

    }

    override fun layout(left: Int, top: Int){
        leftPosition = left
        topPosition = top
        drawableComponent?.layout(left, top, this)
        layoutStrategy?.layout(this, children) ?: Pair(0, 0)

        val (leftCal, topCal) = LayoutGravityHandler().calculateGravityPositions(this, left, top)
        leftPosition = leftCal
        topPosition = topCal
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
        if (props.background != null || backgroundColor != null) {
            val paint = Paint().apply {
                color = Color.parseColor(backgroundColor ?: props.background?.color)
            }

            val rect = RectF(
                0f,
                0f,
                totalWidth.toFloat() - props.margin.right - props.margin.left,
                totalHeight.toFloat() - props.margin.bottom - props.margin.top
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
            measuredWidthDrawable,
            measuredHeightDrawable,
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
        if (x >= leftView && x <= leftView + totalWidth && y >= topView && y <= topView + totalHeight) {
            return true
        }
        return false
    }

    private fun init() {
        totalHeight = 0;
        totalWidth = 0;


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

            TypeConfig.ArrayImage -> {
                ImageArrayDrawable()
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
    }

    override fun setCustomViewGroup(customViewGroup: CustomViewGroup2, context: Context) {
        this.context = context
        this.customViewGroup = customViewGroup
        init()
        if (props.drawable?.type == TypeConfig.Image) {
            (drawableComponent as ImageDrawable).loadImageFromUrl(
                context,
                this,
                props.drawable.data
            ) {
                customViewGroup.requestLayout()
                customViewGroup.invalidate()
            }
        } else if (props.drawable?.type == TypeConfig.ArrayImage) {
            (drawableComponent as ImageArrayDrawable).loadImagesFromUrls(
                context,
                this,
                props.drawable.dataList
            ) {
                customViewGroup.requestLayout()
                customViewGroup.invalidate()
            }
        }

    }
}