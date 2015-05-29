package com.czbix.v2ex.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.Nullable;
import android.util.LruCache;

import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Node;
import com.google.common.base.Preconditions;

public class NodeDao {
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

    private static final LruCache<String, Node> CACHE = new LruCache<>(16);

    static void createTable(SQLiteDatabase db) {
        Preconditions.checkState(db.inTransaction(), "create table must be in transaction");

        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_NAME + " TEXT UNIQUE NOT NULL," +
                KEY_TITLE + " TEXT NOT NULL," +
                KEY_ALT + " TEXT," +
                KEY_AVATAR + " TEXT" +
                ")";

        db.execSQL(sql);

        sql = "CREATE UNIQUE INDEX index_name ON " + TABLE_NAME + "(" +
                KEY_NAME + ")";
        db.execSQL(sql);
    }

    @Nullable
    public static Node get(final String name) {
        Node node = CACHE.get(name);
        if (node != null) {
            return node;
        }

        return execute(new SqlOperation<Node>() {
            @Override
            public Node execute(SQLiteDatabase db) {
                Cursor cursor = null;
                try {
                    cursor = db.rawQuery(SQL_GET_BY_NAME, new String[]{name});
                    if (!cursor.moveToFirst()) {
                        return null;
                    }

                    Node.Builder builder = new Node.Builder();
                    builder.setId(cursor.getInt(0))
                            .setName(cursor.getString(1))
                            .setTitle(cursor.getString(2));

                    String str = cursor.getString(3);
                    if (str != null) {
                        builder.setTitleAlternative(str);
                    }

                    str = cursor.getString(4);
                    if (str != null) {
                        final Avatar avatar = new Avatar.Builder().setBaseUrl(str).createAvatar();
                        builder.setAvatar(avatar);
                    }

                    Node node = builder.createNode();
                    CACHE.put(name, node);

                    return node;
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }, false);
    }

    public static void put(final Node node) {
        execute(new SqlOperation<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                put(db, node);
                return null;
            }
        }, true);
    }

    private static void put(SQLiteDatabase db, Node node) {
        final ContentValues values = new ContentValues(5);
        values.put(KEY_ID, node.getId());
        values.put(KEY_NAME, node.getName());
        values.put(KEY_TITLE, node.getTitle());
        values.put(KEY_ALT, node.getTitleAlternative());
        final Avatar avatar = node.getAvatar();
        if (avatar != null) {
            values.put(KEY_AVATAR, avatar.getBaseUrl());
        }

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        CACHE.put(node.getName(), node);
    }

    public static void updateAvatar(final String name, final String url) {
        execute(new SqlOperation<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                final ContentValues values = new ContentValues(1);
                values.put(KEY_AVATAR, url);

                db.update(TABLE_NAME, values, KEY_NAME + " = ?", new String[]{name});

                CACHE.remove(name);

                return null;
            }
        }, true);
    }

    public static void putAll(final Iterable<Node> nodes) {
        execute(new SqlOperation<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.beginTransaction();
                try {

                    for (Node node : nodes) {
                        put(db, node);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }
        }, true);
    }

    private static <T> T execute(SqlOperation<T> operation, boolean isWrite) {
        SQLiteDatabase db = null;
        try {
            final V2exDb instance = V2exDb.getInstance();
            db = isWrite ? instance.getWritableDatabase() : instance.getReadableDatabase();
            return operation.execute(db);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
