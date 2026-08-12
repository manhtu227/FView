package com.manhtu.jsontoview.model

data class FNode(
    val kind: NodeKind,
    val props: NodeProps,
    val children: List<FNode> = emptyList(),
)

fun FNode.nodeCount(): Int = 1 + children.sumOf { it.nodeCount() }

fun FNode.maxDepth(): Int =
    if (children.isEmpty()) 1
    else 1 + children.maxOf { it.maxDepth() }
