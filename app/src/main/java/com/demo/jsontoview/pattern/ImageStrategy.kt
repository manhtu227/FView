package com.demo.jsontoview.pattern

import Props
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.demo.jsontoview.FView

interface ImageStrategy {
    fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
        props: Props,
    ): Pair<Int, Int>

    fun draw(canvas: Canvas,  width: Int, height: Int, props: Props)
}

class SimpleImageStrategy(private var imageBitmap: Bitmap) : ImageStrategy {

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
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

    override fun draw(canvas: Canvas,  width: Int, height: Int, props: Props) {
        imageBitmap?.let { bitmap ->

            var radius = props.drawable?.props?.radius
            val paint = Paint().apply {
                isAntiAlias = true
            }


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
    }

}

class TwoImageStrategy(private var arrayImage: MutableList<Bitmap?>) : ImageStrategy {
    private var firstImageBitmap: Bitmap? = null
    private var secondImageBitmap: Bitmap? = null

    init {
        if (arrayImage.size == 2) {
            firstImageBitmap = arrayImage[0]
            secondImageBitmap = arrayImage[1]
        }
    }

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
        props: Props,
    ): Pair<Int, Int> {
        var width = 0
        var height = 0
        if (props.drawable?.props?.imageSize != null) {
            width = props.drawable.props.imageSize + props.padding.left + props.padding.right
            height = props.drawable.props.imageSize + props.padding.top + props.padding.bottom
        } else {
            val widthScreen =
                View.MeasureSpec.getSize(widthMeasureSpec) - props.padding.left - props.padding.right - props.margin.left - props.margin.right
            var desiredWidth = 0
            var desiredHeight = 0
            firstImageBitmap?.let {
                desiredWidth = (widthScreen / 2) - 5
                val scaleFactor = desiredWidth / it.width.toFloat()
                val heightS = (it.height * scaleFactor).toInt()
                desiredHeight = if (heightS > desiredWidth) heightS else desiredWidth
                firstImageBitmap =
                    Bitmap.createScaledBitmap(it, desiredWidth, desiredHeight, true)
                firstImageBitmap = getRoundedCornerBitmap(firstImageBitmap!!, 10F)

            }
            secondImageBitmap?.let {
                secondImageBitmap =
                    Bitmap.createScaledBitmap(it, desiredWidth, desiredHeight, true)
                secondImageBitmap = getRoundedCornerBitmap(secondImageBitmap!!, 10F)
            }
            width = widthScreen + props.padding.left + props.padding.right
            height = desiredHeight + props.padding.top + props.padding.bottom

        }
        return Pair(width, height)
    }

    override fun draw(canvas: Canvas,  width: Int, height: Int, props: Props) {
        firstImageBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0F, 0F, null)
            secondImageBitmap?.let { bitmap ->
                canvas.drawBitmap(
                    bitmap,
                    (firstImageBitmap!!.width + 10).toFloat(),
                    0F,
                    null
                )
            }
        }

    }

}

class ThreeImageStrategy(private val arrayImage: MutableList<Bitmap?>) : ImageStrategy {
    private var firstImageBitmap: Bitmap? = null
    private var secondImageBitmap: Bitmap? = null
    private var thirdImageBitmap: Bitmap? = null
    private var checkedHorizontal: Boolean = false

    init {
        if (arrayImage.size == 3) {
            firstImageBitmap = arrayImage[0]
            secondImageBitmap = arrayImage[1]
            thirdImageBitmap = arrayImage[2]
        }
    }

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
        props: Props,
    ): Pair<Int, Int> {
        var width = 0
        var height = 0
        if (props.drawable?.props?.imageSize != null) {
            width = props.drawable.props.imageSize + props.padding.left + props.padding.right
            height = props.drawable.props.imageSize + props.padding.top + props.padding.bottom
        } else {
            val widthScreen =
                View.MeasureSpec.getSize(widthMeasureSpec)  - props.padding.left - props.padding.right - props.margin.left - props.margin.right
            var desiredWidth = 0
            var desiredHeight = 0
            firstImageBitmap?.let {
                if (it.width > it.height) {
                    desiredWidth = widthScreen
                    val scaleFactor = desiredWidth / it.width.toFloat()
                    val heightS = (it.height * scaleFactor).toInt()
                    desiredHeight = if (heightS > 715) heightS else 715

                    height += desiredHeight + 5
                    checkedHorizontal = false
                } else {
                    desiredWidth = ((widthScreen / 3) * 2) - 5
                    desiredHeight = desiredWidth
                    height = desiredHeight + 10
                    checkedHorizontal = true
                }

                firstImageBitmap =
                    Bitmap.createScaledBitmap(it, desiredWidth, desiredHeight, true)
                firstImageBitmap = getRoundedCornerBitmap(firstImageBitmap!!, 10F)

            }
            secondImageBitmap?.let { itSecond ->
                if (!checkedHorizontal) {
                    desiredWidth = (widthScreen / 2) - 5
                    desiredHeight = (desiredHeight / 2) - 5

                    height += desiredHeight + 5
                } else {
                    desiredWidth = (widthScreen / 3) - 5
                    desiredHeight = (desiredHeight / 2) - 5
                }
                secondImageBitmap =
                    Bitmap.createScaledBitmap(itSecond, desiredWidth, desiredHeight, true)
                secondImageBitmap = getRoundedCornerBitmap(secondImageBitmap!!, 10F)
                thirdImageBitmap?.let { itThird ->
                    thirdImageBitmap =
                        Bitmap.createScaledBitmap(itThird, desiredWidth, desiredHeight, true)
                    thirdImageBitmap = getRoundedCornerBitmap(thirdImageBitmap!!, 10F)
                }
            }
            width = widthScreen + props.padding.left + props.padding.right
            height += +props.padding.top + props.padding.bottom

        }
        return Pair(width, height)
    }

    override fun draw(canvas: Canvas, width: Int, height: Int, props: Props) {
        firstImageBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0F,0F, null)
            secondImageBitmap?.let { bitmap ->
                canvas.drawBitmap(
                    bitmap,
                    if (checkedHorizontal) (firstImageBitmap!!.width + 10).toFloat() else 0F,
                    if (checkedHorizontal)0F else (firstImageBitmap!!.height + 10).toFloat(),
                    null
                )
                thirdImageBitmap?.let { bitmap ->
                    canvas.drawBitmap(
                        bitmap,
                        if (checkedHorizontal) (firstImageBitmap!!.width + 10).toFloat() else (secondImageBitmap!!.width + 10).toFloat(),
                        if (checkedHorizontal) (secondImageBitmap!!.height + 10).toFloat() else (firstImageBitmap!!.height + 10).toFloat(),
                        null
                    )
                }
            }
        }
    }
}

class FourImageStrategy(private val arrayImage: MutableList<Bitmap?>) : ImageStrategy {
    private var firstImageBitmap: Bitmap? = null
    private var secondImageBitmap: Bitmap? = null
    private var thirdImageBitmap: Bitmap? = null
    private var fourthImageBitmap: Bitmap? = null
    private var checkedHorizontal: Boolean = false

    init {
        if (arrayImage.size == 4) {
            firstImageBitmap = arrayImage[0]
            secondImageBitmap = arrayImage[1]
            thirdImageBitmap = arrayImage[2]
            fourthImageBitmap = arrayImage[3]
        }
    }

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
        props: Props,
    ): Pair<Int, Int> {
        var width = 0
        var height = 0
        if (props.drawable?.props?.imageSize != null) {
            width = props.drawable.props.imageSize + props.padding.left + props.padding.right
            height = props.drawable.props.imageSize + props.padding.top + props.padding.bottom
        } else {
            val widthScreen =
                View.MeasureSpec.getSize(widthMeasureSpec)  - props.padding.left - props.padding.right - props.margin.left - props.margin.right
            var desiredWidth = 0
            var desiredHeight = 0
            firstImageBitmap?.let { it ->
                if (it.width > it.height) {
                    desiredWidth = widthScreen
                    val scaleFactor = desiredWidth / it.width.toFloat()
                    val heightS = (it.height * scaleFactor).toInt()
                    desiredHeight = if (heightS > 715) heightS else 715

                    height += desiredHeight + 5
                    checkedHorizontal = false
                } else {
                    desiredWidth = ((widthScreen / 3) * 2) - 5
                    desiredHeight = desiredWidth
                    height = desiredHeight + 10
                    checkedHorizontal = true
                }

                firstImageBitmap =
                    Bitmap.createScaledBitmap(it, desiredWidth, desiredHeight, true)
                firstImageBitmap = getRoundedCornerBitmap(firstImageBitmap!!, 10F)

            }
            secondImageBitmap?.let { itSecond ->
                if (!checkedHorizontal) {
                    desiredWidth = (widthScreen / 3) - 5
                    desiredHeight = (desiredHeight / 2) - 5

                    height += desiredHeight + 5
                } else {
                    desiredWidth = (widthScreen / 3) - 5
                    desiredHeight = (desiredHeight / 3) - 5
                }
                secondImageBitmap =
                    Bitmap.createScaledBitmap(itSecond, desiredWidth, desiredHeight, true)
                secondImageBitmap = getRoundedCornerBitmap(secondImageBitmap!!, 10F)
                thirdImageBitmap?.let { itThird ->
                    thirdImageBitmap =
                        Bitmap.createScaledBitmap(itThird, desiredWidth, desiredHeight, true)
                    thirdImageBitmap = getRoundedCornerBitmap(thirdImageBitmap!!, 10F)
                }
                fourthImageBitmap?.let { itFourth ->
                    fourthImageBitmap =
                        Bitmap.createScaledBitmap(itFourth, desiredWidth, desiredHeight, true)
                    fourthImageBitmap = getRoundedCornerBitmap(fourthImageBitmap!!, 10F)
                }
            }
            width = widthScreen + props.padding.left + props.padding.right
            height += +props.padding.top + props.padding.bottom

        }
        return Pair(width, height)
    }

    override fun draw(canvas: Canvas,  width: Int, height: Int, props: Props) {
        firstImageBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0F,0F, null)
            secondImageBitmap?.let { bitmap ->
                canvas.drawBitmap(
                    bitmap,
                    if (checkedHorizontal) (firstImageBitmap!!.width + 10).toFloat() else 0F,
                    if (checkedHorizontal)0F else (firstImageBitmap!!.height + 10).toFloat(),
                    null
                )
                thirdImageBitmap?.let { bitmap ->
                    canvas.drawBitmap(
                        bitmap,
                        if (checkedHorizontal) (firstImageBitmap!!.width + 10).toFloat() else (secondImageBitmap!!.width + 10 + thirdImageBitmap!!.height + 10).toFloat(),
                        if (checkedHorizontal) (secondImageBitmap!!.height + 10).toFloat() else (firstImageBitmap!!.height + 10).toFloat(),
                        null
                    )
                    fourthImageBitmap?.let { bitmap ->
                        canvas.drawBitmap(
                            bitmap,
                            if (checkedHorizontal) (firstImageBitmap!!.width + 10).toFloat() else (secondImageBitmap!!.width + 10).toFloat(),
                            if (checkedHorizontal) (secondImageBitmap!!.height + 10 +
                                    thirdImageBitmap!!.height + 10
                                    ).toFloat() else (firstImageBitmap!!.height + 10).toFloat(),
                            null
                        )
                    }
                }
            }
        }
    }
}


class FiveImageStrategy(private val arrayImage: MutableList<Bitmap?>) : ImageStrategy {
    private var firstImageBitmap: Bitmap? = null
    private var secondImageBitmap: Bitmap? = null
    private var thirdImageBitmap: Bitmap? = null
    private var fourthImageBitmap: Bitmap? = null
    private var fifthImageBitmap: Bitmap? = null
    private var checkedHorizontal: Boolean = false

    init {
        if (arrayImage.size == 5) {
            firstImageBitmap = arrayImage[0]
            secondImageBitmap = arrayImage[1]
            thirdImageBitmap = arrayImage[2]
            fourthImageBitmap = arrayImage[3]
            fifthImageBitmap = arrayImage[4]
        }
    }

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FView,
        props: Props,
    ): Pair<Int, Int> {
        var width = 0
        var height = 0
Log.e("FiveImageStrategy", "measure ${ View.MeasureSpec.getSize(widthMeasureSpec)}")
        val widthScreen =
            View.MeasureSpec.getSize(widthMeasureSpec)  - props.padding.left - props.padding.right - props.margin.left - props.margin.right
        var desiredWidth = 0
        var desiredHeight = 0
        firstImageBitmap?.let { it ->
            if (it.width > it.height) {
                desiredWidth = (widthScreen / 2) - 5
                desiredHeight = desiredWidth
                height += desiredHeight*2+10
                checkedHorizontal = false
            } else {
                desiredWidth = (widthScreen / 2) - 5
                desiredHeight = desiredWidth
                height = desiredHeight + 5
                checkedHorizontal = true
            }

            firstImageBitmap =
                Bitmap.createScaledBitmap(it, desiredWidth, desiredHeight, true)
            firstImageBitmap = getRoundedCornerBitmap(firstImageBitmap!!, 10F)

        }
        secondImageBitmap?.let { itSecond ->
//            if (firstImageBitmap!!.width > firstImageBitmap!!.height) {
//
//                height += desiredHeight + 5
//            }
            secondImageBitmap =
                Bitmap.createScaledBitmap(itSecond, desiredWidth, desiredHeight, true)
            secondImageBitmap = getRoundedCornerBitmap(secondImageBitmap!!, 10F)
            thirdImageBitmap?.let { itThird ->
                if (!checkedHorizontal) {
                    desiredHeight =( (desiredHeight *2)/3)-5
                } else {
                    desiredWidth = (widthScreen / 3)
                    desiredHeight = (desiredHeight / 2) - 10

                    height += desiredHeight + 5
                }
                thirdImageBitmap =
                    Bitmap.createScaledBitmap(itThird, desiredWidth, desiredHeight, true)
                thirdImageBitmap = getRoundedCornerBitmap(thirdImageBitmap!!, 10F)
            }
            fourthImageBitmap?.let { itFourth ->
                fourthImageBitmap =
                    Bitmap.createScaledBitmap(itFourth, desiredWidth, desiredHeight, true)
                fourthImageBitmap = getRoundedCornerBitmap(fourthImageBitmap!!, 10F)
            }
            fifthImageBitmap?.let { itFifth ->
                fifthImageBitmap =
                    Bitmap.createScaledBitmap(itFifth, desiredWidth, desiredHeight, true)
                fifthImageBitmap = getRoundedCornerBitmap(fifthImageBitmap!!, 10F)
            }
        }
        width = widthScreen + props.padding.left + props.padding.right
        height += +props.padding.top + props.padding.bottom

        return Pair(width, height)
    }

    override fun draw(canvas: Canvas, width: Int, height: Int, props: Props) {
        firstImageBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0F,0F, null)
            secondImageBitmap?.let { bitmap ->
                canvas.drawBitmap(
                    bitmap,
                    if (checkedHorizontal)
                        (firstImageBitmap!!.width + 10).toFloat() else 0F,
                    if (checkedHorizontal) 0F else (firstImageBitmap!!.height + 10).toFloat(),
                    null
                )
                thirdImageBitmap?.let { bitmap ->
                    canvas.drawBitmap(
                        bitmap,
                        if (checkedHorizontal) 0f else (firstImageBitmap!!.width + 10).toFloat(),
                        if (checkedHorizontal) (firstImageBitmap!!.height + 10).toFloat() else 0F,
                        null
                    )
                    fourthImageBitmap?.let { bitmap ->
                        canvas.drawBitmap(
                            bitmap,
                            if (checkedHorizontal) (thirdImageBitmap!!.width + 10).toFloat() else (firstImageBitmap!!.width + 10).toFloat(),
                            if (checkedHorizontal) (firstImageBitmap!!.height + 10).toFloat() else (thirdImageBitmap!!.height + 10).toFloat(),
                            null
                        )
                    }
                    fifthImageBitmap?.let { bitmap ->
                        canvas.drawBitmap(
                            bitmap,
                            if (checkedHorizontal) (  thirdImageBitmap!!.width + 10 + fourthImageBitmap!!.width + 10).toFloat() else (firstImageBitmap!!.width + 10).toFloat(),
                            if (checkedHorizontal) (
                                    firstImageBitmap!!.height + 10
                                    ).toFloat() else ( thirdImageBitmap!!.height + 10 + fourthImageBitmap!!.height + 10).toFloat(),
                            null
                        )
                    }
                }
            }
        }
    }
}


fun getRoundedCornerBitmap(bitmap: Bitmap, radius: Float): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
    canvas.drawRoundRect(rect, radius, radius, paint)

    return output
}