package com.czbix.v2ex.ui.helper;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageButton;

import com.czbix.v2ex.R;
import com.czbix.v2ex.util.MiscUtils;
import com.czbix.v2ex.util.ViewUtils;

public class ReplyFormHelper implements TextWatcher, View.OnClickListener {
    private final EditText mContent;
    private final ImageButton mSubmit;
    private final View mRootView;
    private final OnReplyListener mListener;
    private final ImageButton mUpload;
    private final Activity mActivity;
    private boolean isShown;

    public ReplyFormHelper(Activity activity, ViewStub viewStub, OnReplyListener listener) {
        mActivity = activity;
        mRootView = viewStub.inflate();
        mListener = listener;

        mContent = ((EditText) mRootView.findViewById(R.id.content));
        mUpload = (ImageButton) mRootView.findViewById(R.id.upload);
        mSubmit = ((ImageButton) mRootView.findViewById(R.id.submit));

        ViewUtils.setImageTintList(mSubmit, R.color.btn_reply_submit);

        mContent.addTextChangedListener(this);
        mUpload.setOnClickListener(this);
        mSubmit.setOnClickListener(this);

        afterTextChanged(mContent.getText());
        isShown = true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        final boolean isEmpty = TextUtils.isEmpty(s);
        mSubmit.setEnabled(!isEmpty);
        mUpload.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    public void toggle() {
        isShown = !isShown;
        setVisibility(isShown);
    }

    public boolean getVisibility() {
        return isShown;
    }

    public void setVisibility(boolean visible) {
        isShown = visible;
        mRootView.setVisibility(visible ? View.VISIBLE : View.GONE);

        if (!isShown) {
            ViewUtils.hideInputMethod(mRootView);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.upload) {
            MiscUtils.openUrl(mActivity, "https://m.imgur.com/upload/redirect");
        } else if (v.getId() == R.id.submit) {
            mListener.onReply(mContent.getText());
        }
    }

    public interface OnReplyListener {
        void onReply(CharSequence content);
    }

    public void setContent(CharSequence content) {
        mContent.setText(content);
        if (TextUtils.isEmpty(content)) {
            return;
        }
        setVisibility(true);
    }

    public Editable getContent() {
        return mContent.getText();
    }

    public void requestFocus() {
        mContent.requestFocus();
    }
}
