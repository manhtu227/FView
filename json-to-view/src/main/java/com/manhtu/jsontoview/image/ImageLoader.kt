package com.manhtu.jsontoview.image

import android.graphics.drawable.Drawable

/**
 * App-provided image loading. The library never hard-depends on Glide/Coil.
 * Network work should happen off the main thread; [ImageTarget] callbacks may
 * be invoked on the main thread.
 */
interface ImageLoader {
    fun load(url: String, target: ImageTarget)

    fun cancel(target: ImageTarget) {
        // default no-op
    }
}

interface ImageTarget {
    fun onSuccess(drawable: Drawable)
    fun onError()
}
