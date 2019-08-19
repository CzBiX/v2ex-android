package com.czbix.v2ex.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.parser.AsyncImageGetter;
import com.czbix.v2ex.ui.util.Html;

import kotlin.text.Regex;

public class ViewUtils {
    public static final float density;
    public static final Regex tagRegex = new Regex("<(/\\w{1,6}>|img)");
    public static final Regex entityRegex = new Regex("&(\\w{1,10}|#\\d{1,4});");

    private static final Spannable.Factory spannableFactory = new Spannable.Factory() {
        @Override
        public Spannable newSpannable(CharSequence source) {
            if (source instanceof Spannable) {
                return (Spannable) source;
            }

            return super.newSpannable(source);
        }
    };

    static {
        final AppCtx context = AppCtx.getInstance();
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        density = displayMetrics.density;
    }

    public static int getWidthPixels() {
        return AppCtx.getInstance().getResources().getDisplayMetrics().widthPixels;
    }

    public static float dp2Pixel(float dp) {
        return density * dp;
    }

    public static int getDimensionPixelSize(@DimenRes int id) {
        return AppCtx.getInstance().getResources().getDimensionPixelSize(id);
    }

    public static int getExactlyWidth(View view, int maxWidth) {
        int width = view.getWidth();
        if (width <= 0) {
            view.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.UNSPECIFIED);
            width = view.getMeasuredWidth();

            if (width <= 0) {
                width = maxWidth;
            }
        }

        LogUtils.v(ViewUtils.class, "get exactly width, result: %d, view: %s", width, view);
        return width;
    }

    public static void setHtmlIntoTextView(TextView view, String html, int maxWidthPixels, boolean isTopic) {
        final CharSequence content = parseHtml(view, html, isTopic, maxWidthPixels);
        view.setText(content, TextView.BufferType.SPANNABLE);
    }

    public static CharSequence parseHtml(TextView view, String html, boolean isTopic, int maxWidthPixels) {
        if (!isTopic && !tagRegex.containsMatchIn(html) && !entityRegex.containsMatchIn(html)) {
            // Quick reject non-html
            return html.replace("<br>", "");
        }
        return parseHtml(html, new AsyncImageGetter(view, maxWidthPixels), isTopic);
    }

    private static Spanned parseHtml(String html, AsyncImageGetter imageGetter, boolean isTopic) {
        Spanned spanned = Html.fromHtml(html, imageGetter);

        if (isTopic) {
            final SpannableStringBuilder builder = (SpannableStringBuilder) spanned;
            final int length = builder.length();
            if (length > 2) {
                final CharSequence subSequence = builder.subSequence(length - 2, length);
                if (TextUtils.equals(subSequence, "\n\n")) {
                    builder.delete(length - 2, length);

                    spanned = builder;
                }
            }
        }

        return spanned;
    }

    public static void setSpannableFactory(TextView view) {
        view.setSpannableFactory(spannableFactory);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setImageTintList(ImageView view, @ColorRes int resId) {
        ColorStateList colorStateList = ContextCompat.getColorStateList(view.getContext(), resId);
        view.setImageTintList(colorStateList);
    }

    public static Drawable setDrawableTint(Drawable drawable, int tint) {
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, tint);
        return drawable;
    }

    public static void showInputMethod(View view) {
        final InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, 0);
    }

    /**
     * @param view any view in the window
     */
    public static void hideInputMethod(View view) {
        final InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int getAttrColor(Resources.Theme theme, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        if (!theme.resolveAttribute(attrId, typedValue, true)) {
            throw new IllegalArgumentException("can't found attr for: " + Integer.toHexString(attrId));
        }

        return typedValue.data;
    }

    @Nullable
    public static Toolbar initToolbar(AppCompatActivity activity) {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar == null) {
            return null;
        }

        activity.setSupportActionBar(toolbar);
        return toolbar;
    }
}
