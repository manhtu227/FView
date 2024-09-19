import com.google.gson.annotations.SerializedName

enum class TextType(val type: Int) {
    @SerializedName("0")
    Normal(0),

    @SerializedName("1")
    Bold(1),

    @SerializedName("2")
    Italic(2),

    @SerializedName("3")
    BoldItalic(3)
}

enum class ViewTypeConfig(val type: Int) {
    @SerializedName("1")
    RecyclerView(1),

    @SerializedName("2")
    ViewGroup(2)
}

enum class ScaleType(val value: Int) {
    @SerializedName("0")
    Center(0),

    @SerializedName("1")
    CenterCrop(1),

    @SerializedName("2")
    CenterInside(2),

    @SerializedName("3")
    FitXY(3),

    @SerializedName("4")
    FitStart(4),

    @SerializedName("5")
    FitEnd(5),

    @SerializedName("6")
    FitCenter(6)
}

enum class UnitConfig(val unit: Int) {
    @SerializedName("1")
    Dp(1),

    @SerializedName("2")
    Px(2),

    @SerializedName("3")
    Percent(3)
}

enum class GravityConfig(val value: Int) {
    @SerializedName("0")
    Left(0),

    @SerializedName("1")
    Top(1),

    @SerializedName("2")
    Right(2),

    @SerializedName("3")
    Bottom(3),

    @SerializedName("4")
    Center(4)
}

enum class OrientationConfig(val value: Int) {
    @SerializedName("0")
    Horizontal(0),

    @SerializedName("1")
    Vertical(1)
}

enum class LayoutType(val value: Int) {
    @SerializedName("0")
    Continues(0),

    @SerializedName("1")
    Stack(1)
}

enum class TypeConfig(val type: Int) {
    @SerializedName("1")
    Text(1),

    @SerializedName("2")
    Image(2),

    @SerializedName("3")
    Button(3),

    @SerializedName("4")
    ArrayImage(4),

    @SerializedName("5")
    Avatar(5)
}

data class DimensionConfig(
    @SerializedName("value") val value: Int,
    @SerializedName("unit") val unit: UnitConfig,
)

data class BackgroundConfig(
    @SerializedName("type") val type: Int,
    @SerializedName("color") val color: String,
)

data class TextViewConfig(
    @SerializedName("text") val text: String?,
    @SerializedName("fontSize") val fontSize: DimensionConfig?,
    @SerializedName("fontStyle") val fontStyle: Int?,
    @SerializedName("color") val color: String?,
)

data class PaddingConfig(
    @SerializedName("left") var left: Int,
    @SerializedName("right") var right: Int,
    @SerializedName("top") var top: Int,
    @SerializedName("bottom") var bottom: Int,
)

data class BorderConfig(
    @SerializedName("width") val width: Int,
    @SerializedName("color") val color: String,
)

data class PropsDrawable(
    @SerializedName("textSize") val textSize: Int,
    @SerializedName("textColor") val textColor: String,
    @SerializedName("textType") val textType: TextType,
    @SerializedName("background") val background: BackgroundConfig?,
    @SerializedName("scaleType") val scaleType: ScaleType,
    @SerializedName("padding") val padding: PaddingConfig,
    @SerializedName("margin") val margin: PaddingConfig,
    @SerializedName("gap") val gap: Int,
    @SerializedName("icon") val icon: String,
    @SerializedName("radius") val radius: Int?,
    @SerializedName("imageSize") val imageSize: Int?,
    @SerializedName("border") val border: BorderConfig?
)

sealed class DrawableData {
    data class Data(val data: String) : DrawableData()
    data class ArrayImage(val urls: List<String>) : DrawableData()
}


data class DrawableConfig(
    @SerializedName("type") val type: TypeConfig,
    @SerializedName("data") val data: String,
    @SerializedName("dataList") val dataList: List<String>,
    @SerializedName("props") val props: PropsDrawable,
)

enum class TypeClick(val type: Int) {
    @SerializedName("0")
    onClick(0),

    @SerializedName("1")
    OnLongPress(1),

    @SerializedName("2")
    OnDoubleClick(2)
}

data class ClickAction(
    @SerializedName("type") val type: TypeClick,
    @SerializedName("data") val data: String,
)

data class Props(
    @SerializedName("test") val test: String?,
    @SerializedName("width") val width: DimensionConfig,
    @SerializedName("height") val height: DimensionConfig,
    @SerializedName("background") val background: BackgroundConfig?,
    @SerializedName("gap") val gap: Int,
    @SerializedName("radius") val radius: Int?,
    @SerializedName("gravity") val gravity: Set<GravityConfig>?,
    @SerializedName("layoutGravity") val layoutGravity: Set<GravityConfig>?,
    @SerializedName("orientation") val orientation: OrientationConfig,
    @SerializedName("padding") val padding: PaddingConfig,
    @SerializedName("margin") val margin: PaddingConfig,
    @SerializedName("layoutType") val layoutType: LayoutType,
    @SerializedName("drawable") val drawable: DrawableConfig?,
    @SerializedName("clickAction") val clickAction: List<ClickAction>?,
)

//data class FView(
//    @SerializedName("viewType") val viewType: ViewTypeConfig,
//    @SerializedName("props") val props: Props,
//    @SerializedName("children") val children: List<FView> = emptyList()
//)
