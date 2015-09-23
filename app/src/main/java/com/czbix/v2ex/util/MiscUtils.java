package com.czbix.v2ex.util;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.widget.Toast;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.R;
import com.czbix.v2ex.helper.CustomTabsHelper;
import com.czbix.v2ex.network.RequestHelper;

import java.util.List;

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

    public static boolean isEmailLink(String url) {
        return url.startsWith("mailto:");
    }

    public static Intent getEmailIntent(String url) {
        Uri uri = Uri.parse(url);
        return new Intent(Intent.ACTION_SENDTO, uri);
    }

    public static Uri formatUri(String url) {
        Uri uri = Uri.parse(url);

        if (uri.isRelative()) {
            uri = Uri.parse(RequestHelper.BASE_URL + url);
        }

        return uri;
    }

    public static void openUrl(Activity activity, String url) {
        if (isEmailLink(url)) {
            final Intent emailIntent = getEmailIntent(url);
            activity.startActivity(emailIntent);
            return;
        }

        final Uri uri = formatUri(url);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        boolean findMyself = false;
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (resolveInfo.activityInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                findMyself = true;
                break;
            }
        }

        if (findMyself) {
            intent.setPackage(BuildConfig.APPLICATION_ID);
            activity.startActivity(intent);
            return;
        }

        final CustomTabsIntent.Builder builder = CustomTabsHelper.getBuilder(activity, null);
        builder.build().launchUrl(activity, uri);
    }
}
