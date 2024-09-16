package com.demo.jsontoview

import android.content.Context

object FileUtils {

    fun readFile(fileName: String, context: Context): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

}
