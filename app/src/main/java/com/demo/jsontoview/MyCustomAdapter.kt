import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.jsontoview.CustomViewGroup2
import com.demo.jsontoview.FView
import com.demo.jsontoview.Parser

class MyCustomAdapter(private val props: Props, private val children: List<FView>) :
    RecyclerView.Adapter<MyCustomAdapter.ViewHolder>() {

    inner class ViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (props.test == "reTest")
            Log.e("MyCustomAdapter", "saocungdcdsdasdas ${children.size} ")

        val context = parent.context

        return when (viewType) {
            ViewTypeConfig.RecyclerView.type -> {
                val recyclerView = RecyclerView(context)
                ViewHolder(recyclerView)
            }

            ViewTypeConfig.ViewGroup.type -> {
                val viewGroup = CustomViewGroup2(context)
                if (props.test == "reTest")
                    Log.e("MyCustomAdapter", "saocungdcnha21212 ${viewGroup}")

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
                if (props.test == "reTest")
                    Log.e("MyCustomAdapter", "saocungdcnha ${view} ${position} $child")

                view.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        Parser.parseDimension(child.props.width),
                        Parser.parseDimension(child.props.height)
                    )
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
                view.adapter = MyCustomAdapter(child.props, child.children)


            }

            else -> {

            }
        }

    }

    override fun getItemCount(): Int {
        return children.size
    }

    override fun getItemViewType(position: Int): Int {
        if (props.test == "reTest")
            Log.e("MyCustomAdapter", "saocungdcdsddavcbasdas ${position} ${children[position].viewType.type} ${children[position].props.test} ")
        return children[position].viewType.type
    }
}