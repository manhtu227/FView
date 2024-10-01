package com.demo.jsontoview

//import FView
import MyCustomAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
data class User(val name: String, val age: Int)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val jsonObject = FileUtils.readFile("view.json", this@MainActivity)
                Log.e("createViewFromLayout", "createViewFromLayout 1ao ${Gson().fromJson(jsonObject, FView::class.java)}")
                val rootViewData = Parser.parseJsonToViewData(jsonObject)
                withContext(Dispatchers.Main) {
                    val rootView = createViewFromLayout(this@MainActivity, rootViewData)
                    setContentView(rootView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    companion object {
        fun createViewFromLayout(context: Context, layout: FView): android.view.View {
            return when (layout.viewType) {
                ViewTypeConfig.ViewGroup -> handleViewGroup(context, layout)
                ViewTypeConfig.RecyclerView -> handleRecyclerView(context, layout)
                ViewTypeConfig.InputText -> TODO()
            }
        }


        private fun handleViewGroup(context: Context, layout: FView): android.view.View {
            val viewGroup = CustomViewGroup2(context).apply {
                setFViewTree(layout)
            }
            return viewGroup
        }

        private fun handleRecyclerView(context: Context, layout: FView): android.view.View {
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

                // Add divider item decoration
                val dividerItemDecoration = DividerItemDecoration(
                    context, (layoutManager as LinearLayoutManager).orientation
                )

                // Custom màu và độ dày cho divider
                val drawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#5B5F59")) // Màu xám
                    setSize(1, 3)        // Chiều rộng 1px, chiều cao 5px
                }
                dividerItemDecoration.setDrawable(drawable)

                addItemDecoration(dividerItemDecoration)
            }

            return recyclerView
        }

    }
}