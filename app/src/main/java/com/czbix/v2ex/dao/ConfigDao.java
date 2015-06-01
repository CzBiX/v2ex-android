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

    public static final String KEY_USERNAME = "username";
    public static final String KEY_AVATAR = "avatar";
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

    public static String get(final String key, final String defVal) {
        String value = CACHE.get(key);
        if (value != null) {
            return value;
        }

        return execute(new SqlOperation<String>() {
            @Override
            public String execute (SQLiteDatabase db){
                Cursor cursor = null;
                try {
                    cursor = db.rawQuery(SQL_GET_BY_NAME, new String[]{key});
                    if (!cursor.moveToFirst()) {
                        return defVal;
                    }

                    String value = cursor.getString(0);
                    CACHE.put(key, value);

                    return value;
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }, false);
    }

    public static void put(final String key, final String value) {
        execute(new SqlOperation<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                final ContentValues values = new ContentValues(2);
                values.put(KEY_KEY, key);
                values.put(KEY_VALUE, value);

                db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                CACHE.put(key, value);
                return null;
            }
        }, true);
    }

    public static void remove(final String key) {
        execute(new SqlOperation<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.delete(TABLE_NAME, KEY_KEY + " = ?", new String[]{key});
                return null;
            }
        }, true);
    }

    private static <T> T execute(SqlOperation<T> operation, boolean isWrite) {
        SQLiteDatabase db = null;
        try {
            final ConfigDb instance = ConfigDb.getInstance();
            db = isWrite ? instance.getWritableDatabase() : instance.getReadableDatabase();
            return operation.execute(db);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
