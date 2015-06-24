package com.czbix.v2ex;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class BackupAgent extends BackupAgentHelper {
    private static final String PREFS_BACKUP_KEY = "prefs";

    private static final String PREFS_PREF = BuildConfig.APPLICATION_ID + "_preferences";
    public void onCreate() {
        super.onCreate();

        BackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS_PREF);
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
