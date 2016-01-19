package com.czbix.v2ex.ui.helper;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

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

        if (e.getPressure() < 0.8) {
            if (mInForceTouch) {
                onStop();
                return true;
            }
            // handle heavy pressure only
            return false;
        }

        if (e.getActionMasked() == MotionEvent.ACTION_MOVE) {
            onStart();
        } else {
            if (mInForceTouch) {
                onStop();
            } else {
                return false;
            }
        }

        return true;
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
