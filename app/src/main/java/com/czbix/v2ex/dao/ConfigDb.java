package com.czbix.v2ex.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.czbix.v2ex.AppCtx;

public class ConfigDb extends SQLiteOpenHelper {
    private static final String DB_NAME = "config.db";
    private static final int CURRENT_VERSION = 1;

    private static final ConfigDb INSTANCE;

    static {
        INSTANCE = new ConfigDb(AppCtx.getInstance());
    }

    public static ConfigDb getInstance() {
        return INSTANCE;
    }

    public ConfigDb(Context context) {
        super(context, DB_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ConfigDao.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
