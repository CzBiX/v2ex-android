package com.czbix.v2ex.util;

import android.content.Context;
import android.support.annotation.DimenRes;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
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
        }
        if (width <= 0) {
            width = view.getContext().getResources().getDimensionPixelSize(dimenRes);
            if (width <= 0) {
                // fallback to percent
                width = (int) (getScreenWidthPixel(view.getContext()) * 0.8f);
            }
        }

        LogUtils.v(ViewUtils.class, "get exactly width, result: %d, view: %s", width, view);
        return width;
    }

    @SuppressWarnings("ConstantConditions")
    public static void setHtmlIntoTextView(TextView view, String html, @DimenRes int dimenRes) {
        final Spanned spanned = Html.fromHtml(html, new AsyncImageGetter(view, dimenRes), null);
        view.setText(spanned);
    }
}
