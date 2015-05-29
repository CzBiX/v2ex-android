package com.czbix.v2ex.dao;

import android.database.sqlite.SQLiteDatabase;

public abstract class SqlOperation<T> {
    public abstract T execute(SQLiteDatabase db);
}
