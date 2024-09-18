package com.demo.jsontoview.drawable


import Props
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.demo.jsontoview.FTree
import com.demo.jsontoview.models.DrawableComponent
import com.demo.jsontoview.pattern.FiveImageStrategy
import com.demo.jsontoview.pattern.FourImageStrategy
import com.demo.jsontoview.pattern.ImageStrategy
import com.demo.jsontoview.pattern.SimpleImageStrategy
import com.demo.jsontoview.pattern.ThreeImageStrategy
import com.demo.jsontoview.pattern.TwoImageStrategy

class ImageArrayDrawable : DrawableComponent {
    private var imageBitmaps: MutableList<Bitmap?> = mutableListOf()
    private var imageLayout: ImageStrategy? = null

    // Load danh sách các ảnh từ URL
    fun loadImagesFromUrls(
        context: Context,
        fView: FTree,
        urls: List<String>,
        onImagesReady: () -> Unit,
    ) {

        imageBitmaps = fView.imageBitmaps ?: mutableListOf()

        if ( imageBitmaps?.size != urls.size) {
            for (url in urls) {
                Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?,
                        ) {
                            imageBitmaps.add(resource)
                            if (imageBitmaps.size == urls.size) {
                                fView.imageBitmaps = imageBitmaps
                                when (imageBitmaps.size) {
                                    1 -> {
                                        imageLayout =
                                            imageBitmaps[0]?.let { SimpleImageStrategy(it) }
                                    }

                                    2 -> {
                                        imageLayout = TwoImageStrategy(imageBitmaps)
                                    }

                                    3 -> {
                                        imageLayout = ThreeImageStrategy(imageBitmaps)
                                    }

                                    4 -> {
                                        imageLayout = FourImageStrategy(imageBitmaps)
                                    }

                                    5 -> {
                                        imageLayout = FiveImageStrategy(imageBitmaps)
                                    }
                                }

                                onImagesReady()  // Gọi khi tất cả ảnh đã load
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        } else {
            if (imageBitmaps.size == urls.size) {
                when (imageBitmaps.size) {
                    1 -> {
                        imageLayout = imageBitmaps[0]?.let { SimpleImageStrategy(it) }
                    }

                    2 -> {
                        imageLayout = TwoImageStrategy(imageBitmaps)
                    }

                    3 -> {
                        imageLayout = ThreeImageStrategy(imageBitmaps)
                    }

                    4 -> {
                        imageLayout = FourImageStrategy(imageBitmaps)
                    }

                    5 -> {
                        imageLayout = FiveImageStrategy(imageBitmaps)
                    }
                }

                onImagesReady()  // Gọi khi tất cả ảnh đã load
            }
        }
    }

    override fun draw(canvas: Canvas, width: Int, height: Int, props: Props) {
        imageLayout?.draw(canvas, width, height, props)
    }

    override fun layout(left: Int, top: Int, fView: FTree) {
//        TODO("Not yet implemented")
    }

    override fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        fView: FTree,
        props: Props,
    ): Pair<Int, Int> {
        return imageLayout?.measure(widthMeasureSpec, heightMeasureSpec, fView, props) ?: Pair(0, 0)
    }
}