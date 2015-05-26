package com.czbix.v2ex.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.czbix.v2ex.AppCtx;

public class V2exDb extends SQLiteOpenHelper {
    private static final String DB_NAME = "v2ex.db";
    private static final int CURRENT_VERSION = 1;

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
        NodeDAO.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
