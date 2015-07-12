package com.czbix.v2ex.util;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.R;
import com.czbix.v2ex.network.RequestHelper;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

public class MiscUtils {
    private static final String HOST_MASTER;
    private static final String HOST_WWW;
    private static final String PREFIX_TOPIC;
    private static final String PREFIX_NODE;
    public static final String PREFIX_MEMBER;

    public static final boolean HAS_L;
    public static final boolean HAS_JB;

    static {
        final AppCtx context = AppCtx.getInstance();
        HOST_MASTER = context.getString(R.string.master_host);
        HOST_WWW = context.getString(R.string.www_host);

        PREFIX_TOPIC = context.getString(R.string.topic_url_prefix);
        PREFIX_NODE = context.getString(R.string.node_url_prefix);
        PREFIX_MEMBER = context.getString(R.string.member_url_prefix);

        final int sdkInt = VERSION.SDK_INT;
        HAS_L = sdkInt >= VERSION_CODES.LOLLIPOP;
        HAS_JB = sdkInt >= VERSION_CODES.JELLY_BEAN;
    }

    public static void setClipboard(Context context,@Nullable String title, String str) {
        final ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(title, str));

        Toast.makeText(context, R.string.toast_copied, Toast.LENGTH_SHORT).show();
    }

    public static Intent getUrlIntent(String url) {
        Uri uri = Uri.parse(url);
        final boolean isEmailAddress = url.startsWith("mailto:");

        if (isEmailAddress) {
            return new Intent(Intent.ACTION_SENDTO, uri);
        }

        if (uri.isRelative()) {
            uri = Uri.parse(RequestHelper.BASE_URL + url);
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        final String host = uri.getHost();
        final String path = uri.getPath();
        if (host != null && path != null) {
            if ((host.equals(HOST_MASTER) || host.equals(HOST_WWW))
                    && (path.startsWith(PREFIX_TOPIC) || path.startsWith(PREFIX_NODE))) {
                intent.setPackage(BuildConfig.APPLICATION_ID);
            }
        }

        return intent;
    }
}
