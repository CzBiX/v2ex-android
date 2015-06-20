package com.czbix.v2ex.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.parser.AsyncImageGetter;
import com.czbix.v2ex.ui.util.Html;

public class ViewUtils {
    public static final float density;

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

    public static void setHtmlIntoTextView(TextView view, String html, int maxWidthPixels) {
        setHtmlIntoTextView(view, html, new AsyncImageGetter(view, maxWidthPixels));
    }

    private static void setHtmlIntoTextView(TextView view, String html, AsyncImageGetter imageGetter) {
        final Spanned spanned = Html.fromHtml(html, imageGetter);
        final SpannableStringBuilder builder = (SpannableStringBuilder) spanned;
        final int length = builder.length();
        if (length > 32) {
            final CharSequence subSequence = builder.subSequence(length - 2, length);
            if (TextUtils.equals(subSequence, "\n\n")) {
                builder.delete(length - 2, length);
            }
        }
        view.setText(builder);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setImageTintList(ImageView view, @ColorRes int colorId) {
        if (!MiscUtils.HAS_L) {
            return;
        }

        view.setImageTintList(view.getResources().getColorStateList(colorId));
    }

    /**
     * @param view any view in the window
     */
    public static void hideInputMethod(View view) {
        final InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
