package utils

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class DoubleTapListener(context: Context, private val onDoubleTap: () -> Unit) : View.OnTouchListener {
    private val gestureDetector: GestureDetector

    init {
        val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTap()
                return true
            }
        }
        gestureDetector = GestureDetector(context, gestureListener)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.d("TAG", "onTouch triggered")
        return gestureDetector.onTouchEvent(event!!) || v?.onTouchEvent(event) ?: false
    }

}

