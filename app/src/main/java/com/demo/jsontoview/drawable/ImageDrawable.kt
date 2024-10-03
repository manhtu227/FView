package com.demo.jsontoview.drawable

import Props
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser
import com.demo.jsontoview.helpers.HelperDrawable
import com.demo.jsontoview.models.DrawableComponent
import kotlin.math.max
import kotlin.math.min

class ImageDrawable : DrawableComponent {

    override var width: Int = 0

    override var height: Int = 0

    private var imageBitmap: Bitmap? = null
//    private var widthImage: Int? = null
//    private var heightImage: Int? = null

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun loadImageFromUrl(fView: FView, url: String) {

        Glide.with(fView.customViewGroup!!.context!!)
            .asBitmap()
            .load(url)
            .signature(ObjectKey(url))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    if (imageBitmap == null) {
                        imageBitmap = resource
//                        fView.cache.clearAll()
//                        fView.mParent?.cache?.clearAll()
//                        widthImage = resource.width
//                        heightImage = resource.height

                        fView.customViewGroup?.requestLayout()
                        fView.customViewGroup?.invalidate()
                        return
                    }
//                    imageBitmap = resource
//                    widthImage = resource.width
//                    heightImage = resource.height
//                    fView.customViewGroup?.requestLayout()
                    fView.customViewGroup?.invalidate()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
    ) {
        val props = fView.props

        var widthExpect = imageBitmap?.width ?: 0
        var heightExpect = imageBitmap?.height ?: 0

        val layoutWidth = Parser.parseDimension(props.width)
        val layoutHeight = Parser.parseDimension(props.height)
        var scaleFactor:Float = 1f

        val helperWidth = HelperDrawable(props, widthMeasureSpec)

        when (layoutWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                helperWidth.matchParent(widthExpect)
                widthExpect.let {
                    scaleFactor = helperWidth.value.toFloat() / (if (widthExpect == 0) 1 else it!!)
                    if(fView.props.id=="be"){
                        Log.e("ImageDrawable","widthExpect: ${helperWidth.value / 640} $widthExpect scaleFactor: $scaleFactor")
                    }
                }

                width = helperWidth.value
            }

            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                widthExpect?.let {
                    helperWidth.wrapContent(widthExpect)
                    width = helperWidth.value
                }
            }

            else -> {
                if (props.width.unit == UnitConfig.Percent) {
                    helperWidth.unitPercent();
                    val widthParent =
                        helperWidth.value
                    width = (widthParent * props.width.value / 100)
                } else {
                    width = layoutWidth
                }
            }
        }

        val helperHeight = HelperDrawable(props, heightMeasureSpec)
        when (layoutHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                helperHeight.matchParent(heightExpect)
                height = (helperHeight.value * scaleFactor).toInt()
//                height = helperHeight.value
            }

            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                helperHeight.wrapContent(heightExpect)
                height = helperHeight.value
            }

            else -> {
                if (props.height.unit == UnitConfig.Percent) {
                    helperHeight.unitPercent();
                    val heightParent = helperHeight.value
                    height = (heightParent * props.height.value / 100)
                } else {
                    height = layoutHeight
                }
            }
        }

        // scale type
        scaleTypeLayout(props)


    }


    override fun draw(canvas: Canvas, props: Props) {
        canvas.save()
        imageBitmap?.let {
            var radius = props.drawable?.props?.radius ?: 0

            if (radius == 0)
                radius = 10
            val rect = RectF(
                0F,
                0F,
                (width).toFloat(),
                (height).toFloat()
            )
            canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paint)
        }
        canvas.restore()
    }

    override fun layout(left: Int, top: Int, fView: FView) {

    }

    private fun scaleTypeLayout(props: Props) {
        imageBitmap?.let { bitmap ->
            val bitmapWidth = bitmap.width!!.toFloat()
            val bitmapHeight = bitmap.height!!.toFloat()

            val viewWidth = (width).toFloat()
            val viewHeight = (height).toFloat()
            val matrix = Matrix()
            // Tùy chỉnh scaleType mà không thay đổi kích thước tổng thể của view
            when (props.drawable?.props?.scaleType) {
                ScaleType.FitXY -> {
                    val matrix = Matrix()

                    matrix.setScale(viewWidth / bitmapWidth, viewHeight / bitmapHeight)

                    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    shader.setLocalMatrix(matrix)
                    paint.shader = shader
                }

                ScaleType.CenterCrop -> {
                    val matrix = Matrix()

                    // Tính toán scale cho CENTER_CROP
                    val scale = max(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
                    val scaledWidth = scale * bitmapWidth
                    val scaledHeight = scale * bitmapHeight

                    // Đặt matrix cho CENTER_CROP
                    matrix.setScale(scale, scale)

                    // Dịch ảnh vào giữa khung
                    val dx = (viewWidth - scaledWidth) / 2
                    val dy = (viewHeight - scaledHeight) / 2
                    matrix.postTranslate(dx, dy)

                    // Sử dụng BitmapShader để áp dụng shader cho Paint
                    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    shader.setLocalMatrix(matrix)
                    paint.shader = shader

                }

                ScaleType.CenterInside -> {
                    val matrix = Matrix()

                    val scale = min(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
                    val scaledWidth = scale * bitmapWidth
                    val scaledHeight = scale * bitmapHeight

                    matrix.setScale(scale, scale)

                    val dx = (viewWidth - scaledWidth) / 2
                    val dy = (viewHeight - scaledHeight) / 2
                    matrix.postTranslate(dx, dy)

                    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    shader.setLocalMatrix(matrix)
                    paint.shader = shader
                }

                ScaleType.Center -> {
                    val dx = (viewWidth - bitmapWidth) / 2
                    val dy = (viewHeight - bitmapHeight) / 2
                    matrix.postTranslate(dx, dy)

                    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    shader.setLocalMatrix(matrix)
                    paint.shader = shader
                }


                else -> {
                    val matrix = Matrix()

                    val scale = min(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
                    val scaledWidth = scale * bitmapWidth
                    val scaledHeight = scale * bitmapHeight

                    matrix.setScale(scale, scale)

                    val dx = (viewWidth - scaledWidth) / 2
                    val dy = (viewHeight - scaledHeight) / 2
                    matrix.postTranslate(dx, dy)

                    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    shader.setLocalMatrix(matrix)
                    paint.shader = shader
                }
            }
        }
    }
}
