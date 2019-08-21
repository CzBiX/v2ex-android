package com.czbix.v2ex.util

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.R
import com.czbix.v2ex.parser.AsyncImageGetter
import com.czbix.v2ex.ui.util.Html

object ViewUtils {
    @JvmField
    val density: Float
    val tagRegex = Regex("<(/\\w{1,6}>|img)")
    val entityRegex = Regex("&(\\w{1,10}|#\\d{1,4});")

    private val spannableFactory = object : Spannable.Factory() {
        override fun newSpannable(source: CharSequence): Spannable {
            return source as? Spannable ?: super.newSpannable(source)

        }
    }

    init {
        val context = AppCtx.instance
        val displayMetrics = context.resources.displayMetrics
        density = displayMetrics.density
    }

    @JvmStatic
    fun dp2Pixel(dp: Float): Float {
        return density * dp
    }

    fun getDimensionPixelSize(@DimenRes id: Int): Int {
        return AppCtx.instance.resources.getDimensionPixelSize(id)
    }

    fun getExactlyWidth(view: View, maxWidth: Int): Int {
        var width = view.width
        if (width <= 0) {
            view.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.UNSPECIFIED)
            width = view.measuredWidth

            if (width <= 0) {
                width = maxWidth
            }
        }

        LogUtils.v(ViewUtils::class.java, "get exactly width, result: %d, view: %s", width, view)
        return width
    }

    @JvmStatic
    fun setHtmlIntoTextView(view: TextView, html: String, maxWidthPixels: Int, isTopic: Boolean) {
        val content = parseHtml(view, html, isTopic, maxWidthPixels)
        view.setText(content, TextView.BufferType.SPANNABLE)
    }

    fun parseHtml(view: TextView, html: String, isTopic: Boolean, maxWidthPixels: Int): CharSequence {
        return parseHtml(html, AsyncImageGetter(view, maxWidthPixels), isTopic)
    }

    fun parseHtml(html: String, imageGetter: Html.ImageGetter?, isTopic: Boolean): CharSequence {
        return if (!isTopic && !tagRegex.containsMatchIn(html) && !entityRegex.containsMatchIn(html)) {
            // Quick reject non-html
            html.replace("<br>", "")
        } else parseHtmlInternal(html, imageGetter, isTopic)
    }

    private fun parseHtmlInternal(html: String, imageGetter: Html.ImageGetter?, isTopic: Boolean): SpannableStringBuilder {
        val builder = Html.fromHtml(html, imageGetter) as SpannableStringBuilder

        if (isTopic) {
            val length = builder.length
            if (length > 2) {
                val subSequence = builder.subSequence(length - 2, length)
                if (TextUtils.equals(subSequence, "\n\n")) {
                    builder.delete(length - 2, length)
                }
            }
        }

        return builder
    }

    @JvmStatic
    fun setSpannableFactory(view: TextView) {
        view.setSpannableFactory(spannableFactory)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    fun setImageTintList(view: ImageView, @ColorRes resId: Int) {
        val colorStateList = ContextCompat.getColorStateList(view.context, resId)
        view.imageTintList = colorStateList
    }

    @JvmStatic
    fun showInputMethod(view: View) {
        val manager = view.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.showSoftInput(view, 0)
    }

    /**
     * @param view any view in the window
     */
    @JvmStatic
    fun hideInputMethod(view: View) {
        val manager = view.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun getAttrColor(theme: Resources.Theme, @AttrRes attrId: Int): Int {
        val typedValue = TypedValue()
        if (!theme.resolveAttribute(attrId, typedValue, true)) {
            throw IllegalArgumentException("can't found attr for: " + Integer.toHexString(attrId))
        }

        return typedValue.data
    }

    @JvmStatic
    fun initToolbar(activity: AppCompatActivity): Toolbar? {
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar) ?: return null

        activity.setSupportActionBar(toolbar)
        return toolbar
    }
}
