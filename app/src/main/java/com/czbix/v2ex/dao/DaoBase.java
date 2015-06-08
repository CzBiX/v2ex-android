package com.czbix.v2ex.dao;

import android.database.sqlite.SQLiteDatabase;

public abstract class DaoBase {
    protected static <T> T execute(SqlOperation<T> operation) {
        return execute(operation, false);
    }

    protected static <T> T execute(SqlOperation<T> operation, boolean isWrite) {
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
