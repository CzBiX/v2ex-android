package com.czbix.v2ex.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.LruCache;

import com.google.common.base.Preconditions;

public class ConfigDao {
    private static final String TABLE_NAME = "config";

    private static final String KEY_ID = "id";
    private static final String KEY_KEY = "key";
    private static final String KEY_VALUE = "value";

    private static final String[] SCHEMA = {KEY_ID, KEY_KEY, KEY_VALUE};
    private static final String SQL_GET_BY_NAME = SQLiteQueryBuilder.buildQueryString(false, TABLE_NAME, new String[]{KEY_VALUE}, KEY_KEY + " = ?", null, null, null, null);

    private static final LruCache<String, String> CACHE = new LruCache<>(8);

    public static final String KEY_NODE_ETAG = "node_etag";

    static void createTable(SQLiteDatabase db) {
        Preconditions.checkState(db.inTransaction(), "create table must be in transaction");

        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_KEY + " TEXT UNIQUE NOT NULL," +
                KEY_VALUE + " TEXT" +
                ")";
        db.execSQL(sql);

        sql = "CREATE UNIQUE INDEX index_name ON " + TABLE_NAME + "(" +
                KEY_KEY + ")";
        db.execSQL(sql);
    }

    public static String get(String key, String defVal) {
        String value = CACHE.get(key);
        if (value != null) {
            return value;
        }

        final SQLiteDatabase db = getReadDb();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL_GET_BY_NAME, new String[]{key});
            if (!cursor.moveToFirst()) {
                return defVal;
            }

            value = cursor.getString(0);
            CACHE.put(key, value);

            return value;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void put(String key, String value) {
        final SQLiteDatabase db = getWriteDb();
        final ContentValues values = new ContentValues(2);
        values.put(KEY_KEY, key);
        values.put(KEY_VALUE, value);

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        CACHE.put(key, value);
    }

    private static SQLiteDatabase getWriteDb() {
        return ConfigDb.getInstance().getWritableDatabase();
    }

    private static SQLiteDatabase getReadDb() {
        return ConfigDb.getInstance().getReadableDatabase();
    }
}
