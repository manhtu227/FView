package com.demo.jsontoview

import DrawableConfig
import FView
import TypeConfig
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import java.net.URL
import kotlin.math.max
import kotlin.math.min

class CustomViewGroup(
    context: Context, attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private var fView: FView? = null

    fun setFView(fView: FView) {
        Log.e("CustomViewGroup", "setFView: $fView")
        this.fView = fView
    }

    init {
        setWillNotDraw(false)
    }

    private val childBounds = mutableListOf<Rect>()
    private val paddingChild = mutableListOf<Rect>()
    private val marginChild = mutableListOf<Rect>()
    private val layouts = mutableListOf<StaticLayout>()
    private val background = mutableListOf<String>()
    private val drawableArray = mutableListOf<DrawableConfig>()
    private val bitmaps = mutableListOf<Bitmap>()

    private var canvasWidth = 0
    private var canvasHeight = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 0
        var height = 0

        childBounds.clear()
        paddingChild.clear()
        layouts.clear()
        bitmaps.clear()
        background.clear()
        marginChild.clear()

        fView?.let { rootView ->
            // Đo kích thước phần tử bên trong đồng bộ
            val (internalWidth, internalHeight) = runBlocking {
                measureInternalElements(rootView, widthMeasureSpec)
            }

            val layoutWidth = Parser.parseDimension(rootView.props.width)
            val layoutHeight = Parser.parseDimension(rootView.props.height)

            width = when (layoutWidth) {
                ViewGroup.LayoutParams.MATCH_PARENT -> MeasureSpec.getSize(widthMeasureSpec)
                ViewGroup.LayoutParams.WRAP_CONTENT -> internalWidth
                else -> layoutWidth
            }
            height = when (layoutHeight) {
                ViewGroup.LayoutParams.MATCH_PARENT -> MeasureSpec.getSize(heightMeasureSpec)// mode = MeasureSpec.EXACTLY
                ViewGroup.LayoutParams.WRAP_CONTENT -> internalHeight
                else -> layoutHeight
            }

            Log.e("CustomViewGroup", "onMeasure19: $childBounds`, $height")
            Log.e("CustomViewGroup", "onMeasure32323: $width, $height")
            canvasWidth = width
            canvasHeight = height

            // Đảm bảo gọi setMeasuredDimension()
            setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec)
            )
        } ?: run {
            // Đặt kích thước mặc định nếu fView là null
            setMeasuredDimension(
                resolveSize(0, widthMeasureSpec),
                resolveSize(0, heightMeasureSpec)
            )
        }
    }

    private suspend fun measureInternalElements(
        rootView: FView,
        widthMeasureSpec: Int,
    ): Pair<Int, Int> {
        var width = 0
        var height = 0
        val drawableConfig = rootView.props.drawable
        val props = rootView.props
        val paddingHorizontalViewGroup = props.padding.left + props.padding.right
        val marginHorizontalViewGroup = props.margin.left + props.margin.right

        val drawableBounds = Rect()
        if (drawableConfig?.type == TypeConfig.Image) {
            val bitmap = loadImageFromUrl(drawableConfig.data as String) ?: return Pair(0, 0)
            bitmaps.add(bitmap)
            drawableBounds.set(0, 0, bitmap.width, bitmap.height)
            width = max(
                width,
                bitmap.width + drawableConfig.props.padding.left + drawableConfig.props.padding.right + drawableConfig.props.margin.left + drawableConfig.props.margin.right
            )
            height += bitmap.height + drawableConfig.props.padding.top + drawableConfig.props.padding.bottom + drawableConfig.props.margin.top + drawableConfig.props.margin.bottom
        } else if (drawableConfig?.type == TypeConfig.Text) {
            val textPaint = TextPaint().apply {
                textSize = drawableConfig.props.textSize.toFloat()
                color = Color.parseColor(drawableConfig.props.textColor)
            }

            val widthText =
                MeasureSpec.getSize(widthMeasureSpec)
//            - paddingHorizontalViewGroup - marginHorizontalViewGroup - (props.padding.left + props.padding.right) - drawableConfig.props.padding.left - drawableConfig.props.padding.right

            val staticLayout = StaticLayout.Builder.obtain(
                drawableConfig.data as String,
                0,
                drawableConfig.data.length,
                textPaint,
                if (widthText < 0) Resources.getSystem().displayMetrics.widthPixels
                else widthText
            ).build()

            layouts.add(staticLayout)
            drawableBounds.set(0, 0, staticLayout.width, staticLayout.height)
            width = max(
                width,
                staticLayout.width + paddingHorizontalViewGroup + marginHorizontalViewGroup + drawableConfig.props.padding.left + drawableConfig.props.padding.right + drawableConfig.props.margin.left + drawableConfig.props.margin.right
            )
            height += staticLayout.height + drawableConfig.props.padding.top + drawableConfig.props.padding.bottom + drawableConfig.props.margin.top + drawableConfig.props.margin.bottom
        }
//        background.add(drawableConfig.props.background.color)
        drawableArray.add(drawableConfig!!)

        paddingChild.add(
            Rect(
                drawableConfig.props.padding.left,
                drawableConfig.props.padding.top,
                drawableConfig.props.padding.right,
                drawableConfig.props.padding.bottom
            )
        )
        marginChild.add(
            Rect(
                drawableConfig.props.margin.left,
                drawableConfig.props.margin.top,
                drawableConfig.props.margin.right,
                drawableConfig.props.margin.bottom
            )
        )
        childBounds.add(drawableBounds)
        Log.e("CustomViewGroup", "height:  $height")

        val (childTotalWidth, childTotalHeight) = measureFViewRecursively(rootView, width, height)
        width = max(width, childTotalWidth)
        height = childTotalHeight

        Log.e("CustomViewGroup", "height final:  $height")

        return Pair(width, height)
    }

    private fun measureFViewRecursively(
        rootView: FView,
        totalWidth: Int,
        totalHeight: Int,
    ): Pair<Int, Int> {
        var totalWidth = totalWidth
        var totalHeight = totalHeight


        for (child in rootView.children) {
            val paddingHorizontalDrawable = (child.props.drawable?.props?.padding?.top
                ?: 0) + (child.props.drawable?.props?.padding?.bottom ?: 0)
            val marginHorizontalDrawable = (child.props.drawable?.props?.margin?.top
                ?: 0) + (child.props.drawable?.props?.margin?.bottom ?: 0)

            runBlocking {
                measureChild(child)
            }

            totalWidth = max(this.width, childBounds.last().width())
            totalHeight += childBounds.last()
                .height() + paddingHorizontalDrawable + marginHorizontalDrawable

            // Nếu child có children, đệ quy đo tiếp
            if (child.children.isNotEmpty()) {
                val (childTotalWidth, childTotalHeight) = measureFViewRecursively(
                    child,
                    totalWidth,
                    totalHeight
                )
                totalWidth = max(totalWidth, childTotalWidth)
                totalHeight = childTotalHeight
            }
        }

        return Pair(totalWidth, totalHeight)
    }

    private suspend fun measureChild(child: FView) {
        val bounds = Rect()
        val props = child.props
        val drawableConfig = props.drawable

        val paddingHorizontalViewGroup = props.padding.left + props.padding.right
        val marginHorizontalViewGroup = props.margin.left + props.margin.right

        if (drawableConfig?.type == TypeConfig.Image) {

            val bitmap = loadImageFromUrl(drawableConfig.data as String) ?: return
            bitmaps.add(bitmap)
            bounds.set(0, 0, bitmap.width, bitmap.height)

        } else if (drawableConfig?.type == TypeConfig.Text) {
            val textPaint = TextPaint().apply {
                textSize = drawableConfig.props.textSize.toFloat()
                color = Color.parseColor(drawableConfig.props.textColor)
            }

            val widthText =
                MeasureSpec.getSize(measuredWidth) - paddingHorizontalViewGroup - marginHorizontalViewGroup - (props.padding.left + props.padding.right) - drawableConfig.props.padding.left - drawableConfig.props.padding.right

            val staticLayout = StaticLayout.Builder.obtain(
                drawableConfig.data as String,
                0,
                drawableConfig.data.length,
                textPaint,
                if (widthText < 0) Resources.getSystem().displayMetrics.widthPixels
                else widthText
            ).build()

            layouts.add(staticLayout)

            bounds.set(0, 0, staticLayout.width, staticLayout.height)
        }

//        background.add(drawableConfig.props.background.color)
        drawableArray.add(drawableConfig!!)
        paddingChild.add(
            Rect(
                drawableConfig.props.padding.left,
                drawableConfig.props.padding.top,
                drawableConfig.props.padding.right,
                drawableConfig.props.padding.bottom
            )
        )
        marginChild.add(
            Rect(
                drawableConfig.props.margin.left,
                drawableConfig.props.margin.top,
                drawableConfig.props.margin.right,
                drawableConfig.props.margin.bottom
            )
        )

        childBounds.add(bounds)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var currentTop = (fView?.props?.padding?.top ?: 0) + (fView?.props?.margin?.top ?: 0)
        val currentLeft =
            left + (fView?.props?.padding?.left ?: 0) + (fView?.props?.margin?.left ?: 0)

        fView?.let { rootView ->
            val props = rootView.props
            val layoutType = props.layoutType
            val orientation = props.orientation

            // Xử lý cho các children nếu là ViewGroup
            if (rootView.viewType == ViewTypeConfig.ViewGroup) {

                val gravity = props.gravity
                currentTop = when (Parser.parseGravityForView(gravity)) {
                    Gravity.CENTER -> {
                        val parentHeight = bottom - top
                        val top = ((parentHeight - canvasHeight) / 2)
                        if (top < 0) currentTop else top
                    }

                    else -> currentTop
                }

                for ((index, bounds) in childBounds.withIndex()) {
                    val padding = paddingChild[index]
                    val margin = marginChild[index]

                    val (childLeft, childTop) = when (layoutType) {
                        LayoutType.Continues -> {
                            when (orientation) {
                                OrientationConfig.Horizontal -> {
                                    currentTop += padding.top + margin.top
                                    val left = padding.left + margin.left
                                    val top = currentTop
                                    currentTop += bounds.height() + padding.bottom + margin.bottom
                                    left to top
                                }

                                OrientationConfig.Vertical -> {
                                    currentTop += padding.top + margin.top
                                    val left = padding.left + margin.left
                                    val top = currentTop
                                    currentTop += bounds.height() + padding.bottom + margin.bottom
                                    left to top
                                }
                            }
                        }

                        LayoutType.Stack -> {
                            currentLeft + padding.left + margin.left to currentTop + padding.top + margin.top
                        }
                    }

                    val childRight = childLeft + bounds.width()
                    val childBottom = childTop + bounds.height()
                    bounds.left = childLeft
                    bounds.top = childTop
                    bounds.right = childRight
                    bounds.bottom = childBottom

                }
            }

        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var bitmapIndex = 0
        var layoutIndex = 0

        fView?.let { rootView ->
//            val props = rootView.props
//            val drawableConfig = props.drawable
            Log.e("CustomViewGroup", "onDraw111: ${childBounds.withIndex()}")

            for ((index, bounds) in childBounds.withIndex()) {
                val padding = paddingChild[index]
                val margin = marginChild[index]
                val background = background[index]
                val drawableConfig = drawableArray[index]
                Log.e("CustomViewGroup", "onLayout1122 background: $background")

                canvas.drawRect(
                    bounds.left.toFloat() - padding.left.toFloat(),
                    bounds.top.toFloat() - padding.top.toFloat(),
                    bounds.right.toFloat() + padding.right.toFloat() - margin.right.toFloat(),
                    bounds.bottom.toFloat() + padding.bottom.toFloat(),
                    Paint().apply {
                        color = Color.parseColor(background)
                    }
                )
                Log.e("CustomViewGroup", "onDraw: ${drawableConfig.type}")
                if (drawableConfig.type == TypeConfig.Image) {
                    val bitmap = bitmaps[bitmapIndex]
                    val scaleType = drawableConfig.props.scaleType

                    // Áp dụng scaleType
                    when (scaleType) {
                        ScaleType.Center -> {
                            val left = (bounds.width() - bitmap.width) / 2 + bounds.left
                            val top = (bounds.height() - bitmap.height) / 2 + bounds.top
                            canvas.drawBitmap(bitmap, left.toFloat(), top.toFloat(), null)
                        }

                        ScaleType.CenterCrop -> {
                            val scale = max(
                                bounds.width() / bitmap.width.toFloat(),
                                bounds.height() / bitmap.height.toFloat()
                            )
                            val scaledBitmapWidth = (bitmap.width * scale).toInt()
                            val scaledBitmapHeight = (bitmap.height * scale).toInt()
                            val left =
                                (bounds.width() - scaledBitmapWidth) / 2 + bounds.left
                            val top =
                                (bounds.height() - scaledBitmapHeight) / 2 + bounds.top
                            canvas.drawBitmap(
                                bitmap,
                                null,
                                Rect(left, top, left + scaledBitmapWidth, top + scaledBitmapHeight),
                                null
                            )
                        }

                        ScaleType.CenterInside -> {
                            val scale = min(
                                bounds.width() / bitmap.width.toFloat(),
                                bounds.height() / bitmap.height.toFloat()
                            )
                            val scaledBitmapWidth = (bitmap.width * scale).toInt()
                            val scaledBitmapHeight = (bitmap.height * scale).toInt()
                            val left =
                                (bounds.width() - scaledBitmapWidth) / 2 + bounds.left
                            val top =
                                (bounds.height() - scaledBitmapHeight) / 2 + bounds.top
                            canvas.drawBitmap(
                                bitmap,
                                null,
                                Rect(left, top, left + scaledBitmapWidth, top + scaledBitmapHeight),
                                null
                            )
                        }

                        ScaleType.FitXY -> {
                            canvas.drawBitmap(bitmap, null, bounds, null)
                        }

                        ScaleType.FitCenter -> {
                            val scale = min(
                                bounds.width() / bitmap.width.toFloat(),
                                bounds.height() / bitmap.height.toFloat()
                            )
                            val scaledBitmapWidth = (bitmap.width * scale).toInt()
                            val scaledBitmapHeight = (bitmap.height * scale).toInt()
                            val left = (bounds.width() - scaledBitmapWidth) / 2 + bounds.left
                            val top = (bounds.height() - scaledBitmapHeight) / 2 + bounds.top
                            canvas.drawBitmap(
                                bitmap,
                                null,
                                Rect(left, top, left + scaledBitmapWidth, top + scaledBitmapHeight),
                                null
                            )
                        }

                        ScaleType.FitStart -> TODO()
                        ScaleType.FitEnd -> TODO()
                    }
                    bitmapIndex++
                } else if (drawableConfig.type == TypeConfig.Text) {
                    Log.e("CustomViewGroup", "onLayout1122 onDraw111: $bounds")
                    if (layoutIndex >= layouts.size) return
                    val staticLayout = layouts[layoutIndex]
                    canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
                    canvas.save()
                    staticLayout.draw(canvas)
                    canvas.restore()
                    layoutIndex++
                }
            }
        }
    }
}

suspend fun loadImageFromUrl(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.doInput = true
            connection.connect()
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}