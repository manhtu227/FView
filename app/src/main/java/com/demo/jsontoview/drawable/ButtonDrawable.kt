package com.demo.jsontoview.drawable


import Props
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.Layout
import android.text.TextPaint
import android.util.Log
import android.view.Gravity
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser
import com.demo.jsontoview.models.DrawableComponent

class ButtonDrawable(private val context: Context) : DrawableComponent {
    override var width: Int = 0
    override var height: Int = 0


    private var textPaint: TextPaint? = null
    private var backgroundPaint: Paint? = null
    private var borderPaint: Paint? = null

    private var iconDrawable: Drawable? = null // Thay vì sử dụng Bitmap, chúng ta dùng Drawable
    private var boringLayout: BoringLayout? = null
    private var leftPosition: Int = 0
    private var topPosition: Int = 0
    private var colorButton: String? = null

    fun setColor(color: String) {
        if (colorButton == color) {
            colorButton = null
            return
        }
        this.colorButton = color
//        textPaint?.color = Color.parseColor(color)
//        iconDrawable.let { drawable ->
//            drawable?.setTint(Color.parseColor(color))
//        }
    }


    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
    ) {
        val props = fView.props

        textPaint = TextPaint().apply {
            textSize = (Parser.parseDp(props.drawable?.props?.textSize ?: 24)).toFloat()
            color = Color.parseColor(colorButton ?: props.drawable?.props?.textColor)
            isAntiAlias = true
            typeface = props.drawable?.props?.textType?.let { font ->
                when (font) {
                    TextType.Bold -> android.graphics.Typeface.DEFAULT_BOLD
                    TextType.Italic -> android.graphics.Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.ITALIC
                    )

                    TextType.BoldItalic -> android.graphics.Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD_ITALIC
                    )

                    else -> android.graphics.Typeface.DEFAULT
                }
            }
        }

        if (props.drawable?.props?.icon != null) {
            val resourceName = props.drawable.props.icon
            val resourceId =
                context.resources.getIdentifier(resourceName, null, context.packageName)
            iconDrawable = context.getDrawable(resourceId) // Tải vector drawable
        }

        // Khởi tạo Paint cho viền (nếu có trong props)
//        props.drawable?.props?.border?.let {
//            borderPaint = Paint().apply {
//                color = Color.parseColor(it.color)
//                style = Paint.Style.STROKE
//                strokeWidth = it.width.toFloat()
//                isAntiAlias = true
//            }
//        }

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
        width =
            (boringLayout?.width
                ?: 0) + iconWidth + (props.drawable?.props?.gap
                ?: 0)
        height = maxOf(
            (boringLayout?.height ?: 0),
            iconHeight
        )

    }

    override fun draw(canvas: Canvas, props: Props) {
        // Vẽ nền của nút tùy thuộc vào trạng thái nhấn
        val paintToUse = backgroundPaint
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
            if (colorButton != null) {
                textPaint?.color = Color.parseColor(colorButton)
                drawable.setTint(Color.parseColor(colorButton))
            }else{
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


        val leftText =
            leftPosition + (iconDrawable?.intrinsicWidth ?: 0) + (props.drawable?.props?.gap ?: 0)
        // Vẽ văn bản
        textPaint?.let { paint ->
            canvas.save()
            canvas.translate(
                leftText.toFloat(),
                topPosition.toFloat()
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

    override fun layout(left: Int, top: Int, fView: FView) {
        val totalWidth =
            fView.measureWidth - fView.props.margin.left - fView.props.margin.right - fView.props.padding.left - fView.props.padding.right
        val totalHeight =
            fView.measureHeight - fView.props.margin.top - fView.props.margin.bottom - fView.props.padding.top - fView.props.padding.bottom

        val widthDrawable = this.width
        val heightDrawable = this.height

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
