package com.demo.jsontoview.drawable


import Props
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.Layout
import android.text.TextPaint
import android.util.Log
import android.view.Gravity
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser
import com.demo.jsontoview.models.DrawableComponent

class IconDrawable(private val context: Context) : DrawableComponent {
    override var width: Int = 0
    override var height: Int = 0

    private var iconDrawable: Drawable? = null // Thay vì sử dụng Bitmap, chúng ta dùng Drawable
    private var leftPosition: Int = 0
    private var topPosition: Int = 0
    private var colorButton: String? = null

    fun setColor(color: String) {
        if (colorButton == color) {
            colorButton = null
            return
        }
        this.colorButton = color
    }


    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
    ) {
        val props = fView.props



        if (props.drawable?.data != null) {
            val resourceName = props.drawable.data
            val resourceId =
                context.resources.getIdentifier(resourceName, null, context.packageName)
            iconDrawable = context.getDrawable(resourceId) // Tải vector drawable
        }


        val iconWidth = (iconDrawable?.intrinsicWidth ?: 0)
        val iconHeight = (iconDrawable?.intrinsicHeight ?: 0)

        // Tính toán kích thước tổng của nút
        width = iconWidth
        height = iconHeight

    }

    override fun draw(canvas: Canvas, props: Props) {

        // Vẽ icon nếu có
        iconDrawable?.let { drawable: Drawable ->
            val iconLeft = leftPosition + props.padding.left
            val iconTop = topPosition + props.padding.top
            if (colorButton != null) {
                drawable.setTint(Color.parseColor(colorButton))
            } else {
                colorButton = props.drawable?.props?.textColor
            }
            drawable.setBounds(
                iconLeft,
                iconTop,
                iconLeft + drawable.intrinsicWidth,
                iconTop + drawable.intrinsicHeight
            )
            drawable.draw(canvas) // Vẽ icon
        }


    }

    override fun layout(left: Int, top: Int, fView: FView) {
        val totalWidth =
            fView.measureWidth - fView.props.margin.left - fView.props.margin.right - fView.props.padding.left - fView.props.padding.right
        val totalHeight =
            fView.measureHeight - fView.props.margin.top - fView.props.margin.bottom - fView.props.padding.top - fView.props.padding.bottom

        val widthDrawable = this.width
        val heightDrawable = this.height

        Log.e("ButtonDrawable", "layout: $widthDrawable $heightDrawable $totalWidth $totalHeight")

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
                leftPosition = totalWidth - fView.measureWidth
                topPosition = (totalHeight - heightDrawable) / 2
            }

            Gravity.BOTTOM -> {
                topPosition = totalHeight - fView.measureHeight
                leftPosition = (totalWidth - widthDrawable) / 2

            }

            Gravity.START -> {
                leftPosition = 0
            }
        }

    }


}
