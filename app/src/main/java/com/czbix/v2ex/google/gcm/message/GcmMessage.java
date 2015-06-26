package com.czbix.v2ex.google.gcm.message;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.czbix.v2ex.util.LogUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public abstract class GcmMessage {
    @NonNull
    public static GcmMessage from(Bundle data) {
        final String type = data.getString("type");
        Preconditions.checkState(!Strings.isNullOrEmpty(type), "GCM message type can't be null/empty");

        switch (type) {
            case NotificationGcmMessage.MSG_TYPE:
                return new NotificationGcmMessage();
            default:
                return new UnsupportedGcmMessage(type);
        }
    }

    public static void handleMessage(Context context, GcmMessage message) {
        message.handleMessage(context);
    }

    /**
     * handle message in a background thread, be thread safe if need
     */
    protected abstract void handleMessage(Context context);

    private static class UnsupportedGcmMessage extends GcmMessage {
        private final String mType;

        public UnsupportedGcmMessage(String type) {
            mType = type;
        }

        @Override
        protected void handleMessage(Context context) {
            LogUtils.d(UnsupportedGcmMessage.class, "unsupported GCM message type: %s, do nothing",
                    mType);
        }
    }
}
