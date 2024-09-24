package com.demo.jsontoview.helpers

import android.widget.EditText
import com.demo.jsontoview.CustomViewGroup2
import com.demo.jsontoview.FView
import com.demo.jsontoview.UI.FViewComment
import com.demo.jsontoview.drawable.ButtonDrawable

interface ViewEventAction {
    fun onClick()
}

class ViewEventManager(private val viewGroup: CustomViewGroup2, fView: FView) {
    private var eventHandler: ViewEventAction? = null

    init {
        if (fView.props.id == "buttonComment") {
            eventHandler = CommentEventAction(viewGroup)
        } else if (fView.props.id == "buttonLike") {
            eventHandler = LikeEventAction(viewGroup, fView)
        }
    }

    fun handleClick() {
        eventHandler?.onClick()
    }
}

class LikeEventAction(private val viewGroup: CustomViewGroup2,private val fView: FView) : ViewEventAction {
    override fun onClick() {
        (fView.getDrawableComponent() as ButtonDrawable).setColor("#0068FF")
        viewGroup.invalidate()
    }
}

class CommentEventAction(private val viewGroup: CustomViewGroup2) : ViewEventAction {
    override fun onClick() {
        val editText = viewGroup.pendingViews.get("inputComment") as EditText
        val newFView =
            FViewComment(viewGroup.context, viewGroup).createCommentView(editText.text.toString())
        val fViewList = viewGroup.getRootFView()!!.children
        val position = fViewList.size - 1
        fViewList.add(position, newFView)
        viewGroup.requestLayout()
    }
}