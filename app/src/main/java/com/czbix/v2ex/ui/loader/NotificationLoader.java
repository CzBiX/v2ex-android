package com.czbix.v2ex.ui.loader;

import android.content.Context;

import com.czbix.v2ex.model.Notification;
import com.czbix.v2ex.network.RequestHelper;

import java.util.List;

public class NotificationLoader extends AsyncTaskLoader<List<Notification>> {
    public NotificationLoader(Context context) {
        super(context);
    }

    @Override
    public List<Notification> loadInBackgroundWithException() throws Exception {
        return RequestHelper.INSTANCE.getNotifications();
    }
}
