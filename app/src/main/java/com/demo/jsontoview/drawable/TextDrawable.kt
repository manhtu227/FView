package com.demo.jsontoview.drawable

import Props
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.View
import com.demo.jsontoview.FTree
import com.demo.jsontoview.models.DrawableComponent

class TextDrawable : DrawableComponent {
    private var staticLayout: StaticLayout? = null

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FTree,
        props: Props,
    ): Pair<Int, Int> {
        val textPaint = TextPaint().apply {
            textSize = (props.drawable?.props?.textSize ?: 24).toFloat()
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

        val widthMaxText =
            View.MeasureSpec.getSize(widthMeasureSpec) - props.padding.left - props.padding.right - props.margin.left - props.margin.right
        staticLayout = StaticLayout.Builder.obtain(
            props.drawable?.data ?: "", 0, (props.drawable?.data ?: "").length, textPaint,
            if (widthMaxText < 0) 0 else widthMaxText
        ).build()

        val width = staticLayout!!.width + props.padding.left + props.padding.right
        val height = staticLayout!!.height + props.padding.top + props.padding.bottom

        return Pair(width, height)
    }

    override fun draw(canvas: Canvas, width: Int, height: Int, props: Props) {
        staticLayout?.let {
            canvas.save()
            it.draw(canvas)
            canvas.restore()
        }
    }

    override fun layout(left: Int, top: Int, fView: FTree) {
        Log.e("TextDrawable", "layout: $left $top")
    }
}
