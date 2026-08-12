package com.demo.jsontoview.benchmark

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.jsontoview.R
import com.manhtu.jsontoview.benchmark.BenchmarkReport
import com.manhtu.jsontoview.benchmark.BenchmarkRunner
import com.manhtu.jsontoview.benchmark.RenderMode
import com.manhtu.jsontoview.benchmark.deltaPct
import com.manhtu.jsontoview.model.SyntheticTreeFactory
import com.manhtu.jsontoview.model.TreeSpec
import com.manhtu.jsontoview.model.nodeCount
import com.manhtu.jsontoview.parse.JsonTreeParser
import java.util.concurrent.Executors

/** Sample UI — uses the json-to-view SDK benchmark APIs. */
class BenchmarkActivity : AppCompatActivity() {

    private lateinit var modeSpinner: Spinner
    private lateinit var sourceSpinner: Spinner
    private lateinit var runButton: Button
    private lateinit var clearButton: Button
    private lateinit var statusText: TextView
    private lateinit var deltaSummary: TextView
    private lateinit var benchContainer: FrameLayout
    private lateinit var resultsList: RecyclerView
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var runner: BenchmarkRunner

    private val io = Executors.newSingleThreadExecutor()
    private val reports = mutableListOf<BenchmarkReport>()
    private var feedSpec: TreeSpec? = null
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benchmark)

        modeSpinner = findViewById(R.id.modeSpinner)
        sourceSpinner = findViewById(R.id.sourceSpinner)
        runButton = findViewById(R.id.runButton)
        clearButton = findViewById(R.id.clearButton)
        statusText = findViewById(R.id.statusText)
        deltaSummary = findViewById(R.id.deltaSummary)
        benchContainer = findViewById(R.id.benchContainer)
        resultsList = findViewById(R.id.resultsList)

        modeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Flat", "Nested", "Both"),
        )
        sourceSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Feed JSON", "Shallow", "Deep", "Wide", "FeedLike"),
        )

        resultsAdapter = ResultsAdapter()
        resultsList.layoutManager = LinearLayoutManager(this)
        resultsList.adapter = resultsAdapter

        runner = BenchmarkRunner(benchContainer)

        runButton.setOnClickListener { if (!running) startRun() }
        clearButton.setOnClickListener {
            reports.clear()
            resultsAdapter.clear()
            deltaSummary.text = ""
            benchContainer.removeAllViews()
            statusText.text = "Cleared"
        }

        preloadFeed()
    }

    private fun preloadFeed() {
        statusText.text = "Loading feed…"
        io.execute {
            try {
                val json = assets.open("view.json").bufferedReader().use { it.readText() }
                val spec = JsonTreeParser.parseTreeSpec(json, name = "feed-json")
                runOnUiThread {
                    feedSpec = spec
                    statusText.text = "Ready (feed nodes=${spec.root.nodeCount()})"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "Feed load failed: ${e.message}"
                }
            }
        }
    }

    private fun startRun() {
        val spec = resolveSpec() ?: run {
            statusText.text = "Source not ready"
            return
        }
        val modeChoice = modeSpinner.selectedItemPosition
        running = true
        runButton.isEnabled = false
        statusText.text = "Running ${spec.name}…"
        deltaSummary.text = ""

        when (modeChoice) {
            0 -> runOne(spec, RenderMode.FLAT) { finishRun() }
            1 -> runOne(spec, RenderMode.NESTED) { finishRun() }
            else -> runOne(spec, RenderMode.FLAT) {
                runOne(spec, RenderMode.NESTED) {
                    showDelta()
                    finishRun()
                }
            }
        }
    }

    private fun runOne(spec: TreeSpec, mode: RenderMode, next: () -> Unit) {
        statusText.text = "Running ${mode.name} · ${spec.name}"
        benchContainer.post {
            runner.run(spec, mode, includeScroll = true) { report ->
                reports.add(report)
                resultsAdapter.submit(reports.toList())
                next()
            }
        }
    }

    private fun showDelta() {
        val flat = reports.lastOrNull { it.mode == RenderMode.FLAT }
        val nested = reports.lastOrNull { it.mode == RenderMode.NESTED }
        if (flat == null || nested == null) return
        deltaSummary.text = buildString {
            append("Δ measure=${"%.1f".format(deltaPct(nested.measureMs, flat.measureMs))}% ")
            append("layout=${"%.1f".format(deltaPct(nested.layoutMs, flat.layoutMs))}% ")
            append("views: flat=${flat.viewCount} nested=${nested.viewCount}")
        }
    }

    private fun finishRun() {
        running = false
        runButton.isEnabled = true
        statusText.text = "Done (${reports.size} reports)"
    }

    private fun resolveSpec(): TreeSpec? {
        return when (sourceSpinner.selectedItemPosition) {
            0 -> feedSpec
            1 -> SyntheticTreeFactory.shallow()
            2 -> SyntheticTreeFactory.deep()
            3 -> SyntheticTreeFactory.wide()
            4 -> SyntheticTreeFactory.feedLike()
            else -> null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        io.shutdownNow()
    }
}
