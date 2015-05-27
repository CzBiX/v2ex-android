package com.czbix.v2ex.util;

import android.util.Log;

import com.czbix.v2ex.BuildConfig;

public class LogUtils {
    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.v(tag, msg);
    }

    public static void v(String tag, String msg, String... args) {
        if (BuildConfig.DEBUG) Log.v(tag, String.format(msg, args));
    }

    public static void v(String tag, String msg, Throwable tr, String... args) {
        if (BuildConfig.DEBUG) Log.v(tag, String.format(msg, args), tr);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) Log.v(tag, msg, tr);
    }

    public static void v(Class<?> cls, String msg) {
        if (BuildConfig.DEBUG) Log.v(cls.getSimpleName(), msg);
    }

    public static void v(Class<?> cls, String msg, String... args) {
        if (BuildConfig.DEBUG) Log.v(cls.getSimpleName(), String.format(msg, args));
    }

    public static void v(Class<?> cls, String msg, Throwable tr, String... args) {
        if (BuildConfig.DEBUG) Log.v(cls.getSimpleName(), String.format(msg, args), tr);
    }

    public static void v(Class<?> cls, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) Log.v(cls.getSimpleName(), msg ,tr);
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.d(tag, msg);
    }

    public static void d(String tag, String msg, String... args) {
        if (BuildConfig.DEBUG) Log.d(tag, String.format(msg, args));
    }

    public static void d(String tag, String msg, Throwable tr, String... args) {
        if (BuildConfig.DEBUG) Log.d(tag, String.format(msg, args), tr);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) Log.d(tag, msg, tr);
    }

    public static void d(Class<?> cls, String msg) {
        if (BuildConfig.DEBUG) Log.d(cls.getSimpleName(), msg);
    }

    public static void d(Class<?> cls, String msg, String... args) {
        if (BuildConfig.DEBUG) Log.d(cls.getSimpleName(), String.format(msg, args));
    }

    public static void d(Class<?> cls, String msg, Throwable tr, String... args) {
        if (BuildConfig.DEBUG) Log.d(cls.getSimpleName(), String.format(msg, args), tr);
    }

    public static void d(Class<?> cls, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) Log.d(cls.getSimpleName(), msg ,tr);
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.i(tag, msg);
    }

    public static void i(String tag, String msg, String... args) {
        if (BuildConfig.DEBUG) Log.i(tag, String.format(msg, args));
    }

    public static void i(String tag, String msg, Throwable tr, String... args) {
        if (BuildConfig.DEBUG) Log.i(tag, String.format(msg, args), tr);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) Log.i(tag, msg, tr);
    }

    public static void i(Class<?> cls, String msg) {
        if (BuildConfig.DEBUG) Log.i(cls.getSimpleName(), msg);
    }

    public static void i(Class<?> cls, String msg, String... args) {
        if (BuildConfig.DEBUG) Log.i(cls.getSimpleName(), String.format(msg, args));
    }

    public static void i(Class<?> cls, String msg, Throwable tr, String... args) {
        if (BuildConfig.DEBUG) Log.i(cls.getSimpleName(), String.format(msg, args), tr);
    }

    public static void i(Class<?> cls, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) Log.i(cls.getSimpleName(), msg ,tr);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(String tag, String msg, String... args) {
        Log.w(tag, String.format(msg, args));
    }

    public static void w(String tag, String msg, Throwable tr, String... args) {
        Log.w(tag, String.format(msg, args), tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }

    public static void w(Class<?> cls, String msg) {
        Log.w(cls.getSimpleName(), msg);
    }

    public static void w(Class<?> cls, String msg, String... args) {
        Log.w(cls.getSimpleName(), String.format(msg, args));
    }

    public static void w(Class<?> cls, String msg, Throwable tr, String... args) {
        Log.w(cls.getSimpleName(), String.format(msg, args), tr);
    }

    public static void w(Class<?> cls, String msg, Throwable tr) {
        Log.w(cls.getSimpleName(), msg ,tr);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, String... args) {
        Log.e(tag, String.format(msg, args));
    }

    public static void e(String tag, String msg, Throwable tr, String... args) {
        Log.e(tag, String.format(msg, args), tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    public static void e(Class<?> cls, String msg) {
        Log.e(cls.getSimpleName(), msg);
    }

    public static void e(Class<?> cls, String msg, String... args) {
        Log.e(cls.getSimpleName(), String.format(msg, args));
    }

    public static void e(Class<?> cls, String msg, Throwable tr, String... args) {
        Log.e(cls.getSimpleName(), String.format(msg, args), tr);
    }

    public static void e(Class<?> cls, String msg, Throwable tr) {
        Log.e(cls.getSimpleName(), msg ,tr);
    }
}
