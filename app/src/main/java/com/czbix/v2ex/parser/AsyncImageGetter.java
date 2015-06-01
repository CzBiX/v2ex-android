package com.czbix.v2ex.parser;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DimenRes;
import android.text.Html;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Preconditions;

public class AsyncImageGetter implements Html.ImageGetter {
    private static final String TAG = AsyncImageGetter.class.getSimpleName();
    private final TextView mTextView;
    private final int mDimenRes;

    public AsyncImageGetter(TextView textView, @DimenRes int dimenRes) {
        mTextView = textView;
        mDimenRes = dimenRes;
    }

    @Override
    public Drawable getDrawable(String source) {
        LogUtils.v(TAG, "load image for text view: %s", source);

        final NetworkDrawable drawable = new NetworkDrawable();
        final int width = ViewUtils.getExactlyWidth(mTextView, mDimenRes);
        final NetworkDrawableTarget target = new NetworkDrawableTarget(mTextView, drawable, width);
        Glide.with(mTextView.getContext()).load(source).asBitmap().fitCenter().into(target);
        return drawable;
    }

    private static class NetworkDrawable extends BitmapDrawable {
        private static final Drawable DEFAULT_DRAWABLE;
        private static final Paint PAINT;
        private Drawable mDrawable;

        static {
            DEFAULT_DRAWABLE = AppCtx.getInstance().getResources().getDrawable(android.R.drawable.presence_offline);
            Preconditions.checkNotNull(DEFAULT_DRAWABLE);
            final Rect rect = new Rect(0, 0, DEFAULT_DRAWABLE.getIntrinsicWidth(),
                    DEFAULT_DRAWABLE.getIntrinsicHeight());
            DEFAULT_DRAWABLE.setBounds(rect);

            PAINT = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            PAINT.setColor(Color.GRAY);
        }

        @SuppressWarnings("deprecation")
        public NetworkDrawable() {
            setBounds(DEFAULT_DRAWABLE.getBounds());
        }

        @Override
        public void draw(Canvas canvas) {
            if (mDrawable == null) {
                canvas.drawRect(getBounds(), PAINT);
                DEFAULT_DRAWABLE.draw(canvas);
                return;
            }

            mDrawable.draw(canvas);
        }
    }

    private static class NetworkDrawableTarget extends SimpleTarget<Bitmap> {
        private final TextView mTextView;
        private final NetworkDrawable mDrawable;

        private NetworkDrawableTarget(TextView textView, NetworkDrawable drawable, int maxWidth) {
            super(maxWidth, SIZE_ORIGINAL);
            mTextView = textView;
            mDrawable = drawable;
        }

        @Override
        public void onResourceReady(Bitmap bitmap,
                                    GlideAnimation<? super Bitmap> glideAnimation) {
            final BitmapDrawable bitmapDrawable = new BitmapDrawable(null, bitmap);
            bitmapDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            mDrawable.mDrawable = bitmapDrawable;
            mDrawable.setBounds(bitmapDrawable.getBounds());

            mTextView.setText(mTextView.getText());
        }
    }
}
