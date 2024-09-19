package com.demo.jsontoview.drawable

import Props
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.demo.jsontoview.FTree
import com.demo.jsontoview.models.DrawableComponent
import kotlin.math.max
import kotlin.math.min

class ImageDrawable : DrawableComponent {
    private var imageBitmap: Bitmap? = null
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun loadImageFromUrl(context: Context, fView: FTree, url: String, onImageReady: () -> Unit) {
        imageBitmap = fView.imageBitmap
        if (imageBitmap != null) {
            paint = fView.paint
            Log.e("ImageDrawable", "loadImageFromUrl: $imageBitmap")
            fView.customViewGroup?.invalidate()
            return
        }
        Glide.with(context)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageBitmap = resource
                    fView.imageBitmap = imageBitmap
                    onImageReady()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FTree,
        props: Props,
    ): Pair<Int, Int> {
        var width = 0
        var height = 0

        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        if (heightMode == View.MeasureSpec.EXACTLY) {
            height = View.MeasureSpec.getSize(heightMeasureSpec)
        }

        if (props.width.unit == UnitConfig.Percent) {
            val widthParent =
                (fView.mParent?.widthSize
                    ?: 0) - (fView.mParent?.props?.margin?.left
                    ?: 0) - (fView.mParent?.props?.margin?.right
                    ?: 0) - (fView.mParent?.props?.padding?.left
                    ?: 0) - (fView.mParent?.props?.padding?.right
                    ?: 0) - props.padding.left - props.padding.right -(fView.mParent?.props?.gap
                    ?: 0)

            imageBitmap?.let {
                val desiredWidth = (widthParent * props.width.value / 100)
                val scaleFactor = desiredWidth / (it.width ?: 1).toFloat()
                val desiredHeight =
                    (when (props.height.value) {
                        ViewGroup.LayoutParams.MATCH_PARENT -> {
                            if (heightMode == View.MeasureSpec.EXACTLY) {
                                height - props.padding.top - props.padding.bottom
                            } else {
                                (it.height ?: 0)
                            }
                        }

                        ViewGroup.LayoutParams.WRAP_CONTENT -> {
                            (it.height ?: 0)
                        }

                        else -> {
                            it.height * scaleFactor
                        }
                    }).toInt()
                width = desiredWidth + props.padding.left + props.padding.right
                height = desiredHeight + props.padding.top + props.padding.bottom
            }
        } else if (props.width.value == ViewGroup.LayoutParams.MATCH_PARENT) {
            imageBitmap?.let {
                val desiredWidth =
                    View.MeasureSpec.getSize(widthMeasureSpec) - props.padding.left - props.padding.right - props.margin.left - props.margin.right - (fView.mParent?.props?.gap
                        ?: 0) * ((fView.mParent?.children?.size ?: 1) - 1)
                val scaleFactor = desiredWidth / it.width.toFloat()
                val desiredHeight = (it.height * scaleFactor).toInt()

                width = desiredWidth + props.padding.left + props.padding.right
                height = desiredHeight + props.padding.top + props.padding.bottom

            }
        } else if (props.width.value == ViewGroup.LayoutParams.WRAP_CONTENT) {
            width = (imageBitmap?.width ?: 0) + props.padding.left + props.padding.right
            height = (imageBitmap?.height ?: 0) + props.padding.top + props.padding.bottom
        } else {
            imageBitmap?.let {
                val desiredWidth = props.width.value
                val scaleFactor = width / it.width.toFloat()
                val desiredHeight = (when (props.height.value) {
                    ViewGroup.LayoutParams.MATCH_PARENT -> {
                        height
                    }

                    ViewGroup.LayoutParams.WRAP_CONTENT -> {
                        (it.height ?: 0)
                    }

                    else -> {
                        props.height.value
                    }
                }).toInt()

                width = desiredWidth + props.padding.left + props.padding.right
                height = desiredHeight + props.padding.top + props.padding.bottom
            }
        }

        imageBitmap?.let { bitmap ->
            val bitmapWidth = bitmap.width.toFloat()
            val bitmapHeight = bitmap.height.toFloat()

            val viewWidth = (width - props.padding.left - props.padding.right).toFloat()
            val viewHeight = (height - props.padding.top - props.padding.bottom).toFloat()

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
                    val matrix = Matrix()
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
        fView.paint = paint
        return Pair(width, height)
    }


    override fun draw(canvas: Canvas, width: Int, height: Int, props: Props) {
        canvas.save()
        imageBitmap?.let { bitmap ->
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

    override fun layout(left: Int, top: Int, fView: FTree) {
//        TODO("Not yet implemented")
    }
}

