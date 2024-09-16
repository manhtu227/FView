import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.jsontoview.CustomViewGroup
import com.demo.jsontoview.CustomViewGroup2
import com.demo.jsontoview.FTree

class MyCustomAdapter(private val children: List<FTree>) :
    RecyclerView.Adapter<MyCustomAdapter.ViewHolder>() {

    inner class ViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.e("MyCustomAdapter", "onCreateViewHolder $viewType")
        val context = parent.context

        return when (viewType) {
            ViewTypeConfig.RecyclerView.type -> {
                val recyclerView = RecyclerView(context)
                ViewHolder(recyclerView)
            }
            ViewTypeConfig.ViewGroup.type -> {
                val viewGroup = CustomViewGroup2(context)
                ViewHolder(viewGroup)
            }
            else -> {
                ViewHolder(TextView(context))
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val child = children[position]
        val view = holder.view

        when (view) {
            is CustomViewGroup2 -> {
                view.apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }.setFViewTree(child)
            }
            is RecyclerView -> {
                view.layoutManager = if (child.props.orientation == OrientationConfig.Vertical) {
                    LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
                } else {
                    LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
                }
                view.apply {
                    setPadding(
                        child.props.padding.left,
                        child.props.padding.top,
                        child.props.padding.right,
                        child.props.padding.bottom
                    )
                }
                view.adapter = MyCustomAdapter(child.children)


            }
            else -> {

            }
        }

    }

    override fun getItemCount(): Int {
        return children.size
    }

    override fun getItemViewType(position: Int): Int {
        Log.e("MyCustomAdapter", "getItemViewType ${children.size} $position  ${children[position].viewType}")
        return children[position].viewType.type
    }
}