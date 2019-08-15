package com.czbix.v2ex.parser;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.PrefStore;
import com.czbix.v2ex.ui.util.Html;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.MiscUtils;
import com.czbix.v2ex.util.ViewUtils;
import com.google.common.base.Preconditions;

import org.jetbrains.annotations.NotNull;

public class AsyncImageGetter implements Html.ImageGetter {
    private static final String TAG = AsyncImageGetter.class.getSimpleName();
    private final TextView mTextView;
    private final int mMaxWidth;

    /**
     * @param maxWidth max width in pixel
     */
    public AsyncImageGetter(TextView textView, int maxWidth) {
        mTextView = textView;
        mMaxWidth = maxWidth;
    }

    @Override
    public Drawable getDrawable(String source) {
        source = MiscUtils.formatUrl(source);
        LogUtils.v(TAG, "load image for text view: %s", source);

        boolean shouldLoadImage = PrefStore.getInstance().shouldLoadImage();
        final NetworkDrawable drawable = new NetworkDrawable(shouldLoadImage);
        if (shouldLoadImage) {
            final int width = ViewUtils.getExactlyWidth(mTextView, mMaxWidth);
            final NetworkDrawableTarget target = new NetworkDrawableTarget(mTextView, drawable, width);
            Glide.with(mTextView.getContext()).asBitmap().centerInside().load(source).into(target);
        }
        return drawable;
    }

    private static class NetworkDrawable extends Drawable {
        private static final Drawable DRAWABLE_LOADING;
        private static final Drawable DRAWABLE_PROBLEM;
        private static final Drawable DRAWABLE_DISABLED;
        private static final Rect DRAWABLE_BOUNDS;
        private static final Paint PAINT;

        private boolean mIsDisabled;
        private boolean mIsFailed;
        private Drawable mDrawable;


        static {
            DRAWABLE_LOADING = ContextCompat.getDrawable(AppCtx.getInstance(),
                    R.drawable.ic_sync_white_24dp);
            DRAWABLE_PROBLEM = ContextCompat.getDrawable(AppCtx.getInstance(),
                    R.drawable.ic_sync_problem_white_24dp);
            DRAWABLE_DISABLED = ContextCompat.getDrawable(AppCtx.getInstance(),
                    R.drawable.ic_sync_disabled_white_24dp);

            Preconditions.checkNotNull(DRAWABLE_LOADING);
            Preconditions.checkNotNull(DRAWABLE_PROBLEM);
            Preconditions.checkNotNull(DRAWABLE_DISABLED);

            DRAWABLE_BOUNDS = new Rect(0, 0, DRAWABLE_LOADING.getIntrinsicWidth(),
                    DRAWABLE_PROBLEM.getIntrinsicHeight());
            DRAWABLE_LOADING.setBounds(DRAWABLE_BOUNDS);
            DRAWABLE_PROBLEM.setBounds(DRAWABLE_BOUNDS);
            DRAWABLE_DISABLED.setBounds(DRAWABLE_BOUNDS);

            PAINT = new Paint();
            PAINT.setColor(Color.LTGRAY);
        }

        NetworkDrawable(boolean shouldLoadImage) {
            super();
            setBounds(DRAWABLE_BOUNDS);
            mIsDisabled = !shouldLoadImage;
        }

        public void setDrawable(Drawable drawable) {
            mDrawable = drawable;
            if (drawable == null) {
                setBounds(0, 0, 0, 0);
            } else {
                setBounds(mDrawable.getBounds());
            }
        }

        void setFailed() {
            mIsFailed = true;
        }

        @Override
        public void draw(@NotNull Canvas canvas) {
            if (mDrawable != null) {
                mDrawable.draw(canvas);
                return;
            }

            canvas.drawRect(getBounds(), PAINT);
            if (mIsDisabled) {
                DRAWABLE_DISABLED.draw(canvas);
            } else if (mIsFailed) {
                DRAWABLE_PROBLEM.draw(canvas);
            } else {
                DRAWABLE_LOADING.draw(canvas);
            }
        }

        @Override
        public void setAlpha(int i) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            if (mDrawable != null) {
                return mDrawable.getOpacity();
            }

            return DRAWABLE_LOADING.getOpacity();
        }
    }

    private static class NetworkDrawableTarget extends CustomTarget<Bitmap> {
        private final TextView mTextView;
        private final NetworkDrawable mDrawable;
        private final int mMaxWidth;

        private NetworkDrawableTarget(TextView textView, NetworkDrawable drawable, int maxWidth) {
            super(maxWidth, SIZE_ORIGINAL);
            mMaxWidth = maxWidth;
            mTextView = textView;
            mDrawable = drawable;
        }

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            final BitmapDrawable bitmapDrawable = new BitmapDrawable(null, resource);

            int width = resource.getWidth();
            int height = resource.getHeight();

            float fitWidth = width > mMaxWidth ? mMaxWidth : Math.min(ViewUtils.dp2Pixel(width), mMaxWidth);
            height *= fitWidth / width;
            width = (int) fitWidth;

            bitmapDrawable.setBounds(0, 0, width, height);
            mDrawable.setDrawable(bitmapDrawable);

            mTextView.setText(mTextView.getText());
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            mDrawable.setFailed();
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            mDrawable.setDrawable(null);
        }
    }
}
