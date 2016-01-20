package com.czbix.v2ex.ui.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.czbix.v2ex.R;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.ui.widget.TopicView;

import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

public class FloatTopicPresenter {
    private final View mRootView;
    private final TopicView mTopicView;
    private final ProgressBar mProgress;

    @SuppressLint("InflateParams")
    public FloatTopicPresenter(Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        mRootView = inflater.inflate(R.layout.view_float_topic, null);
        mProgress = ((ProgressBar) mRootView.findViewById(R.id.progress));
        mTopicView = (TopicView) mRootView.findViewById(R.id.topic);
        mRootView.setVisibility(View.INVISIBLE);

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

        windowManager.addView(mRootView, layoutParams);
    }

    public void fillData(Topic topic) {
        mTopicView.fillData(topic);
        mProgress.setVisibility(View.VISIBLE);

        RequestHelper.getTopicByApi(topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    mTopicView.fillData(t);
                    mProgress.setVisibility(View.INVISIBLE);
                }, e -> {
                    if (e instanceof ConnectionException) {
                        Toast.makeText(mRootView.getContext(), R.string.toast_connection_exception,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    public void setVisibility(int visibility) {
        mRootView.setVisibility(visibility);
    }
}
