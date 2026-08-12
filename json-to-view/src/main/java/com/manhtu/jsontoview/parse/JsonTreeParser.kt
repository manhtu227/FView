package com.manhtu.jsontoview.parse

import com.manhtu.jsontoview.model.Dimension
import com.manhtu.jsontoview.model.DimUnit
import com.manhtu.jsontoview.model.EdgeInsets
import com.manhtu.jsontoview.model.FNode
import com.manhtu.jsontoview.model.NodeKind
import com.manhtu.jsontoview.model.NodeProps
import com.manhtu.jsontoview.model.TreeSource
import com.manhtu.jsontoview.model.TreeSpec
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Maps legacy view.json trees into pure [FNode] model.
 * Tolerant of missing fields; unknown props ignored.
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
        val viewType = obj.getIntOr("viewType", 2)
        val childrenJson = obj.getAsJsonArrayOrNull("children")
        val children = childrenJson?.mapNotNull { el ->
            if (el.isJsonObject) parseNode(el.asJsonObject) else null
        }.orEmpty()

        val orientation = propsObj?.getIntOr("orientation", 1) ?: 1
        val layoutType = propsObj?.getIntOr("layoutType", 0) ?: 0
        val drawable = propsObj?.getAsJsonObjectOrNull("drawable")
        val drawableType = drawable?.getIntOr("type", -1) ?: -1
        val hasMeaningfulChildren = children.isNotEmpty()

        val kind = when {
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

        var text: String? = null
        var textSizeSp = 14f
        var textColor = 0xFF000000.toInt()

        if (kind == NodeKind.TEXT && drawable != null) {
            text = drawable.get("data")?.asStringOrNull()
            val dProps = drawable.getAsJsonObjectOrNull("props")
            textSizeSp = dProps?.get("textSize")?.asFloatOrNull() ?: 14f
            textColor = parseColor(dProps?.get("textColor")?.asStringOrNull()) ?: 0xFF000000.toInt()
        }

        val resolvedBackground = when {
            backgroundColor != null -> backgroundColor
            kind == NodeKind.BOX && drawable != null && drawableType != 1 -> 0xFF888888.toInt()
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
        )

        return FNode(kind = kind, props = props, children = children)
    }

    private fun parseDimension(obj: JsonObject?, default: Dimension): Dimension {
        if (obj == null) return default
        val value = obj.getIntOr("value", default.value)
        val unitCode = obj.getIntOr("unit", 1)
        val unit = when (unitCode) {
            2 -> DimUnit.PX
            3 -> DimUnit.PERCENT
            else -> DimUnit.DP
        }
        return when (value) {
            -1 -> Dimension.MATCH
            -2 -> Dimension.WRAP
            else -> Dimension(value, unit)
        }
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
        val bg = props?.getAsJsonObjectOrNull("background") ?: return null
        val colorStr = bg.get("color")?.asStringOrNull()
        return parseColor(colorStr)
    }

    /** Parses #RRGGBB or #AARRGGBB; returns null on failure. */
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
