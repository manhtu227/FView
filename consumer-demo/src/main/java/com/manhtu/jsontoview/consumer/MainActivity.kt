package com.manhtu.jsontoview.consumer

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.manhtu.jsontoview.JsonToViewHost
import com.manhtu.jsontoview.RenderConfig
import com.manhtu.jsontoview.image.ImageLoader
import com.manhtu.jsontoview.image.ImageTarget

/**
 * Minimal second application that consumes `:json-to-view` as a library.
 * Proves an external-style binary can render backend JSON without SDK sources.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val host = JsonToViewHost(this).apply {
            mode = JsonToViewHost.Mode.FLAT
            renderConfig = RenderConfig(
                // Offline placeholder loader (no Glide) — still exercises ImageLoader API.
                imageLoader = object : ImageLoader {
                    override fun load(url: String, target: ImageTarget) {
                        target.onSuccess(ColorDrawable(0xFF90CAF9.toInt()))
                    }
                },
                actionHandler = { _, action ->
                    Toast.makeText(
                        this@MainActivity,
                        "${action.type}: ${action.payload}",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
        setContentView(
            host,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        val json = assets.open("feed-page.json").bufferedReader().use { it.readText() }
        host.bindJson(json)
    }
}
