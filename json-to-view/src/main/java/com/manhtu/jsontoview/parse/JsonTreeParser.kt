package com.manhtu.jsontoview.parse

import com.manhtu.jsontoview.model.Dimension
import com.manhtu.jsontoview.model.DimUnit
import com.manhtu.jsontoview.model.EdgeInsets
import com.manhtu.jsontoview.model.FNode
import com.manhtu.jsontoview.model.NodeAction
import com.manhtu.jsontoview.model.NodeKind
import com.manhtu.jsontoview.model.NodeProps
import com.manhtu.jsontoview.model.TreeSource
import com.manhtu.jsontoview.model.TreeSpec
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Maps JSON trees into pure [FNode] model.
 *
 * Supports:
 * - **Stable 0.2+** schema: `"type": "column"|"row"|…` + `props.imageUrl` / `props.action`
 * - **Legacy** `viewType` / `drawable` trees (sample feed)
 */
object JsonTreeParser {

    fun parse(json: String): FNode {
        val root = JsonParser.parseString(json).asJsonObject
        return parseNode(root)
    }

    fun parseTreeSpec(json: String, name: String = "feed"): TreeSpec =
        TreeSpec(
            name = name,
            root = parse(json),
            source = TreeSource.FEED_JSON,
        )

    private fun parseNode(obj: JsonObject): FNode {
        val propsObj = obj.getAsJsonObjectOrNull("props")
        val childrenJson = obj.getAsJsonArrayOrNull("children")
        val children = childrenJson?.mapNotNull { el ->
            if (el.isJsonObject) parseNode(el.asJsonObject) else null
        }.orEmpty()

        val stableType = obj.get("type")?.asStringOrNull()?.lowercase()
        val viewType = obj.getIntOr("viewType", 2)
        val orientation = propsObj?.getIntOr("orientation", 1) ?: 1
        val layoutType = propsObj?.getIntOr("layoutType", 0) ?: 0
        val drawable = propsObj?.getAsJsonObjectOrNull("drawable")
        val drawableType = drawable?.getIntOr("type", -1) ?: -1
        val hasMeaningfulChildren = children.isNotEmpty()

        val kind = when {
            stableType != null -> kindFromStableType(stableType)
            viewType == 1 -> NodeKind.LIST
            viewType == 3 -> NodeKind.BOX
            layoutType == 1 -> NodeKind.STACK
            drawableType == 1 && !hasMeaningfulChildren -> NodeKind.TEXT
            drawable != null && drawableType != 1 && !hasMeaningfulChildren -> NodeKind.BOX
            viewType == 2 && orientation == 0 -> NodeKind.ROW
            viewType == 2 -> NodeKind.COLUMN
            else -> NodeKind.BOX
        }

        val width = parseDimension(propsObj?.getAsJsonObjectOrNull("width"), Dimension.MATCH)
        val height = parseDimension(propsObj?.getAsJsonObjectOrNull("height"), Dimension.WRAP)
        val padding = parseInsets(propsObj?.getAsJsonObjectOrNull("padding"))
        val margin = parseInsets(propsObj?.getAsJsonObjectOrNull("margin"))
        val gap = propsObj?.getIntOr("gap", 0) ?: 0
        val backgroundColor = parseBackgroundColor(propsObj)
        val cornerRadius = propsObj?.get("cornerRadius")?.asFloatOrNull() ?: 0f

        var text: String? = propsObj?.get("text")?.asStringOrNull()
        var textSizeSp = propsObj?.get("textSizeSp")?.asFloatOrNull()
            ?: propsObj?.get("textSize")?.asFloatOrNull()
            ?: 14f
        var textColor = parseColor(propsObj?.get("textColor")?.asStringOrNull()) ?: 0xFF000000.toInt()

        if (kind == NodeKind.TEXT && drawable != null) {
            text = text ?: drawable.get("data")?.asStringOrNull()
            val dProps = drawable.getAsJsonObjectOrNull("props")
            textSizeSp = dProps?.get("textSize")?.asFloatOrNull() ?: textSizeSp
            textColor = parseColor(dProps?.get("textColor")?.asStringOrNull()) ?: textColor
        }

        var imageUrl = propsObj?.get("imageUrl")?.asStringOrNull()
        if (imageUrl.isNullOrBlank() && drawable != null && drawableType != 1) {
            // Legacy image-like drawable: treat data as URL when it looks like one
            val data = drawable.get("data")?.asStringOrNull()
            if (!data.isNullOrBlank() && (data.startsWith("http") || data.startsWith("content:"))) {
                imageUrl = data
            }
        }

        val action = parseAction(propsObj?.getAsJsonObjectOrNull("action"))

        val resolvedBackground = when {
            backgroundColor != null -> backgroundColor
            kind == NodeKind.BOX && drawable != null && drawableType != 1 && imageUrl.isNullOrBlank() ->
                0xFF888888.toInt()
            else -> null
        }

        val props = NodeProps(
            id = propsObj?.get("id")?.asStringOrNull(),
            width = width,
            height = height,
            padding = padding,
            margin = margin,
            gap = gap,
            backgroundColor = resolvedBackground,
            cornerRadius = cornerRadius,
            text = text,
            textSizeSp = textSizeSp,
            textColor = textColor,
            imageUrl = imageUrl,
            contentDescription = propsObj?.get("contentDescription")?.asStringOrNull(),
            action = action,
        )

        return FNode(kind = kind, props = props, children = children)
    }

    private fun kindFromStableType(type: String): NodeKind = when (type) {
        "row" -> NodeKind.ROW
        "column" -> NodeKind.COLUMN
        "stack" -> NodeKind.STACK
        "box" -> NodeKind.BOX
        "text" -> NodeKind.TEXT
        "list" -> NodeKind.LIST
        else -> NodeKind.BOX
    }

    private fun parseAction(obj: JsonObject?): NodeAction? {
        if (obj == null) return null
        val type = obj.get("type")?.asStringOrNull() ?: return null
        val payload = obj.get("payload")?.asStringOrNull()
        return NodeAction(type = type, payload = payload)
    }

    private fun parseDimension(obj: JsonObject?, default: Dimension): Dimension {
        if (obj == null) return default
        val value = obj.getIntOr("value", default.value)
        val unit = parseUnit(obj.get("unit"), DimUnit.DP)
        return when (value) {
            -1 -> Dimension.MATCH
            -2 -> Dimension.WRAP
            else -> Dimension(value, unit)
        }
    }

    private fun parseUnit(el: JsonElement?, default: DimUnit): DimUnit {
        if (el == null || !el.isJsonPrimitive) return default
        val p = el.asJsonPrimitive
        if (p.isNumber) {
            return when (p.asInt) {
                2 -> DimUnit.PX
                3 -> DimUnit.PERCENT
                else -> DimUnit.DP
            }
        }
        if (p.isString) {
            return when (p.asString.lowercase()) {
                "px" -> DimUnit.PX
                "percent", "%" -> DimUnit.PERCENT
                else -> DimUnit.DP
            }
        }
        return default
    }

    private fun parseInsets(obj: JsonObject?): EdgeInsets {
        if (obj == null) return EdgeInsets.ZERO
        return EdgeInsets(
            left = obj.getIntOr("left", 0),
            top = obj.getIntOr("top", 0),
            right = obj.getIntOr("right", 0),
            bottom = obj.getIntOr("bottom", 0),
        )
    }

    private fun parseBackgroundColor(props: JsonObject?): Int? {
        props?.get("backgroundColor")?.asStringOrNull()?.let { return parseColor(it) }
        val bg = props?.getAsJsonObjectOrNull("background") ?: return null
        return parseColor(bg.get("color")?.asStringOrNull())
    }

    fun parseColor(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim().removePrefix("#")
        return try {
            when (s.length) {
                6 -> (0xFF000000 or s.toLong(16)).toInt()
                8 -> s.toLong(16).toInt()
                else -> null
            }
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun JsonObject.getIntOr(key: String, default: Int): Int {
        val el = get(key) ?: return default
        return try {
            if (el.isJsonPrimitive && el.asJsonPrimitive.isNumber) el.asInt else default
        } catch (_: Exception) {
            default
        }
    }

    private fun JsonObject.getAsJsonObjectOrNull(key: String): JsonObject? {
        val el = get(key) ?: return null
        return if (el.isJsonObject) el.asJsonObject else null
    }

    private fun JsonObject.getAsJsonArrayOrNull(key: String): com.google.gson.JsonArray? {
        val el = get(key) ?: return null
        return if (el.isJsonArray) el.asJsonArray else null
    }

    private fun JsonElement.asStringOrNull(): String? =
        try {
            if (isJsonPrimitive) asString else null
        } catch (_: Exception) {
            null
        }

    private fun JsonElement.asFloatOrNull(): Float? =
        try {
            if (isJsonPrimitive && asJsonPrimitive.isNumber) asFloat else null
        } catch (_: Exception) {
            null
        }
}
