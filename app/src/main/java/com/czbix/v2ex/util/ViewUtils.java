package com.czbix.v2ex.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.czbix.v2ex.parser.AsyncImageGetter;
import com.czbix.v2ex.ui.util.Html;

public class ViewUtils {
    public static float convertDp2Pixel(Context context, float dp) {
        return context.getResources().getDisplayMetrics().density * dp;
    }

    public static int getScreenWidthPixel(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * @param dimenRes dimen resources id for fallback
     */
    public static int getExactlyWidth(View view, @DimenRes int dimenRes) {
        int width = view.getWidth();
        if (width <= 0) {
            view.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.UNSPECIFIED);
            width = view.getMeasuredWidth();

            if (width <= 0) {
                width = view.getContext().getResources().getDimensionPixelSize(dimenRes);

                if (width <= 0) {
                    // fallback to percent
                    width = (int) (getScreenWidthPixel(view.getContext()) * 0.8f);
                }
            }
        }

        LogUtils.v(ViewUtils.class, "get exactly width, result: %d, view: %s", width, view);
        return width;
    }

    public static void setHtmlIntoTextViewWithRes(TextView view, String html, @DimenRes int dimenRes) {
        setHtmlIntoTextView(view, html, AsyncImageGetter.fromRes(view, dimenRes));
    }

    public static void setHtmlIntoTextViewWithPixel(TextView view, String html, int pixel) {
        setHtmlIntoTextView(view, html, AsyncImageGetter.fromPixel(view, pixel));
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
