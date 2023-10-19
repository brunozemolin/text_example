package com.example.textbrush


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.textbrush.databinding.ActivityMainBinding
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var lastTextViewMoved: TextView? = null
    private var mXDelta = 0
    private var mYDelta = 0
    private var mRootWidth = 0
    private var mRootHeight = 0
    private var nextFont = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListenerMain()
        setListenerTextSize()
        setListenerFontStyle()
        setListenerTextRotate()
    }

    private fun setListenerMain() {
        with(binding) {
            rootLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    mRootWidth = rootLayout.width
                    mRootHeight = rootLayout.height
                }
            })

            rootLayout.setOnClickListener {
                if (edtTextInsert.hasFocus()) {
                    edtTextInsert.visibility = View.GONE
                    if (edtTextInsert.text.toString().isNotEmpty()) {
                        buildComponent(edtTextInsert.text.toString())
                        edtTextInsert.setText("")
                        rootLayout.requestFocus()
                        hideKeyboard(edtTextInsert)
                    }
                } else {
                    edtTextInsert.visibility = View.VISIBLE
                    edtTextInsert.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

                }
            }

            edtTextInsert.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    buildComponent(edtTextInsert.text.toString())
                    edtTextInsert.setText("")
                    hideKeyboard(edtTextInsert)
                }
                false
            }
        }
    }

    private fun setListenerFontStyle() {
        with(binding) {
            changeTextStyle.setOnClickListener {
                when (nextFont) {
                    INDEX_ONE -> lastTextViewMoved?.let {
                        setFont(it, this@MainActivity, R.font.font_a)
                        nextFont = 2
                    }

                    INDEX_TWO -> lastTextViewMoved?.let {
                        setFont(it, this@MainActivity, R.font.font_b)
                        nextFont = 3
                    }

                    INDEX_THREE -> lastTextViewMoved?.let {
                        setFont(it, this@MainActivity, R.font.font_c)
                        nextFont = 0
                    }

                    else -> lastTextViewMoved?.let {
                        setFont(it, this@MainActivity, R.font.font_d)
                        nextFont = 1
                    }
                }
            }
        }
    }

    private fun setListenerTextRotate() {
        with(binding) {
            containerRotatePlus.setOnClickListener {
                lastTextViewMoved?.let { it.rotation = it.rotation + 5 }
            }

            containerRotateMinus.setOnClickListener {
                lastTextViewMoved?.let {
                    it.rotation = it.rotation - 5
                }
            }
        }
    }

    private fun setListenerTextSize() {
        with(binding) {
            containerLetterPlus.setOnClickListener {
                lastTextViewMoved?.let { plusTextSize(it) }
            }

            containerLetterMinus.setOnClickListener {
                lastTextViewMoved?.let { minusTextSize(it) }
            }
        }
    }

    private fun buildComponent(text: String) {
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        val textView = buildTextView(text, this, params)
        lastTextViewMoved = textView
        setListenerTextView(textView, params)
        binding.rootLayout.addView(textView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListenerTextView(textView: TextView, params: LayoutParams) {
        textView.setOnTouchListener { view, event ->
            if (hasTwoPointers(event)) {
                setZoomInOrZoomOut(event, textView)
            } else {
                lastTextViewMoved = view as TextView
                val xScreenTouch = event.rawX.toInt()
                val yScreenTouch = event.rawY.toInt()
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        hideKeyboard(textView)
                        val location = IntArray(2)
                        textView.getLocationOnScreen(location)
                        params.removeRule(RelativeLayout.CENTER_IN_PARENT)

                        val diff = (location[1] - view.y).toInt()
                        mXDelta = xScreenTouch - location[0]
                        mYDelta = yScreenTouch - (location[1] - diff)

                        params.leftMargin =
                            max(0, min(mRootWidth - view.width, xScreenTouch - mXDelta))
                        params.topMargin =
                            max(0, min(mRootHeight - view.height, yScreenTouch - mYDelta))
                        textView.layoutParams = params

                    }

                    MotionEvent.ACTION_MOVE -> {
                        val layoutParams = view.layoutParams as LayoutParams
                        layoutParams.leftMargin =
                            max(0, min(mRootWidth - view.width, xScreenTouch - mXDelta))
                        layoutParams.topMargin =
                            max(0, min(mRootHeight - view.height, yScreenTouch - mYDelta))
                        view.layoutParams = layoutParams

                        if (isNearToBottom(binding.rootLayout, yScreenTouch)) {
                            showTrashArea(binding.vTrashArea, binding.trash)
                        } else {
                            hideTrashArea(binding.vTrashArea, binding.trash)
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (isNearToBottom(binding.rootLayout, yScreenTouch)) {
                            binding.rootLayout.removeView(view)
                            hideTrashArea(binding.vTrashArea, binding.trash)
                        }
                    }
                }
            }
            true
        }
    }

    private fun hasTwoPointers(event: MotionEvent) = event.pointerCount == 2

    companion object {
        const val INDEX_ONE = 1
        const val INDEX_TWO = 2
        const val INDEX_THREE = 3
    }
}
