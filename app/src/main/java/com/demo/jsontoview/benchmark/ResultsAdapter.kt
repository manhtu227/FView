package com.demo.jsontoview.benchmark

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.jsontoview.R

class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.Holder>() {
    private val items = mutableListOf<BenchmarkReport>()

    fun submit(reports: List<BenchmarkReport>) {
        items.clear()
        items.addAll(reports)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_benchmark_row, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val r = items[position]
        holder.title.text = "${r.mode} · ${r.treeName} · nodes=${r.treeNodeCount} depth=${r.maxTreeDepth}"
        holder.body.text = buildString {
            append("measure=${"%.2f".format(r.measureMs)} layout=${"%.2f".format(r.layoutMs)} ")
            append("draw=${"%.2f".format(r.drawMs)} first=${"%.2f".format(r.firstFrameMs)}\n")
            append("views=${r.viewCount} viewDepth=${r.maxViewDepth} heap=${"%.1f".format(r.javaHeapMb)}MB")
            r.scrollAvgFrameMs?.let {
                append(" scrollAvg=${"%.2f".format(it)} dropped=${r.droppedFrames}")
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.rowTitle)
        val body: TextView = view.findViewById(R.id.rowBody)
    }
}
