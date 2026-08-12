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
        findViewById<Button>(R.id.benchmarkButton).setOnClickListener {
            startActivity(Intent(this, BenchmarkActivity::class.java))
        }
    }
}
