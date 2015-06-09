package com.czbix.v2ex.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.eventbus.BusEvent.NewUnreadEvent;
import com.czbix.v2ex.ui.MainActivity;
import com.google.common.eventbus.Subscribe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NotificationStatus {
    private static final NotificationStatus instance;

    public static final int ID_NOTIFICATIONS = 0;

    static {
        instance = new NotificationStatus(AppCtx.getInstance());
    }

    public static NotificationStatus getInstance() {
        return instance;
    }

    private final NotificationManagerCompat mNtfManager;
    private final Context mContext;

    NotificationStatus(Context context) {
        mContext = context;
        mNtfManager = NotificationManagerCompat.from(mContext);
    }

    public void init() {
        AppCtx.getEventBus().register(this);
    }

    @Subscribe
    public void onNewUnread(NewUnreadEvent e) {
        if (!e.hasNew()) {
            cancelNotification(ID_NOTIFICATIONS);
            return;
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setTicker(mContext.getString(R.string.ntf_title_new_notifications))
                .setContentTitle(mContext.getString(R.string.ntf_title_new_notifications))
                .setContentText(mContext.getString(R.string.ntf_desc_from_v2ex))
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setNumber(e.mCount)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);

        final Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(MainActivity.BUNDLE_GOTO, MainActivity.GOTO_NOTIFICATIONS)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);

        mNtfManager.notify(ID_NOTIFICATIONS, builder.build());
    }

    public void cancelNotification(@NotificationId int id) {
        mNtfManager.cancel(id);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ID_NOTIFICATIONS})
    public @interface NotificationId {}
}
