package com.demo.jsontoview.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.demo.jsontoview.R
import com.demo.jsontoview.benchmark.BenchmarkActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        findViewById<Button>(R.id.feedButton).setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }
        findViewById<Button>(R.id.sampleHelloButton).setOnClickListener {
            openSample("samples/hello.json")
        }
        findViewById<Button>(R.id.sampleCardButton).setOnClickListener {
            openSample("samples/card.json")
        }
        findViewById<Button>(R.id.sampleFeedButton).setOnClickListener {
            openSample("samples/feed-page.json")
        }
        findViewById<Button>(R.id.aiStudioButton).setOnClickListener {
            startActivity(Intent(this, AiStudioActivity::class.java))
        }
        findViewById<Button>(R.id.benchmarkButton).setOnClickListener {
            startActivity(Intent(this, BenchmarkActivity::class.java))
        }
    }

    private fun openSample(asset: String) {
        startActivity(
            Intent(this, SampleJsonActivity::class.java)
                .putExtra(SampleJsonActivity.EXTRA_ASSET, asset),
        )
    }
}
