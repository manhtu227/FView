package com.manhtu.jsontoview.util

import android.view.View
import android.view.ViewGroup

data class HierarchySnapshot(
    val viewCount: Int,
    val maxDepth: Int,
)

object HierarchyStats {
    /** Snapshot Android view hierarchy. Root depth = 1; viewCount includes root. */
    fun snapshot(root: View): HierarchySnapshot {
        var count = 0
        var maxDepth = 1
        fun walk(view: View, depth: Int) {
            count++
            if (depth > maxDepth) maxDepth = depth
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    walk(view.getChildAt(i), depth + 1)
                }
            }
        }
        walk(root, 1)
        return HierarchySnapshot(viewCount = count, maxDepth = maxDepth)
    }
}
