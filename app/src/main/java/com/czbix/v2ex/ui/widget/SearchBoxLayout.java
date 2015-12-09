package com.czbix.v2ex.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czbix.v2ex.R;
import com.czbix.v2ex.util.MiscUtils;
import com.czbix.v2ex.util.ViewUtils;

public class SearchBoxLayout extends FrameLayout implements View.OnClickListener, TextWatcher, TextView.OnEditorActionListener {
    private ImageButton mBtnBack;
    private ImageButton mBtnClear;
    private EditText mQuery;
    private RelativeLayout mBox;
    private Listener mListener;

    public SearchBoxLayout(Context context) {
        super(context);
        init();
    }

    public SearchBoxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchBoxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final Context context = getContext();
        inflate(context, R.layout.view_search_box, this);

        setBackgroundResource(R.color.transparent_background);

        mBox = (RelativeLayout) findViewById(R.id.box);
        if (!MiscUtils.HAS_L) {
            ViewCompat.setElevation(mBox, ViewUtils.getDimensionPixelSize(R.dimen.appbar_elevation));
        }

        setOnClickListener(this);
        mBtnBack = ((ImageButton) findViewById(R.id.action_back));
        mBtnClear = ((ImageButton) findViewById(R.id.action_clear));
        mQuery = ((EditText) findViewById(R.id.query));

        mBtnBack.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);

        mQuery.addTextChangedListener(this);
        mQuery.setOnEditorActionListener(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void show() {
        mListener.start();
        setVisibility(VISIBLE);

        if (MiscUtils.HAS_L) {
            final int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
            final Animator boxAnimator = ViewAnimationUtils.createCircularReveal(mBox,
                    mBox.getWidth(), mBox.getHeight() / 2, 0, mBox.getWidth())
                    .setDuration(animDuration);

            boxAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mQuery.requestFocus();
                    ViewUtils.showInputMethod(mQuery);
                }
            });

            boxAnimator.start();
        } else {
            mQuery.requestFocus();
        }
    }

    public void hide() {
        hide(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void hide(boolean withAnimation) {
        mListener.end();
        ViewUtils.hideInputMethod(this);

        if (withAnimation && MiscUtils.HAS_L) {
            final int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
            final Animator boxAnimator = ViewAnimationUtils.createCircularReveal(mBox,
                    mBox.getWidth(), mBox.getHeight() / 2, mBox.getWidth(), 0)
                    .setDuration(animDuration);

            boxAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setVisibility(GONE);
                    mQuery.setText("");
                }
            });

            boxAnimator.start();
        } else {
            setVisibility(GONE);
            mQuery.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == this) {
            hide();
            return;
        }

        switch (v.getId()) {
            case R.id.action_back:
                hide();
                break;
            case R.id.action_clear:
                mQuery.setText("");
                break;
        }
    }

    public void setOnActionListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        final String newText = s.toString();
        mListener.onQueryTextChange(newText);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_SEARCH) {
            return false;
        }

        final String query = v.getText().toString();
        return mListener.onQueryTextSubmit(query);
    }

    public interface Listener {
        void onQueryTextChange(String newText);
        boolean onQueryTextSubmit(String query);
        void start();
        void end();
    }
}
