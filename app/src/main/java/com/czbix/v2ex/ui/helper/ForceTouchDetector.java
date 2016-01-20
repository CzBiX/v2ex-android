package com.czbix.v2ex.ui.helper;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.czbix.v2ex.util.LogUtils;

public class ForceTouchDetector {
    private boolean mInForceTouch;
    private final Runnable mOnStartListener;
    private final Runnable mOnStopListener;

    public ForceTouchDetector(@NonNull Runnable onStartListener,
                                        @NonNull Runnable onStopListener) {
        mOnStartListener = onStartListener;
        mOnStopListener = onStopListener;
    }

    public boolean handleEvent(MotionEvent e) {
        if (e.getActionIndex() != 0) {
            // handle first pointer only
            return false;
        }

        final int action = e.getActionMasked();
        final float pressure = e.getPressure();

        if (action == MotionEvent.ACTION_UP) {
            if (mInForceTouch) {
                onStop();
                e.setAction(MotionEvent.ACTION_CANCEL);
                return false;
            }
        }

        if (pressure < 0.8) {
            if (mInForceTouch) {
                if (pressure < 0.7) {
                    onStop();
                    e.setAction(MotionEvent.ACTION_CANCEL);
                    return false;
                }
            } else {
                return false;
            }
        }

        if (action == MotionEvent.ACTION_MOVE) {
            onStart();
            return true;
        } else {
            if (mInForceTouch) {
                onStop();
                return true;
            } else {
                return false;
            }
        }
    }

    private void onStart() {
        if (mInForceTouch) {
            return;
        }

        mInForceTouch = true;
        mOnStartListener.run();
    }

    private void onStop() {
        if (!mInForceTouch) {
            return;
        }

        mInForceTouch = false;
        mOnStopListener.run();
    }
}
