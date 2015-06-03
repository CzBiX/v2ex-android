package com.czbix.v2ex.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.czbix.v2ex.R;

public class UiUtils {
    public static void setClipboard(Context context,@Nullable String title, String str) {
        final ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(title, str));

        Toast.makeText(context, R.string.toast_copied, Toast.LENGTH_SHORT).show();
    }
}
