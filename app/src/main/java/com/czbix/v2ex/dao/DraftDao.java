package com.czbix.v2ex.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.czbix.v2ex.model.db.Draft;
import com.google.common.base.Preconditions;

public class DraftDao extends DaoBase {
    private static final String TABLE_NAME = "draft";

    private static final String KEY_ID = "id";
    private static final String KEY_TOPIC_ID = "topic_id";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_TIME = "time";

    private static final String[] SCHEMA = {KEY_ID, KEY_TOPIC_ID, KEY_CONTENT, KEY_TIME};
    private static final String SQL_GET_ALL = SQLiteQueryBuilder.buildQueryString(false,
            TABLE_NAME, new String[]{KEY_ID, KEY_TIME}, null, null, null, null, null);
    private static final String SQL_GET_BY_TOPIC_ID = SQLiteQueryBuilder.buildQueryString(false,
            TABLE_NAME, SCHEMA, KEY_TOPIC_ID + " = ?", null, null, null, null);

    static void createTable(SQLiteDatabase db) {
        Preconditions.checkState(db.inTransaction(), "create table must be in transaction");

        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_TOPIC_ID + " INTEGER UNIQUE NOT NULL," +
                KEY_CONTENT + " TEXT NOT NULL," +
                KEY_TIME + " INTEGER" +
                ")";
        db.execSQL(sql);

        sql = String.format("CREATE UNIQUE INDEX %1$s_%2$s ON %1$s(%2$s)", TABLE_NAME, KEY_TOPIC_ID);
        db.execSQL(sql);
    }

    public static Draft get(final int topicId) {
        return execute(new SqlOperation<Draft>() {
            @Override
            public Draft execute(SQLiteDatabase db) {
                Cursor cursor = null;
                try {
                    cursor = db.rawQuery(SQL_GET_BY_TOPIC_ID, new String[]{Integer.toString(topicId)});
                    if (!cursor.moveToFirst()) {
                        return null;
                    }

                    return new Draft(cursor.getLong(0), cursor.getInt(1), cursor.getString(2),
                            cursor.getLong(3));
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    public static void cleanExpired() {
        execute(new SqlOperation<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                Cursor cursor = null;
                try {
                    cursor = db.rawQuery(SQL_GET_ALL, null);

                    while (cursor.moveToNext()) {
                        final long time = cursor.getLong(1);
                        if (Draft.isExpired(time)) {
                            final long id = cursor.getLong(0);
                            db.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{Long.toString(id)});
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                return null;
            }
        }, true);
    }

    public static void delete(final long draftId) {
        execute(new SqlOperation<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{Long.toString(draftId)});

                return null;
            }
        }, true);
    }

    public static long update(final int topicId, final String content) {
        return execute(new SqlOperation<Long>() {
            @Override
            public Long execute(SQLiteDatabase db) {
                final ContentValues values = new ContentValues(3);
                values.put(KEY_TOPIC_ID, topicId);
                values.put(KEY_CONTENT, content);
                values.put(KEY_TIME, System.currentTimeMillis());

                return db.insertWithOnConflict(TABLE_NAME, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
        }, true);
    }
}
