package com.demo.jsontoview.demo

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demo.jsontoview.BuildConfig
import com.demo.jsontoview.R
import com.demo.jsontoview.ai.LayoutAiClient
import com.manhtu.jsontoview.JsonToViewHost
import com.manhtu.jsontoview.RenderConfig
import com.manhtu.jsontoview.parse.JsonTreeParser
import java.util.concurrent.Executors

/**
 * Sample-only: natural language → AI API → stable layout JSON → JsonToViewHost preview.
 * API key from debug BuildConfig (local.properties); never part of the published SDK.
 */
class AiStudioActivity : AppCompatActivity() {

    private val io = Executors.newSingleThreadExecutor()
    private lateinit var host: JsonToViewHost
    private lateinit var promptInput: EditText
    private lateinit var generateButton: Button
    private lateinit var modeSpinner: Spinner
    private lateinit var statusText: TextView
    private lateinit var jsonPreview: TextView

    private var lastJson: String? = null
    private var systemPrompt: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_studio)
        title = getString(R.string.ai_studio_title)

        promptInput = findViewById(R.id.promptInput)
        generateButton = findViewById(R.id.generateButton)
        modeSpinner = findViewById(R.id.modeSpinner)
        statusText = findViewById(R.id.statusText)
        jsonPreview = findViewById(R.id.jsonPreview)
        val previewContainer = findViewById<FrameLayout>(R.id.previewContainer)

        modeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Flat", "Nested"),
        )

        host = JsonToViewHost(this).apply {
            mode = JsonToViewHost.Mode.FLAT
            renderConfig = RenderConfig(
                imageLoader = GlideImageLoader(this@AiStudioActivity),
                actionHandler = { node, action ->
                    Toast.makeText(
                        this@AiStudioActivity,
                        "${action.type}: ${action.payload} (id=${node.props.id})",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
        previewContainer.addView(
            host,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )

        systemPrompt = assets.open("ai/system_prompt.txt").bufferedReader().use { it.readText() }

        if (BuildConfig.AI_API_KEY.isBlank()) {
            statusText.text = getString(R.string.ai_studio_missing_key)
        } else {
            statusText.text = getString(R.string.ai_studio_ready)
        }

        promptInput.setText(getString(R.string.ai_studio_default_prompt))

        generateButton.setOnClickListener { generate() }

        modeSpinner.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long,
                ) {
                    host.mode = if (position == 0) {
                        JsonToViewHost.Mode.FLAT
                    } else {
                        JsonToViewHost.Mode.NESTED
                    }
                    lastJson?.let { bindJson(it) }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            },
        )
    }

    private fun generate() {
        val key = BuildConfig.AI_API_KEY
        if (key.isBlank()) {
            statusText.text = getString(R.string.ai_studio_missing_key)
            return
        }
        val prompt = promptInput.text?.toString().orEmpty().trim()
        if (prompt.isEmpty()) {
            Toast.makeText(this, R.string.ai_studio_empty_prompt, Toast.LENGTH_SHORT).show()
            return
        }

        generateButton.isEnabled = false
        statusText.text = getString(R.string.ai_studio_generating)

        io.execute {
            try {
                val client = LayoutAiClient(
                    apiKey = key,
                    baseUrl = BuildConfig.AI_BASE_URL,
                    model = BuildConfig.AI_MODEL,
                    systemPrompt = systemPrompt,
                )
                var json = client.generateLayoutJson(prompt)
                // Validate; one repair attempt via re-extract only (parse must succeed)
                try {
                    JsonTreeParser.parse(json)
                } catch (first: Exception) {
                    // Retry once with stricter user message
                    json = client.generateLayoutJson(
                        "$prompt\n\nPrevious output was invalid. Return ONLY valid JSON object.",
                    )
                    JsonTreeParser.parse(json)
                }
                runOnUiThread {
                    lastJson = json
                    jsonPreview.text = json
                    bindJson(json)
                    statusText.text = getString(R.string.ai_studio_success)
                    generateButton.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = getString(R.string.ai_studio_error, e.message ?: "unknown")
                    generateButton.isEnabled = true
                }
            }
        }
    }

    private fun bindJson(json: String) {
        try {
            JsonTreeParser.parse(json) // validate again on UI path
            host.bindJson(json)
        } catch (e: Exception) {
            statusText.text = getString(R.string.ai_studio_error, e.message ?: "parse")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        io.shutdownNow()
    }
}
