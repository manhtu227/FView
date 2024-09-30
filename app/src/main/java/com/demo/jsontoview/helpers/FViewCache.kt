package com.demo.jsontoview.helpers

class FViewCache {
    private val cacheData: MutableMap<String, Any> = mutableMapOf()

    fun setCache(key: String, value: Any) {
        cacheData[key] = value
    }

    fun getCache(key: String): Any? {
        return cacheData[key]
    }

    fun clearCache(key: String) {
        cacheData.remove(key)
    }

    fun clearAll() {
        cacheData.clear()
    }
}
