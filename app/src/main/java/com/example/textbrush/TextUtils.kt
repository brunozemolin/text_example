package com.example.textbrush

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlin.math.sqrt


const val textMaxSize: Float = 140f
const val textMinSize: Float = 40f
const val initialTextSize: Float = 20f
private const val PARAM_PLUS_MINUS = 1
private var mBaseDistZoomIn = 0
private var mBaseDistZoomOut = 0

fun buildTextView(text: String, context: Context, params: LayoutParams): TextView {
    return TextView(context).apply {
        setText(text)
        textSize = initialTextSize
        setTextColor(ContextCompat.getColor(context, R.color.black))
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layoutParams = params
    }
}

fun setZoomInOrZoomOut(event: MotionEvent, textView: TextView) {
    val currentSize = textView.textSize
    val currentDistance: Int = getDistanceFromEvent(event)
    if (event.action and ACTION_MASK == ACTION_POINTER_DOWN && isBetweenMinAndMax(currentSize)) {
        mBaseDistZoomIn = getDistanceFromEvent(event)
        mBaseDistZoomOut = getDistanceFromEvent(event)
    } else {
        if (currentDistance > mBaseDistZoomIn) plusTextSize(textView)
        if (currentDistance < mBaseDistZoomOut) minusTextSize(textView)
    }
}

fun hideTrashArea(trashArea: View, trash: ImageView) {
    trash.animate().alpha(0.0f).duration = 100
    trashArea.animate().alpha(0.0f).duration = 100
    trashArea.visibility = View.GONE
    trash.visibility = View.GONE
}

fun showTrashArea(trashArea: View, trash: ImageView) {
    trash.animate().alpha(1.0f).duration = 100
    trashArea.animate().alpha(1.0f).duration = 100
    trashArea.visibility = View.VISIBLE
    trash.visibility = View.VISIBLE
}

fun isNearToBottom(rootLayout: RelativeLayout, yScreenTouch: Int) =
    (rootLayout.height - yScreenTouch) < 30

fun minusTextSize(textView: TextView) {
    var finalSize = textView.textSize - PARAM_PLUS_MINUS
    if (finalSize < textMinSize) {
        finalSize = textMinSize
    }
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize)
}

fun plusTextSize(textView: TextView) {
    var finalSize = textView.textSize + PARAM_PLUS_MINUS
    if (finalSize > textMaxSize) {
        finalSize = textMaxSize
    }
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize)
}

fun isBetweenMinAndMax(currentSize: Float) = currentSize in textMinSize..textMaxSize

private fun getDistanceFromEvent(event: MotionEvent): Int {
    val dx = (event.getX(0) - event.getX(1)).toInt()
    val dy = (event.getY(0) - event.getY(1)).toInt()
    return sqrt((dx * dx + dy * dy).toDouble()).toInt()
}

fun setFont(textView: TextView, context: Context, font: Int){
    val typeface = ResourcesCompat.getFont(context, font)
    textView.typeface = typeface
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}
