package com.czbix.v2ex.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.google.common.base.Preconditions;

public class TopicDao extends DaoBase {
    private static final String TABLE_NAME = "topic";

    private static final String KEY_TOPIC_ID = "topic_id";
    private static final String KEY_LAST_READ = "last_read";

    private static final String SQL_GET_BY_TOPIC_ID = SQLiteQueryBuilder.buildQueryString(false,
            TABLE_NAME, new String[]{KEY_LAST_READ}, KEY_TOPIC_ID + " = ?", null, null, null, null);

    static void createTable(SQLiteDatabase db) {
        Preconditions.checkState(db.inTransaction());

        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                KEY_TOPIC_ID + " INTEGER PRIMARY KEY," +
                KEY_LAST_READ + " INTEGER" +
                ")";
        db.execSQL(sql);
    }

    public static int getLastRead(final int topicId) {
        return execute(new SqlOperation<Integer>() {
            @Override
            public Integer execute(SQLiteDatabase db) {
                Cursor cursor = null;
                try {
                    cursor = db.rawQuery(SQL_GET_BY_TOPIC_ID, new String[]{Integer.toString(topicId)});
                    if (!cursor.moveToFirst()) {
                        return -1;
                    }

                    return cursor.getInt(0);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    public static void setLastRead(final int topicId, final int num) {
        Preconditions.checkArgument(num > 0);

        execute(new SqlOperation<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                final ContentValues values = new ContentValues(2);
                values.put(KEY_TOPIC_ID, topicId);
                values.put(KEY_LAST_READ, num);

                db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                return null;
            }
        }, true);
    }
}
