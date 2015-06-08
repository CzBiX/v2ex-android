package com.czbix.v2ex.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.util.LogUtils;
import com.google.common.base.Preconditions;

public class V2exDb extends SQLiteOpenHelper {
    private static final String TAG = V2exDb.class.getSimpleName();
    private static final String DB_NAME = "v2ex.db";
    private static final int CURRENT_VERSION = 2;

    private static final V2exDb INSTANCE;

    static {
        INSTANCE = new V2exDb(AppCtx.getInstance());
    }

    public static V2exDb getInstance() {
        return INSTANCE;
    }

    public V2exDb(Context context) {
        super(context, DB_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ConfigDao.createTable(db);
        NodeDao.createTable(db);
        DraftDao.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            LogUtils.i(TAG, "upgrade database from %d to %d", oldVersion, newVersion);
            TopicDao.createTable(db);
            oldVersion = 2;
        }

        Preconditions.checkState(oldVersion == newVersion, "old version not match new version");
    }
}
