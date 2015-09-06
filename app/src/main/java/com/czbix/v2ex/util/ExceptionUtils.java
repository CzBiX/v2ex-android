package com.czbix.v2ex.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.ExIllegalStateException;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.common.exception.UnauthorizedException;
import com.czbix.v2ex.network.HttpStatus;

public class ExceptionUtils {
    /**
     * warp exception in {@link #handleException(Fragment, Exception)} with {@link FatalException}
     */
    public static boolean handleExceptionNoCatch(Fragment fragment, Exception ex) {
        try {
            return handleException(fragment, ex);
        } catch (Exception e) {
            throw new FatalException(e);
        }
    }

    public static boolean handleException(Fragment fragment, Exception e) throws Exception {
        final FragmentActivity activity = fragment.getActivity();
        boolean needFinishActivity = false;
        int stringId;
        if (e instanceof UnauthorizedException) {
            needFinishActivity = true;
            stringId = R.string.toast_need_sign_in;
        } else if (e instanceof ConnectionException) {
            stringId = R.string.toast_connection_exception;
        } else if (e instanceof RemoteException) {
            Crashlytics.logException(e);
            stringId = R.string.toast_remote_exception;
        } else if (e instanceof RequestException) {
            final RequestException ex = (RequestException) e;
            Crashlytics.logException(ex);
            switch (ex.getCode()) {
                case HttpStatus.SC_FORBIDDEN:
                    stringId = R.string.toast_access_denied;
                    break;
                default:
                    throw e;
            }
        } else if (e instanceof IllegalStateException) {
            boolean logException = true;
            if (e instanceof ExIllegalStateException) {
                final ExIllegalStateException ex = (ExIllegalStateException) e;
                logException = ex.shouldLogged;
            }
            if (logException) {
                Crashlytics.logException(e);
            }

            stringId = R.string.toast_parse_failed;
        } else {
            throw e;
        }

        if (fragment.getUserVisibleHint()) {
            Toast.makeText(activity, stringId, Toast.LENGTH_LONG).show();
        }
        return needFinishActivity;
    }
}
