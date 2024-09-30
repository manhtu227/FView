package com.demo.jsontoview.drawable

import Props
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.View
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser
import com.demo.jsontoview.models.DrawableComponent

class TextDrawable : DrawableComponent {
    override var width: Int = 0
    override var height: Int = 0

    private var staticLayout: StaticLayout? = null

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
    ) {
        val props = fView.props
        val textPaint = TextPaint().apply {
            textSize = (Parser.parseDp(props.drawable?.props?.textSize ?: 24)).toFloat()
            color = Color.parseColor(props.drawable?.props?.textColor)
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

        val measuredTextWidth = textPaint.measureText(props.drawable?.data)

        val widthMaxText =
            View.MeasureSpec.getSize(widthMeasureSpec) - props.padding.left - props.padding.right - props.margin.left - props.margin.right

        val finalWidth =
            if (measuredTextWidth < widthMaxText) measuredTextWidth.toInt() else widthMaxText

        val tempLayout = StaticLayout.Builder.obtain(
            props.drawable?.data ?: "", 0, (props.drawable?.data ?: "").length, textPaint,
            finalWidth
        ).build()

        if (props.drawable?.props?.textMaxLines != null && tempLayout.lineCount > props.drawable.props.textMaxLines) {
            val lastLineIndex = tempLayout.getLineEnd(props.drawable.props.textMaxLines-1)
            val truncatedText =
                (props.drawable.data ?: "").substring(0, lastLineIndex - 3).plus("...")

            staticLayout = StaticLayout.Builder.obtain(
                truncatedText, 0, truncatedText.length, textPaint,
                finalWidth
            ).build()
        } else {
            staticLayout = tempLayout
        }


        width = staticLayout!!.width
        height = staticLayout!!.height

    }

    override fun draw(canvas: Canvas, props: Props) {
        staticLayout?.let {
            canvas.save()
            it.draw(canvas)
            canvas.restore()
        }
    }

    override fun layout(left: Int, top: Int, fView: FView) {
        Log.e("TextDrawable", "layout: $left $top")
    }
}
