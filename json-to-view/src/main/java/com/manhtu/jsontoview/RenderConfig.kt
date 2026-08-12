package com.manhtu.jsontoview

import com.manhtu.jsontoview.action.NodeActionHandler
import com.manhtu.jsontoview.image.ImageLoader

/**
 * Runtime wiring for renderers. Default config is offline-safe (no loader →
 * image placeholders; no handler → taps ignored). Prefer default for benchmarks.
 */
data class RenderConfig(
    val imageLoader: ImageLoader? = null,
    val actionHandler: NodeActionHandler? = null,
    /** Used when [imageUrl] is set but loader is null or load fails. */
    val imagePlaceholderColor: Int = 0xFF888888.toInt(),
)
