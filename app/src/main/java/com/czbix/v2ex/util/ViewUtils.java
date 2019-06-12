package com.czbix.v2ex.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.parser.AsyncImageGetter;
import com.czbix.v2ex.ui.util.Html;

import java.util.regex.Pattern;

import kotlin.text.Regex;

public class ViewUtils {
    public static final float density;
    public static final Regex tagRegex = new Regex("<(/\\w{1,6}>|img)");

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
        if (!isTopic && !tagRegex.containsMatchIn(html)) {
            // Quick reject non-html
            view.setText(html.replace("<br>", "\n"));
            return;
        }
        setHtmlIntoTextView(view, html, new AsyncImageGetter(view, maxWidthPixels), isTopic);
    }

    private static void setHtmlIntoTextView(TextView view, String html, AsyncImageGetter imageGetter, boolean isTopic) {
        final Spanned spanned = Html.fromHtml(html, imageGetter);
        final SpannableStringBuilder builder = (SpannableStringBuilder) spanned;

        if (isTopic) {
            final int length = builder.length();
            if (length > 2) {
                final CharSequence subSequence = builder.subSequence(length - 2, length);
                if (TextUtils.equals(subSequence, "\n\n")) {
                    builder.delete(length - 2, length);
                }
            }
        }
        view.setText(builder);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setImageTintList(ImageView view, @ColorRes int resId) {
        ColorStateList colorStateList = ContextCompat.getColorStateList(view.getContext(), resId);
        if (MiscUtils.HAS_L) {
            view.setImageTintList(colorStateList);
        } else {
            Drawable drawable = DrawableCompat.wrap(view.getDrawable());
            DrawableCompat.setTintList(drawable, colorStateList);
            view.setImageDrawable(drawable);
        }
    }

    @SuppressLint("NewApi")
    public static Drawable getDrawable(Context context, @DrawableRes int resId) {
        if (MiscUtils.HAS_L) {
            return context.getDrawable(resId);
        }

        final Resources res = context.getResources();
        //noinspection deprecation
        return res.getDrawable(resId);
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackground(View view, @Nullable Drawable drawable) {
        if (MiscUtils.HAS_JB) {
            view.setBackground(drawable);
        } else {
            //noinspection deprecation
            view.setBackgroundDrawable(drawable);
        }
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
