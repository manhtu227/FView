package com.demo.jsontoview.drawable

import Props
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.demo.jsontoview.FTree
import com.demo.jsontoview.models.DrawableComponent

class ImageDrawable : DrawableComponent {
    private var imageBitmap: Bitmap? = null

    fun loadImageFromUrl(context: Context, url: String, onImageReady: () -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageBitmap = resource
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

        // Kiểm tra xem có size của ảnh hay không
        if (props.drawable?.props?.imageSize != null) {
            width = props.drawable.props.imageSize + props.padding.left + props.padding.right
            height = props.drawable.props.imageSize + props.padding.top + props.padding.bottom
        } else {
            // Tính toán kích thước view dựa trên ảnh
            if (props.width.value == ViewGroup.LayoutParams.MATCH_PARENT) {
                imageBitmap?.let {

                    val desiredWidth =
                        View.MeasureSpec.getSize(widthMeasureSpec) - props.padding.left - props.padding.right - props.margin.left - props.margin.right
                    val scaleFactor = desiredWidth / it.width.toFloat()
                    val desiredHeight = (it.height * scaleFactor).toInt()

                    // Cập nhật bitmap cho đúng kích thước mới
                    imageBitmap = Bitmap.createScaledBitmap(it, desiredWidth, desiredHeight, true)

                    imageBitmap?.let { bitmap ->
                        width = bitmap.width + props.padding.left + props.padding.right
                        height = bitmap.height + props.padding.top + props.padding.bottom
                    }
                }
            } else {
                width = (imageBitmap?.width ?: 0) + props.padding.left + props.padding.right
                height = (imageBitmap?.height ?: 0) + props.padding.top + props.padding.bottom
            }
        }

        // Không thay đổi width/height, chỉ điều chỉnh hiển thị ảnh bên trong
        imageBitmap?.let { bitmap ->
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            val widthDraw = width - props.padding.left - props.padding.right
            val heightDraw = height - props.padding.top - props.padding.bottom

            // Tùy chỉnh scaleType mà không thay đổi kích thước tổng thể của view
            when (props.drawable?.props?.scaleType) {
                ScaleType.FitXY -> {
                    imageBitmap = Bitmap.createScaledBitmap(bitmap, widthDraw, heightDraw, true)
                }

                ScaleType.CenterCrop -> {
                    val scaleFactor = maxOf(
                        widthDraw.toFloat() / bitmapWidth,
                        heightDraw.toFloat() / bitmapHeight
                    )
                    val scaledWidth = (bitmapWidth * scaleFactor).toInt()
                    val scaledHeight = (bitmapHeight * scaleFactor).toInt()

                    // Crop ảnh
                    imageBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                }

                ScaleType.CenterInside -> {
                    val scaleFactor = minOf(
                        widthDraw.toFloat() / bitmapWidth,
                        heightDraw.toFloat() / bitmapHeight
                    )
                    val scaledWidth = (bitmapWidth * scaleFactor).toInt()
                    val scaledHeight = (bitmapHeight * scaleFactor).toInt()

                    // Đặt lại ảnh với kích thước nhỏ hơn nhưng không vượt quá khung hình
                    imageBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                }

                else -> {
                    // Giữ nguyên kích thước ảnh (CENTER)
                    // Ảnh được hiển thị ở giữa khung hình mà không thay đổi
                }
            }
        }

        return Pair(width, height)
    }


    override fun draw(canvas: Canvas, width: Int, height: Int, props: Props) {
        canvas.save()
        imageBitmap?.let { bitmap ->

            var radius = props.drawable?.props?.radius
            val paint = Paint().apply {
                isAntiAlias = true
            }

            val rect = RectF(
                0F,
                0F,
                (width).toFloat(),
                (height).toFloat()
            )
            val bitmapRect = Rect(0, 0, bitmap.width, bitmap.height)
            val width = width - props.padding.left - props.padding.right
            val height = height - props.padding.top - props.padding.bottom

            if (radius != null) {

                if (radius == 0)
                    radius = 10

                val roundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvasBitmap = Canvas(roundBitmap)
                val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
                val roundRect = RectF(rectF)
                canvasBitmap.drawRoundRect(roundRect, radius.toFloat(), radius.toFloat(), paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvasBitmap.drawBitmap(bitmap, bitmapRect, rectF, paint)
                canvas.drawBitmap(roundBitmap, 0F,0F, null)
            }


        }
        canvas.restore()
    }

    override fun layout(left: Int, top: Int, fView: FTree) {
//        TODO("Not yet implemented")
    }
}

