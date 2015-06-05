package com.czbix.v2ex.util;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.czbix.v2ex.R;
import com.czbix.v2ex.network.RequestHelper;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

public class MiscUtils {
    public static final boolean HAS_L = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;

    public static void setClipboard(Context context,@Nullable String title, String str) {
        final ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(title, str));

        Toast.makeText(context, R.string.toast_copied, Toast.LENGTH_SHORT).show();
    }

    public static Intent getUrlIntent(String url) {
        Uri uri = Uri.parse(url);
        if (uri.isRelative()) {
            uri = Uri.parse(RequestHelper.BASE_URL + url);
        }

        return new Intent(Intent.ACTION_VIEW, uri);
    }
}
