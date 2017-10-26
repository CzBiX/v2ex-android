package com.czbix.v2ex.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.view.ViewCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.czbix.v2ex.R
import com.czbix.v2ex.util.MiscUtils
import com.czbix.v2ex.util.ViewUtils

class SearchBoxLayout : FrameLayout, View.OnClickListener, TextWatcher, TextView.OnEditorActionListener {
    private lateinit var mBtnBack: ImageButton
    private lateinit var mBtnClear: ImageButton
    private lateinit var mQuery: EditText
    private lateinit var mBox: RelativeLayout
    private lateinit var mListener: Listener

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val context = context
        View.inflate(context, R.layout.view_search_box, this)

        setBackgroundResource(R.color.transparent_background)

        mBox = findViewById(R.id.box)
        if (!MiscUtils.HAS_L) {
            ViewCompat.setElevation(mBox, ViewUtils.getDimensionPixelSize(R.dimen.appbar_elevation).toFloat())
        }

        setOnClickListener(this)
        mBtnBack = findViewById(R.id.action_back)
        mBtnClear = findViewById(R.id.action_clear)
        mQuery = findViewById(R.id.query)

        mBtnBack.setOnClickListener(this)
        mBtnClear.setOnClickListener(this)

        mQuery.addTextChangedListener(this)
        mQuery.setOnEditorActionListener(this)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun show() {
        if (visibility == View.VISIBLE) {
            return
        }

        mListener.onShow()
        visibility = View.VISIBLE

        if (MiscUtils.HAS_L) {
            val animDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
            val boxAnimator = ViewAnimationUtils.createCircularReveal(mBox,
                    mBox.width, mBox.height / 2, 0f, mBox.width.toFloat())
                    .setDuration(animDuration.toLong())

            boxAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mQuery.requestFocus()
                    ViewUtils.showInputMethod(mQuery)
                }
            })

            boxAnimator.start()
        } else {
            mQuery.requestFocus()
        }
    }

    @SuppressLint("NewApi")
    @JvmOverloads
    fun hide(withAnimation: Boolean = true) {
        if (visibility != View.VISIBLE) {
            return
        }

        mListener.onHide()
        ViewUtils.hideInputMethod(this)

        if (withAnimation && MiscUtils.HAS_L) {
            val animDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
            val boxAnimator = ViewAnimationUtils.createCircularReveal(mBox,
                    mBox.width, mBox.height / 2, mBox.width.toFloat(), 0f)
                    .setDuration(animDuration.toLong())

            boxAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                    mQuery.setText("")
                }
            })

            boxAnimator.start()
        } else {
            visibility = View.GONE
            mQuery.setText("")
        }
    }

    override fun onClick(v: View) {
        if (v === this) {
            hide()
            return
        }

        when (v.id) {
            R.id.action_back -> hide()
            R.id.action_clear -> mQuery.setText("")
        }
    }

    fun setOnActionListener(listener: Listener) {
        mListener = listener
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable) {
        val newText = s.toString()
        mBtnClear.visibility = if (newText.isEmpty()) View.GONE else View.VISIBLE

        mListener.onQueryTextChange(newText)
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId != EditorInfo.IME_ACTION_SEARCH) {
            return false
        }

        val query = v.text.toString()
        return mListener.onQueryTextSubmit(query)
    }

    interface Listener {
        fun onQueryTextChange(newText: String)
        fun onQueryTextSubmit(query: String): Boolean
        fun onShow()
        fun onHide()
    }
}
