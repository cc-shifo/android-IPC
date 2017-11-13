package com.example.democontentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    // 创建matcher
    private static final String AUTHORITY = "com.example.democontentprovider.BookContentProvider";
    private static final String TABLE_BOOK = "book";
    private static final String TABLE_USER = "user";
    public static final int BOOK_URI_CODE = 0;
    public static final int USER_URI_CODE = 1;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        mUriMatcher.addURI(AUTHORITY, TABLE_BOOK, BOOK_URI_CODE);
        mUriMatcher.addURI(AUTHORITY, TABLE_USER, USER_URI_CODE);
    }

    // 获得表明
    private String getTableName(Uri uri) {
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case BOOK_URI_CODE:
                tableName = TABLE_BOOK;
                break;
            case USER_URI_CODE:
                tableName = TABLE_USER;
                break;
            default:break;
        }

        return tableName;
    }

    private Context mContext;
    private SQLiteDatabase mDb;
    private void initProviderData() {
        mDb = new DbOpenHelper(mContext).getWritableDatabase();
        mDb.execSQL("delete from " + TABLE_BOOK);
        mDb.execSQL("delete from " + TABLE_USER);
        mDb.execSQL("insert into book values(3,'Android');");
        mDb.execSQL("insert into book values(4,'Ios');");
        mDb.execSQL("insert into book values(5,'Html5');");
        mDb.execSQL("insert into user values(1,'jake',1);");
        mDb.execSQL("insert into user values(2,'jasmine',0);");
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate: on thread: " + Thread.currentThread().getName());
        Log.d(TAG, "onCreate: on thread: " + Thread.currentThread().getId());
        mContext = getContext();
        initProviderData();

        return true;
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

        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // projection是列， selection是where中的条件，如果条件中有参数或者占位符等，那
        // selectionArgs是条件中参数的对应值
        return mDb.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "update");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int row = mDb.update(table, values, selection, selectionArgs);
        if (row > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return row;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d(TAG, "insert on pid: " + Process.myPid());
        Log.d(TAG, "insert on thread" + Thread.currentThread().getName());
        Log.d(TAG, "insert on thread id" + Thread.currentThread().getId());
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        mDb.insert(table, null, values);
        // insert, update, delete等会改变数据库参数，所以当数据库改变后要发出通知。
        mContext.getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        Log.d(TAG, "delete");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int count = mDb.delete(table, selection, selectionArgs);
        if (count > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
