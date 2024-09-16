package com.demo.jsontoview.drawable


import Props
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import com.demo.jsontoview.FTree
import com.demo.jsontoview.Parser
import com.demo.jsontoview.R
import com.demo.jsontoview.models.DrawableComponent

class ButtonDrawable(private val context: Context) : DrawableComponent {
    private var textPaint: TextPaint? = null
    private var backgroundPaint: Paint? = null
    private var backgroundPaintPressed: Paint? = null
    private var borderPaint: Paint? = null

    private var iconDrawable: Drawable? = null // Thay vì sử dụng Bitmap, chúng ta dùng Drawable
    private var isPressed: Boolean = false
    private var boringLayout: BoringLayout? = null
    private var leftPosition: Int = 0
    private var topPosition: Int = 0


    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FTree,
        props: Props,
    ): Pair<Int, Int> {

        // Khởi tạo Paint cho văn bản
        textPaint = TextPaint().apply {
            textSize = (props.drawable?.props?.textSize ?: 24).toFloat()
            color = Color.parseColor(props.drawable?.props?.textColor)
            isAntiAlias = true
        }
        if (props.drawable?.props?.icon != null) {
            val resourceName = props.drawable.props.icon
            val resourceId =
                context.resources.getIdentifier(resourceName, null, context.packageName)
            iconDrawable = context.getDrawable(resourceId) // Tải vector drawable
        }

        // Khởi tạo Paint cho viền (nếu có trong props)
        props.drawable?.props?.border?.let {
            borderPaint = Paint().apply {
                color = Color.parseColor(it.color)
                style = Paint.Style.STROKE
                strokeWidth = it.width.toFloat()
                isAntiAlias = true
            }
        }

        // Đo kích thước văn bản
        val metrics = BoringLayout.isBoring(props.drawable?.data, textPaint)
        if (metrics != null) {
            boringLayout = BoringLayout.make(
                props.drawable?.data,
                textPaint,
                metrics.width,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                metrics,
                false
            )
        }


        val iconWidth = (iconDrawable?.intrinsicWidth ?: 0)
        val iconHeight = (iconDrawable?.intrinsicHeight ?: 0)

        // Tính toán kích thước tổng của nút
        val width =
            (boringLayout?.width
                ?: 0) + props.padding.left + props.padding.right + iconWidth + (props.drawable?.props?.gap
                ?: 0) + (props.drawable?.props?.border?.width ?: 0) * 3
        val height = maxOf(
            (boringLayout?.height ?: 0) + props.padding.top + props.padding.bottom,
            iconHeight + props.padding.top + props.padding.bottom
        ) + (props.drawable?.props?.border?.width ?: 0) * 3

        return Pair(width, height)
    }

    override fun draw(canvas: Canvas, width: Int, height: Int, props: Props) {
        // Vẽ nền của nút tùy thuộc vào trạng thái nhấn
        val paintToUse = if (isPressed) backgroundPaintPressed else backgroundPaint
        paintToUse?.let {
            canvas.drawRect(
                0F,
                0F,
                (width).toFloat(),
                (height).toFloat(),
                it
            )
        }

        // Vẽ icon nếu có
        iconDrawable?.let { drawable: Drawable ->
            val iconLeft = leftPosition + props.padding.left
            val iconTop = topPosition + props.padding.top

            drawable.setBounds(
                iconLeft,
                iconTop,
                iconLeft + drawable.intrinsicWidth,
                iconTop + drawable.intrinsicHeight
            )
            drawable.draw(canvas) // Vẽ icon
        }
        Log.e(
            "ButtonDrawable",
            "draw2: ${leftPosition} ${iconDrawable!!.intrinsicWidth}  ${boringLayout!!.width}"
        )

        val leftText =
            leftPosition + (iconDrawable?.intrinsicWidth ?: 0) + (props.drawable?.props?.gap ?: 0)
        // Vẽ văn bản
        textPaint?.let { paint ->
            canvas.save()
            canvas.translate(
                leftText.toFloat(),
                topPosition.toFloat() + props.padding.top
            )
            boringLayout?.draw(canvas)
            canvas.restore()
        }

        // Vẽ viền nếu có
        borderPaint?.let {
            canvas.drawRect(
                0F,
                0F,
                width.toFloat(),
                height.toFloat(),
                it
            )
        }
    }

    override fun layout(left: Int, top: Int, fView: FTree) {


        val totalWidth = fView.totalWidth
        val totalHeight = fView.totalHeight

        val widthDrawable = fView.internalWidth
        val heightDrawable = fView.internalHeight


        val gravity = Parser.parseGravityForView(fView.props.gravity)

        when (gravity) {
            Gravity.CENTER -> {
                leftPosition = (totalWidth - widthDrawable) / 2
                topPosition = (totalHeight - heightDrawable) / 2
            }

            Gravity.CENTER_VERTICAL -> {
                topPosition = (totalHeight - heightDrawable) / 2
            }

            Gravity.CENTER_HORIZONTAL -> {
                leftPosition = (totalWidth - widthDrawable) / 2
            }

            Gravity.END -> {
                leftPosition = totalWidth - fView.totalWidth
                topPosition = (totalHeight - heightDrawable) / 2
            }

            Gravity.BOTTOM -> {
                topPosition = totalHeight - fView.totalHeight
                leftPosition = (totalWidth - widthDrawable) / 2

            }

            Gravity.START -> {
                leftPosition = 0
            }
        }


    }

    fun onTouchEvent(event: MotionEvent, x: Float, y: Float): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                return true
            }
        }
        return false
    }


}
