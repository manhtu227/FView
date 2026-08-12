package com.manhtu.jsontoview.action

import com.manhtu.jsontoview.model.FNode
import com.manhtu.jsontoview.model.NodeAction

/** App callback when a node with [NodeAction] is activated (tap). */
fun interface NodeActionHandler {
    fun onAction(node: FNode, action: NodeAction)
}
