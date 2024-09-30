package com.demo.jsontoview

import MyCustomAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ViewGroupType {
    fun viewGroup(context: Context, layout: FView): android.view.View {
        val viewGroup = CustomViewGroup2(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                Parser.parseDimension(layout.props.width),
                Parser.parseDimension(layout.props.height)
            )
            setFViewTree(layout)
        }
        return viewGroup
    }

    fun recyclerView(context: Context, layout: FView): android.view.View {
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

            adapter = MyCustomAdapter(layout.props,layout.children)

        }

        return recyclerView
    }

    fun inputText(context: Context, layout: FView): android.view.View {
        val inputText = EditText(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                Parser.parseDimension(layout.props.width),
                Parser.parseDimension(layout.props.height)
            )
            setPadding(
                layout.props.padding.left,
                layout.props.padding.top,
                layout.props.padding.right,
                layout.props.padding.bottom
            )

            // Tạo background với border
            val drawable = GradientDrawable().apply {
                // Thiết lập màu nền
                if (layout.props.background != null) {
                    setColor(Color.parseColor(layout.props.background.color))
                }
                cornerRadius = 100f
            }
            background = drawable
            hint = layout.props.hintText

        }

        return inputText
    }

    fun getView(context: Context, layout: FView): android.view.View {
        return when (layout.viewType) {
            ViewTypeConfig.ViewGroup -> viewGroup(context, layout)
            ViewTypeConfig.RecyclerView -> recyclerView(context, layout)
            ViewTypeConfig.InputText -> inputText(context, layout)
        }
    }
}