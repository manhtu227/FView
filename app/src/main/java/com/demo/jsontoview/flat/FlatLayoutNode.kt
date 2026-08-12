package com.demo.jsontoview.flat

import com.demo.jsontoview.model.FNode

/** Mutable layout result for a virtual [FNode] after measure/layout. */
class FlatLayoutNode(
    val node: FNode,
    var x: Int = 0,
    var y: Int = 0,
    var w: Int = 0,
    var h: Int = 0,
    val children: MutableList<FlatLayoutNode> = mutableListOf(),
)
