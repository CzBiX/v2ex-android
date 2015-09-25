package com.czbix.v2ex.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Avatar;

public class AvatarView extends ImageView {
    private int mRealSize;
    private boolean hasRealSize;

    public AvatarView(Context context) {
        super(context);
        init();
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        // empty
    }

    private int getRealSize() {
        if (!hasRealSize) {
            mRealSize = getLayoutParams().width - getPaddingTop() * 2 ;
        }

        return mRealSize;
    }

    public void setAvatar(Avatar avatar) {
        final int size = getRealSize();
        final String url = avatar.getUrlByPx(size);
        Glide.with(getContext()).load(url).placeholder(R.drawable.avatar_default)
                .override(size, size).crossFade().fitCenter().into(this);
    }
}
