import android.util.Log
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser
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
    ViewGroup(2),

    @SerializedName("3")
    InputText(3)
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

enum class JustifyContentConfig(val value: Int) {
    @SerializedName("0")
    Start(0),

    @SerializedName("1")
    Center(1),

    @SerializedName("2")
    End(2),

    @SerializedName("3")
    SpaceBetween(3),

    @SerializedName("4")
    SpaceAround(4),

    @SerializedName("5")
    SpaceEvenly(5)
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
    Icon(4),

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
    @SerializedName("textSize") val textSize: Int? = null,
    @SerializedName("textMaxLines") val textMaxLines: Int? = null,
    @SerializedName("fontStyle") val fontStyle: Int? = null,
    @SerializedName("letterSpacing") val letterSpacing: Int? = null,
    @SerializedName("lineHeight") val lineHeight: Int? = null,
    @SerializedName("textAlign") val textAlign: Int? = null,
    @SerializedName("textColor") val textColor: String? = null,
    @SerializedName("textType") val textType: TextType? = null,
    @SerializedName("background") val background: BackgroundConfig? = null,
    @SerializedName("scaleType") val scaleType: ScaleType? = null,
    @SerializedName("gap") var gap: Int? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("radius") val radius: Int? = null,
    @SerializedName("imageSize") val imageSize: Int? = null,
    @SerializedName("border") val border: BorderConfig? = null,
)


data class DrawableConfig(
    @SerializedName("type") val type: TypeConfig,
    @SerializedName("data") var data: String,
    @SerializedName("props") val props: PropsDrawable,
)

enum class TypeAction(val type: Int) {
    @SerializedName("0")
    ShowComment(0),
}

data class ClickAction(
    @SerializedName("type") val type: TypeAction,
    @SerializedName("dataFView") val dataFView: FView?,
    @SerializedName("data") val data: String?,
)

data class Props(
    @SerializedName("id") val id: String? = null,
    @SerializedName("isComponent") val isComponent: Boolean? = null,
    @SerializedName("width") val width: DimensionConfig,
    @SerializedName("height") val height: DimensionConfig,
    @SerializedName("test") val test: String? = null,
    @SerializedName("background") val background: BackgroundConfig? = null,
    @SerializedName("border") val border: BorderConfig? = null,
    @SerializedName("gap") var gap: Int? = null,
    @SerializedName("radius") val radius: Int? = null,
    @SerializedName("gravity") val gravity: Set<GravityConfig>? = null,
    @SerializedName("layoutGravity") val layoutGravity: Set<GravityConfig>? = null,
    @SerializedName("orientation") val orientation: OrientationConfig,
    @SerializedName("padding") val padding: PaddingConfig = PaddingConfig(0, 0, 0, 0),
    @SerializedName("margin") val margin: PaddingConfig = PaddingConfig(0, 0, 0, 0),
    @SerializedName("layoutType") val layoutType: LayoutType,
    @SerializedName("drawable") val drawable: DrawableConfig? = null,
    @SerializedName("clickAction") val clickAction: List<ClickAction>? = null,
    @SerializedName("isClick") val isClick: Boolean? = null,
    @SerializedName("hintText") val hintText: String? = null,
    @SerializedName("justifyContent") val justifyContent: JustifyContentConfig? = null,
)

