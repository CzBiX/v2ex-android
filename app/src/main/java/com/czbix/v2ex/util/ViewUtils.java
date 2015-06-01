package com.czbix.v2ex.util;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.parser.AsyncImageGetter;
import com.google.common.base.Preconditions;

public class ViewUtils {
    private static final DisplayMetrics DISPLAY_METRICS = AppCtx.getInstance().getResources().getDisplayMetrics();
    public static final int DISPLAY_WIDTH_PX = DISPLAY_METRICS.widthPixels;

    public static int getExactlyWidth(View view) {
        int width = view.getWidth();
        if (width <= 0) {
            view.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.UNSPECIFIED);
            width = view.getMeasuredWidth();
        }
        if (width <= 0) {
            width = DISPLAY_WIDTH_PX / 2;
        }

        LogUtils.v(ViewUtils.class, "get exactly width: %d, view: %s", width, view);
        return width;
    }

    @SuppressWarnings("ConstantConditions")
    public static void setHtmlIntoTextView(TextView view, String html) {
        final Spanned spanned = Html.fromHtml(html, new AsyncImageGetter(view), null);
        Preconditions.checkState(spanned instanceof SpannableStringBuilder, "why it's not builder");
        final SpannableStringBuilder builder = (SpannableStringBuilder) spanned;

        final ImageSpan[] imageSpans = builder.getSpans(0, builder.length(), ImageSpan.class);
        for (ImageSpan imageSpan : imageSpans) {
            final int spanEnd = builder.getSpanEnd(imageSpan);
            builder.insert(spanEnd, "\n");
        }

        view.setText(builder);
    }
}
