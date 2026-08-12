package com.demo.jsontoview.demo

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demo.jsontoview.R
import com.demo.jsontoview.flat.FlatHostView
import com.demo.jsontoview.parse.JsonTreeParser
import java.util.concurrent.Executors

class FeedActivity : AppCompatActivity() {
    private val io = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        val container = findViewById<FrameLayout>(R.id.feedContainer)
        val host = FlatHostView(this)
        container.addView(
            host,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )

        io.execute {
            try {
                val json = assets.open("view.json").bufferedReader().use { it.readText() }
                val root = JsonTreeParser.parse(json)
                runOnUiThread { host.bind(root) }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Failed to load feed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        io.shutdownNow()
    }
}
