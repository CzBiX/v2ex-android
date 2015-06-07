package com.czbix.v2ex.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.czbix.v2ex.parser.AsyncImageGetter;

public class ViewUtils {
    public static int getScreenWidthPixel(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

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

    public static void setHtmlIntoTextView(TextView view, String html, @DimenRes int dimenRes) {
        final Spanned spanned = Html.fromHtml(html, new AsyncImageGetter(view, dimenRes), null);
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
