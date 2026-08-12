package com.manhtu.jsontoview.model

/**
 * Builds deterministic synthetic [TreeSpec]s for flat-vs-nested benchmarks.
 *
 * Recursive rule: depth remaining 1 → TEXT leaf; otherwise COLUMN/ROW alternating
 * with [branching] children. Background colors cycle a fixed ARGB palette.
 */
object SyntheticTreeFactory {

    private val PALETTE = intArrayOf(
        0xFFE57373.toInt(),
        0xFF64B5F6.toInt(),
        0xFF81C784.toInt(),
        0xFFFFB74D.toInt(),
        0xFFBA68C8.toInt(),
        0xFF4DB6AC.toInt(),
        0xFFFF8A65.toInt(),
        0xFF90A4AE.toInt(),
    )

    fun shallow(): TreeSpec =
        TreeSpec(
            name = "synthetic-shallow",
            root = build(depthRemaining = 3, branching = 7),
            source = TreeSource.SYNTHETIC,
        )

    fun deep(): TreeSpec =
        TreeSpec(
            name = "synthetic-deep",
            root = build(depthRemaining = 20, branching = 1),
            source = TreeSource.SYNTHETIC,
        )

    fun wide(): TreeSpec =
        TreeSpec(
            name = "synthetic-wide",
            root = build(depthRemaining = 2, branching = 199),
            source = TreeSource.SYNTHETIC,
        )

    fun feedLike(cards: Int = 15): TreeSpec {
        val cardRoots = List(cards) { index ->
            build(depthRemaining = 6, branching = 2, colorIndex = index)
        }
        val root = FNode(
            kind = NodeKind.COLUMN,
            props = NodeProps(
                width = Dimension.MATCH,
                height = Dimension.WRAP,
                backgroundColor = colorAt(0),
                gap = 8,
            ),
            children = cardRoots,
        )
        return TreeSpec(
            name = "synthetic-feedLike",
            root = root,
            source = TreeSource.SYNTHETIC,
        )
    }

    fun custom(depth: Int, branching: Int): TreeSpec =
        TreeSpec(
            name = "synthetic-custom",
            root = build(depthRemaining = depth, branching = branching),
            source = TreeSource.SYNTHETIC,
        )

    private fun build(
        depthRemaining: Int,
        branching: Int,
        level: Int = 0,
        colorIndex: Int = 0,
    ): FNode {
        val color = colorAt(colorIndex + level)
        if (depthRemaining <= 1) {
            return FNode(
                kind = NodeKind.TEXT,
                props = NodeProps(
                    width = Dimension.WRAP,
                    height = Dimension.WRAP,
                    backgroundColor = color,
                    text = "n$colorIndex-$level",
                    textColor = 0xFF212121.toInt(),
                ),
            )
        }
        val kind = if (level % 2 == 0) NodeKind.COLUMN else NodeKind.ROW
        val children = List(branching) { childIndex ->
            build(
                depthRemaining = depthRemaining - 1,
                branching = branching,
                level = level + 1,
                colorIndex = colorIndex + childIndex + 1,
            )
        }
        return FNode(
            kind = kind,
            props = NodeProps(
                width = Dimension.MATCH,
                height = if (level == 0) Dimension.MATCH else Dimension.WRAP,
                backgroundColor = color,
                gap = 4,
            ),
            children = children,
        )
    }

    private fun colorAt(index: Int): Int = PALETTE[Math.floorMod(index, PALETTE.size)]
}
