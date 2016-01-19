package com.czbix.v2ex.ui.presenter;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.widget.TopicView;

public class FloatTopicPresenter {
    private final TopicView mView;

    public FloatTopicPresenter(Context context) {
        mView = new TopicView(context);
        mView.setVisibility(View.INVISIBLE);

        init(context);
    }

    private void init(Context context) {
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );

        windowManager.addView(mView, layoutParams);
    }

    public void fillData(Topic topic) {
        mView.fillData(topic);
    }

    public void setVisibility(int visibility) {
        mView.setVisibility(visibility);
    }
}
