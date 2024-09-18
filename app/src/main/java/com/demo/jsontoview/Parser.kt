package com.demo.jsontoview

import DimensionConfig
import GravityConfig
import android.content.res.Resources
import android.view.Gravity
import android.view.ViewGroup
import com.google.gson.Gson

object Parser {
    fun parseJsonToViewData(json: String): FTree {
        val gson = Gson()
        return gson.fromJson(json, FTree::class.java)
    }

    fun parseDimension(dimension: DimensionConfig): Int {
        return when (dimension.value) {
            -1 -> ViewGroup.LayoutParams.MATCH_PARENT
            -2 -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> when (dimension.unit) {
                UnitConfig.Px -> dimension.value
                UnitConfig.Dp -> (dimension.value * Resources.getSystem().displayMetrics.density).toInt()
                UnitConfig.Percent -> if (dimension.value > 100) 100 else dimension.value
            }
        }
    }

    fun parseGravityForView(gravitySet: Set<GravityConfig>?): Int? {
        return gravitySet?.fold(0) { acc, gravity ->
            acc or when (gravity) {
                GravityConfig.Left -> Gravity.START
                GravityConfig.Top -> Gravity.TOP
                GravityConfig.Right -> Gravity.END
                GravityConfig.Bottom -> Gravity.BOTTOM
                GravityConfig.Center -> Gravity.CENTER
            }
        }
    }

}