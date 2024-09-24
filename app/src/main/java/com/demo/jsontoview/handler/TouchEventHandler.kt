package com.demo.jsontoview.helpers

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.MotionEvent
import androidx.lifecycle.Lifecycle
import com.demo.jsontoview.FView

class TouchEventHandler(private val event: MotionEvent, private val fView: FView) {

    fun onTouchEvent(): FView? {
        val x = event.x
        val y = event.y
        val props = fView.props
        if (props.isClick == true && checkClick(x, y)) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateBackgroundColor(
                        Color.parseColor(props.background?.color ?: "#FFFFFF"),
                        Color.LTGRAY
                    )
                }

                MotionEvent.ACTION_UP -> {
                    animateBackgroundColor(
                        Color.LTGRAY,
                        Color.parseColor(props.background?.color ?: "#FFFFFF")
                    )
                    return fView
                }

                else -> {
                    animateBackgroundColor(
                        Color.LTGRAY,
                        Color.parseColor(props.background?.color ?: "#FFFFFF")
                    )
                }
            }
        }
        return null
    }

    private fun animateBackgroundColor(startColor: Int, endColor: Int) {
        fView.colorAnimator?.cancel() // Hủy bỏ animation trước đó nếu có
        fView.colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor).apply {
            duration = 300 // Thời gian animation, có thể điều chỉnh
            addUpdateListener { animator ->
                fView.backgroundColor =
                    String.format("#%06X", 0xFFFFFF and (animator.animatedValue as Int))
                fView.customViewGroup?.invalidate() // Vẽ lại view để cập nhật màu nền
            }
            start()
        }
    }


    private fun checkClick(x: Float, y: Float): Boolean {
        val leftView = fView.leftView
        val topView = fView.topView
        val measureWidth = fView.measureWidth
        val measureHeight = fView.measureHeight
        if (x >= leftView && x <= leftView + measureWidth && y >= topView && y <= topView + measureHeight) {
            return true
        }
        return false
    }
}