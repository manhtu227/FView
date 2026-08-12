package com.manhtu.jsontoview.model

enum class TreeSource {
    FEED_JSON,
    SYNTHETIC,
}

data class TreeSpec(
    val name: String,
    val root: FNode,
    val source: TreeSource,
)
