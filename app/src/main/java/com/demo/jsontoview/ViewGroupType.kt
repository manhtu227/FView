package com.demo.jsontoview

import MyCustomAdapter
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ViewGroupType {
    fun viewGroup(context: Context, layout: FTree): android.view.View {
        val viewGroup = CustomViewGroup2(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                Parser.parseDimension(layout.props.width),
                Parser.parseDimension(layout.props.height)
            )
            setFViewTree(layout)
        }
        return viewGroup
    }

    fun recyclerView(context: Context, layout: FTree): android.view.View {
        val recyclerView = RecyclerView(context).apply {
            layoutManager = if (layout.props.orientation == OrientationConfig.Vertical) {
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            } else {
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            layoutParams = ViewGroup.LayoutParams(
                Parser.parseDimension(layout.props.width),
                Parser.parseDimension(layout.props.height)
            ).apply {
                setPadding(
                    layout.props.padding.left,
                    layout.props.padding.top,
                    layout.props.padding.right,
                    layout.props.padding.bottom
                )
            }
            if (layout.props.background != null)
                setBackgroundColor(Color.parseColor(layout.props.background.color))

            adapter = MyCustomAdapter(layout.children)

        }

        return recyclerView
    }
}