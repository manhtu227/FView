package com.manhtu.jsontoview.util

import android.content.Context
import com.manhtu.jsontoview.model.DimUnit
import com.manhtu.jsontoview.model.Dimension
import kotlin.math.roundToInt

object Dimens {
    fun dp(context: Context, v: Int): Int {
        return (v * context.resources.displayMetrics.density).roundToInt()
    }

    /**
     * Resolve a [Dimension] against [parentSize] and [density].
     *
     * - value -1 (MATCH) → parentSize
     * - value -2 (WRAP) is a sentinel; callers that need LayoutParams should map to
     *   ViewGroup.LayoutParams.WRAP_CONTENT at those call sites. This method returns -2 as-is.
     * - fixed sizes: DP * density, PX as-is, PERCENT parentSize * value / 100
     */
    fun resolve(dim: Dimension, parentSize: Int, density: Float): Int {
        return when (dim.value) {
            -1 -> parentSize
            -2 -> dim.value
            else -> when (dim.unit) {
                DimUnit.DP -> (dim.value * density).roundToInt()
                DimUnit.PX -> dim.value
                DimUnit.PERCENT -> parentSize * dim.value / 100
            }
        }
    }
}
