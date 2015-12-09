package com.czbix.v2ex.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Member;

public class AvatarView extends ImageView {
    private int mRealSize;
    private boolean mHasRealSize;

    public AvatarView(Context context) {
        this(context, null);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int getRealSize() {
        if (!mHasRealSize) {
            mRealSize = getLayoutParams().width - getPaddingTop() * 2 ;
            mHasRealSize = true;
        }

        return mRealSize;
    }

    public void setAvatar(Avatar avatar) {
        final int size = getRealSize();
        final String url = avatar.getUrlByPx(size);
        Glide.with(getContext()).load(url).placeholder(R.drawable.avatar_default)
                .override(size, size).crossFade().fitCenter().into(this);
    }

    public interface OnAvatarActionListener {
        void onMemberClick(Member member);
    }
}
