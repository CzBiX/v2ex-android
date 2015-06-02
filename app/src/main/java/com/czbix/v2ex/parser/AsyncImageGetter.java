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
import com.czbix.v2ex.R;
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
        private static final Drawable DRAWABLE_LOADING;
        private static final Drawable DRAWABLE_FAILED;
        private static final Rect DRAWABLE_BOUNDS;
        private static final Paint PAINT;
        private boolean isFailed;
        private Drawable mDrawable;


        static {
            DRAWABLE_LOADING = AppCtx.getInstance().getResources().getDrawable(R.drawable.ic_sync_white_24dp);
            DRAWABLE_FAILED = AppCtx.getInstance().getResources().getDrawable(R.drawable.ic_sync_problem_white_24dp);
            Preconditions.checkNotNull(DRAWABLE_LOADING);
            Preconditions.checkNotNull(DRAWABLE_FAILED);

            DRAWABLE_BOUNDS = new Rect(0, 0, DRAWABLE_FAILED.getIntrinsicWidth(),
                    DRAWABLE_FAILED.getIntrinsicHeight());
            DRAWABLE_LOADING.setBounds(DRAWABLE_BOUNDS);
            DRAWABLE_FAILED.setBounds(DRAWABLE_BOUNDS);

            PAINT = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            PAINT.setColor(Color.LTGRAY);
        }

        @SuppressWarnings("deprecation")
        public NetworkDrawable() {
            super();
            setBounds(DRAWABLE_BOUNDS);
        }

        public void setDrawable(Drawable drawable) {
            if (drawable == null) {
                isFailed = true;
                return;
            }
            mDrawable = drawable;
            setBounds(mDrawable.getBounds());
        }

        @Override
        public void draw(Canvas canvas) {
            if (mDrawable == null) {
                canvas.drawRect(getBounds(), PAINT);
                if (isFailed) {
                    DRAWABLE_FAILED.draw(canvas);
                } else {
                    DRAWABLE_LOADING.draw(canvas);
                }
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
            mDrawable.setDrawable(bitmapDrawable);

            mTextView.setText(mTextView.getText());
        }
    }
}
