package com.manhtu.jsontoview.model

data class NodeProps(
    val id: String? = null,
    val width: Dimension,
    val height: Dimension,
    val padding: EdgeInsets = EdgeInsets.ZERO,
    val margin: EdgeInsets = EdgeInsets.ZERO,
    val gap: Int = 0,
    val backgroundColor: Int? = null,
    val cornerRadius: Float = 0f,
    val text: String? = null,
    val textSizeSp: Float = 14f,
    val textColor: Int = 0xFF000000.toInt(),
    /** Optional image URL; loaded via app-provided [com.manhtu.jsontoview.image.ImageLoader]. */
    val imageUrl: String? = null,
    val contentDescription: String? = null,
    /** Non-null means the node is tappable when a handler is configured. */
    val action: NodeAction? = null,
)
