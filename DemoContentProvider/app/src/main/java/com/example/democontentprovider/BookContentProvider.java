package com.example.democontentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Administrator on 17-11-13.
 */

public class BookContentProvider extends ContentProvider {
    private static final String TAG = "BookContentProvider";

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate: on thread: " + Thread.currentThread().getName());
        Log.d(TAG, "onCreate: on thread: " + Thread.currentThread().getId());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        // C运行在Provider端进程的Binder线程池
        Log.d(TAG, "query on pid: " + Process.myPid());
        Log.d(TAG, "query on thread" + Thread.currentThread().getName());
        Log.d(TAG, "query on thread id" + Thread.currentThread().getId());
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
