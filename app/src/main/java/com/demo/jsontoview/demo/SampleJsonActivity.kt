package com.demo.jsontoview.demo

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demo.jsontoview.R
import com.manhtu.jsontoview.JsonToViewHost
import com.manhtu.jsontoview.RenderConfig

/**
 * Loads a stable-schema JSON sample from assets and renders with Flat + Glide.
 *
 * Intent extra [EXTRA_ASSET]: path under assets, e.g. `samples/hello.json`
 */
class SampleJsonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        val container = findViewById<FrameLayout>(R.id.feedContainer)
        val asset = intent.getStringExtra(EXTRA_ASSET) ?: "samples/hello.json"
        title = asset.substringAfterLast('/')

        val host = JsonToViewHost(this).apply {
            mode = JsonToViewHost.Mode.FLAT
            renderConfig = RenderConfig(
                imageLoader = GlideImageLoader(this@SampleJsonActivity),
                actionHandler = { node, action ->
                    Toast.makeText(
                        this@SampleJsonActivity,
                        "action=${action.type} payload=${action.payload} id=${node.props.id}",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
        container.addView(
            host,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )

        try {
            val json = assets.open(asset).bufferedReader().use { it.readText() }
            host.bindJson(json)
        } catch (e: Exception) {
            Toast.makeText(this, "Load failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_ASSET = "asset"
    }
}
