package com.czbix.v2ex.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.czbix.v2ex.R;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.UnauthorizedException;

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
            stringId = R.string.toast_remote_exception;
        } else {
            throw e;
        }

        Toast.makeText(activity, stringId, Toast.LENGTH_LONG).show();
        return needFinishActivity;
    }
}
