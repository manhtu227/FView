import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.View.MeasureSpec
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.demo.jsontoview.CustomViewGroup2
import com.demo.jsontoview.Parser
import com.google.gson.annotations.SerializedName

class FView(
    @SerializedName("viewType") val viewType: ViewTypeConfig,
    @SerializedName("props") val props: Props,
    @SerializedName("children") val children: List<FView> = emptyList(),

    ) {
    var measuredWidthDrawable: Int = 0
    var measuredHeightDrawable: Int = 0

    var totalWidth: Int = 0
    var totalHeight: Int = 0


    private var leftPosition: Int = 0
    private var topPosition: Int = 0
    private var staticLayout: StaticLayout? = null
    private var imageBitmap: Bitmap? = null
    private var context: Context? = null
    private var customViewGroup: CustomViewGroup2? = null  // Thêm tham chiếu đến CustomViewGroup2


    private var padding: PaddingConfig = props.padding
    private var margin: PaddingConfig = props.margin

    fun setPaddingParent(padding: PaddingConfig) {
        Log.e("FView", "FView ne anh ơi22: $padding")
        this.padding = padding
    }

    fun setMarginParent(marginNow: PaddingConfig) {
        margin = marginNow
    }

    fun setCustomViewGroup(customViewGroup: CustomViewGroup2, context: Context) {
        this.context = context
        this.customViewGroup = customViewGroup

        if (props.drawable?.type == TypeConfig.Image) {
            loadImageFromUrl(props.drawable.data)
        }
    }


    // Hàm đo lường FView dựa trên props
    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutWidth = Parser.parseDimension(props.width)
        val layoutHeight = Parser.parseDimension(props.height)
        padding =
            if (padding != null) padding else props.padding
        margin =
            if (margin != null) margin else props.margin

        val (internalWidth, internalHeight) =
            measureDrawable(props, widthMeasureSpec, heightMeasureSpec)

        measuredWidthDrawable = when (layoutWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT -> MeasureSpec.getSize(widthMeasureSpec)
            ViewGroup.LayoutParams.WRAP_CONTENT -> internalWidth
            else -> layoutWidth
        }
        measuredHeightDrawable = when (layoutHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT -> MeasureSpec.getSize(heightMeasureSpec) // mode = MeasureSpec.EXACTLY
            ViewGroup.LayoutParams.WRAP_CONTENT -> internalHeight
            else -> layoutHeight
        }
        if (totalWidth == 0) totalWidth += measuredWidthDrawable
        if (totalHeight == 0) totalHeight += measuredHeightDrawable

        children.forEachIndexed { index, child ->
            child.setCustomViewGroup(customViewGroup!!, context!!)

            when (props.layoutType) {
                LayoutType.Continues -> {

                    if (props.orientation == OrientationConfig.Vertical) {

                        val paddingChild = PaddingConfig(
                            padding.left + child.props.padding.left,
                            padding.right + child.props.padding.right,
                            child.props.padding.top,
                            child.props.padding.bottom
                        )
                        val marginChild = PaddingConfig(
                            margin.left + child.props.margin.left,
                            margin.right + child.props.margin.right,
                            margin.top,
                            margin.bottom
                        )
                        if (index == 0) {
                            paddingChild.top = padding.top + child.props.padding.top
                            marginChild.top = margin.top + child.props.margin.top
                        }
                        if (index == children.size - 1) {
                            paddingChild.bottom = padding.bottom + child.props.padding.bottom
                            marginChild.bottom = margin.bottom + child.props.margin.bottom
                        }
                        child.setPaddingParent(paddingChild)
                        child.setMarginParent(marginChild)
                        child.measure(widthMeasureSpec, heightMeasureSpec)
                    } else if (props.orientation == OrientationConfig.Horizontal) {
                        val paddingChild = PaddingConfig(
                            padding.left,
                            padding.right,
                            child.props.padding.top + child.props.padding.top,
                            child.props.padding.bottom + child.props.padding.bottom
                        )
                        val marginChild = PaddingConfig(
                            margin.left,
                            margin.right,
                            margin.top + child.props.margin.top,
                            margin.bottom + child.props.margin.bottom
                        )
                        if (index == 0) {
                            paddingChild.left = padding.left + child.props.padding.left
                            marginChild.left = margin.left + child.props.margin.left
                        }
                        if (index == children.size - 1) {
                            paddingChild.right = padding.right + child.props.padding.right
                            marginChild.right = margin.right + child.props.margin.right
                        }
                        child.setPaddingParent(paddingChild)
                        child.setMarginParent(marginChild)
                        child.measure(widthMeasureSpec, heightMeasureSpec)
                    }

                }

                LayoutType.Stack -> {
                    child.measure(widthMeasureSpec, heightMeasureSpec)
                }
            }

            when (props.layoutType) {
                LayoutType.Continues -> {
                    // Nếu là Continues, cộng dồn chiều rộng hoặc chiều cao của các child
                    if (props.orientation == OrientationConfig.Vertical) {

                        totalHeight += child.measuredHeightDrawable + margin.top + margin.bottom
                        totalWidth =
                            maxOf(
                                totalWidth,
                                child.measuredWidthDrawable
                            )  // Chiều rộng lớn nhất cho vertical
                    } else if (props.orientation == OrientationConfig.Horizontal) {
                        totalWidth += child.measuredWidthDrawable
                        totalHeight = maxOf(
                            totalHeight,
                            child.measuredHeightDrawable + margin.top + margin.bottom
                        )  // Chiều cao lớn nhất cho horizontal
                    }
                }

                LayoutType.Stack -> {
                    // Nếu là Stack, lấy giá trị lớn nhất
                    totalWidth = maxOf(totalWidth, child.measuredWidthDrawable)
                    totalHeight = maxOf(totalHeight, child.measuredHeightDrawable)
                }
            }
        }
    }

    private fun measureDrawable(props: Props, widthMeasureSpec: Int, heightMeasureSpec: Int)
            : Pair<Int, Int> {
        var width = 0
        var height = 0

        if (props.drawable?.type == TypeConfig.Text) {
            val textPaint = TextPaint().apply {
                textSize = props.drawable.props.textSize.toFloat()
                color = Color.parseColor(props.drawable.props.textColor)
            }
            val widthMaxText =
                MeasureSpec.getSize(widthMeasureSpec) - padding.left - padding.right - margin.left - margin.right
            staticLayout = StaticLayout.Builder.obtain(
                props.drawable.data,
                0,
                props.drawable.data.length,
                textPaint,
                if (widthMaxText < 0) 0 else widthMaxText
            ).setText(props.drawable.data).build()

            height =
                staticLayout!!.height + props.padding.top + props.padding.bottom
            width =
                staticLayout!!.width + props.padding.left + props.padding.right
        } else if (
            props.drawable?.type == TypeConfig.Image
        ) {
            height = (imageBitmap?.height ?: 0) + props.padding.top + props.padding.bottom
            width = (imageBitmap?.width ?: 0) + props.padding.left + props.padding.right
        }

        return Pair(width, height)
    }

    // Hàm để bố trí FView (đặt vị trí cho các child)
    fun layout(left: Int, top: Int) {
        leftPosition = left
        topPosition = top
        Log.e("FView", "measure layout: $imageBitmap")

        var currentOffset = 0

        // Bố trí từng child dựa trên LayoutType và orientation
        when (props.layoutType) {
            LayoutType.Continues -> {
                if (props.orientation == OrientationConfig.Vertical) {
                    children.forEach { child ->
                        child.layout(
                            leftPosition,
                            topPosition + currentOffset
                        )
                        currentOffset += child.measuredHeightDrawable + child.props.margin.top + child.props.margin.bottom
                    }
                } else if (props.orientation == OrientationConfig.Horizontal) {
                    children.forEach { child ->
                        child.layout(
                            leftPosition + props.padding.left + currentOffset,
                            topPosition + props.padding.top
                        )
                        currentOffset += child.measuredWidthDrawable + child.props.margin.left + child.props.margin.right

                    }
                }
            }

            LayoutType.Stack -> {
                children.forEach { child ->
                    child.layout(
                        leftPosition + props.padding.left,
                        topPosition + props.padding.top
                    )
                }
            }
        }

        Log.d(
            "FView",
            "layout: $leftPosition, $topPosition, width: $measuredWidthDrawable, height: $measuredHeightDrawable"
        )


    }

    // Hàm để vẽ FView lên Canvas
    fun draw(canvas: Canvas) {
//        Log.e("FView", "measure canvas: ${props.background.color} $totalWidth $totalHeight")
        canvas.save()

        canvas.translate(
            leftPosition.toFloat() + props.margin.left,
            topPosition.toFloat() + props.margin.top
        )


        // Vẽ nền của FView
        val paint = Paint().apply {
//            color = Color.parseColor(props.background.color)
        }
        canvas.drawRect(
            0f,
            0f,
            totalWidth.toFloat(),
            totalHeight.toFloat(),
            paint
        )

        // Vẽ drawable (text hoặc image)
        props.drawable.let { drawable ->
            when (drawable?.type) {
                TypeConfig.Text -> drawText(canvas, drawable)
                TypeConfig.Image -> drawImage(canvas, drawable)
                TypeConfig.Button -> TODO()
                TypeConfig.ArrayImage -> TODO()
                TypeConfig.Avatar -> TODO()
                null -> TODO()
            }
        }

        // Vẽ các child
        children.forEach { child ->
            child.draw(canvas)
        }

        canvas.restore()
    }

    fun requestLayout() {
//        measure(measuredWidthDrawable, measuredHeightDrawable)
//        layout(leftPosition, topPosition)
        customViewGroup?.requestLayout()
        customViewGroup?.invalidate()

//
    }

    // Vẽ text nếu drawable là dạng text
    private fun drawText(canvas: Canvas, drawable: DrawableConfig) {
        val textPaint = TextPaint().apply {
            textSize = drawable.props.textSize.toFloat()
            color = Color.parseColor(drawable.props.textColor)
        }

        // Vẽ text từ StaticLayout
        staticLayout?.let {
            canvas.save()
            canvas.translate(props.padding.left.toFloat(), props.padding.top.toFloat())
            it.draw(canvas)
            canvas.restore()
        }
    }

    // Vẽ image placeholder nếu drawable là dạng image
    private fun drawImage(canvas: Canvas, drawable: DrawableConfig) {
        imageBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } ?: run {
            val paint = Paint().apply {
                textSize = drawable.props.textSize.toFloat()
                color = Color.RED
            }
            canvas.drawText("Image Placeholder", 0f, drawable.props.textSize.toFloat(), paint)
        }
    }

    private fun loadImageFromUrl(url: String) {
        Log.e("FView", "loadImageFromUrl: $context")
        Glide.with(context!!)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageBitmap = resource
                    Log.e("FView", "onResourceReady: $resource")
                    requestLayout()
                    // Vẽ lại toàn bộ view hoặc phần cụ thể
                    // Nếu bạn sử dụng custom view, cần cách để yêu cầu vẽ lại
                    // Ví dụ: invalidate() nếu bạn đang sử dụng View
                    // Hoặc thông báo cho hệ thống của bạn biết rằng cần phải vẽ lại phần canvas chứa ảnh
                    // parent.invalidate() hoặc tương tự nếu có parent view
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle the case when image loading is cleared
                }
            })
    }
}
