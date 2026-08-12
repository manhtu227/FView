package com.demo.jsontoview.demo

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.manhtu.jsontoview.image.ImageLoader
import com.manhtu.jsontoview.image.ImageTarget

/** Sample-only ImageLoader using Glide (not part of the SDK). */
class GlideImageLoader(private val context: Context) : ImageLoader {
    private val targets = mutableMapOf<ImageTarget, CustomTarget<Drawable>>()

    override fun load(url: String, target: ImageTarget) {
        val glideTarget = object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                target.onSuccess(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onLoadFailed(errorDrawable: Drawable?) {
                target.onError()
            }
        }
        targets[target] = glideTarget
        Glide.with(context.applicationContext).load(url).into(glideTarget)
    }

    override fun cancel(target: ImageTarget) {
        targets.remove(target)?.let { Glide.with(context.applicationContext).clear(it) }
    }
}
