package com.czbix.v2ex.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.Nullable;

import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Node;
import com.google.common.base.Preconditions;

public class NodeDAO {
    private static final String TABLE_NAME = "node";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TITLE = "title";
    // meaning alternative
    private static final String KEY_ALT = "alt";
    private static final String KEY_AVATAR = "avatar";

    private static final String[] SCHEMA = {KEY_ID, KEY_NAME, KEY_TITLE, KEY_ALT, KEY_AVATAR};

    private static final String SQL_GET_BY_NAME = SQLiteQueryBuilder.buildQueryString(false,
            TABLE_NAME, SCHEMA, KEY_NAME + " = ?", null, null, null ,null);

    public static void createTable(SQLiteDatabase db) {
        Preconditions.checkState(db.inTransaction(), "create table must be in transaction");

        String sql = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT UNIQUE NOT NULL,"
                + KEY_TITLE + " TEXT NOT NULL,"
                + KEY_ALT + " TEXT,"
                + KEY_AVATAR + " TEXT"
                + ")";

        db.execSQL(sql);

        sql = "CREATE UNIQUE INDEX index_name ON " + TABLE_NAME + "("
                + KEY_NAME + ")";
        db.execSQL(sql);
    }

    @Nullable
    public static Node get(String name) {
        final SQLiteDatabase db = getReadDb();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL_GET_BY_NAME, new String[]{name});
            if (!cursor.moveToFirst()) {
                return null;
            }

            Node.Builder builder = new Node.Builder();
            builder.setId(cursor.getInt(0))
                    .setName(cursor.getString(1))
                    .setTitle(cursor.getString(2))
                    .setTitleAlternative(cursor.getString(3));
            final Avatar avatar = new Avatar.Builder().setBaseUrl(cursor.getString(4)).createAvatar();
            builder.setAvatar(avatar);

            return builder.createNode();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static SQLiteDatabase getWriteDb() {
        return V2exDb.getInstance().getWritableDatabase();
    }

    private static SQLiteDatabase getReadDb() {
        return V2exDb.getInstance().getReadableDatabase();
    }

    public static void insert(Node node) {
        final SQLiteDatabase db = getWriteDb();
        final ContentValues values = new ContentValues(5);
        values.put(KEY_ID, node.getId());
        values.put(KEY_NAME, node.getName());
        values.put(KEY_TITLE, node.getTitle());
        values.put(KEY_ALT, node.getAlternative());
        values.put(KEY_AVATAR, node.getAvatar().getBaseUrl());

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
