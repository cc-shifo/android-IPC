package com.example.democontentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.democontentprovider.model.Book;
import com.example.democontentprovider.model.User;

/**
 * 1.创建一个空Provider，实现CRUD函数等6个函数。
 * 2.创建一个使用Provider的活动。
 * 3.创建数据库，作为Provider的数据来源。
 * 4.实现空Provider的函数。
 * 5.在活动中使用数据库。
 * */
public class ProviderActivity extends AppCompatActivity {
    private static final String TAG = "ProviderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        /** uri中的authorities值来自android:android:authorities，其值可自定义。
         * 如果想在path处使用*号，那么ContentProvider的CRUD函数必须能解析星号，例如getTableName()必须能
         * 返回正确的表明，否则在这个带星号的uri上的CRUD操作都会抛出“不支持的uri异常”，说明uri出错了。
         */
        // Uri uri = Uri.parse("content://com.example.provider/*");
        Uri uri = Uri.parse("content://com.example.democontentprovider/book");
        ContentResolver resolver = getContentResolver();
        resolver.query(uri, null, null, null, null);
        resolver.query(uri, null, null, null, null);
        resolver.query(uri, null, null, null, null);

        // 插入数据
        Uri uriBook = Uri.parse("content://com.example.democontentprovider/book");
        ContentValues values = new ContentValues();
        values.put("_id", 6);
        values.put("name", "硅谷科技史100年");
        resolver.insert(uriBook, values);
        // 查询数据
        Cursor cursor = resolver.query(uriBook, new String[]{"_id", "name"}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int bookID = cursor.getInt(0);
                String bookName = cursor.getString(1);
                Book book = new Book(bookID, bookName);
                Log.d(TAG, "query book:" + book.toString());
            }

            cursor.close();
        }



        Uri uriUser = Uri.parse("content://com.example.democontentprovider/user");
        cursor = resolver.query(uriUser, new String[]{"_id", "name", "sex"},
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                int userId = cursor.getInt(0);
                String userName = cursor.getString(1);
                boolean isMale = cursor.getInt(2) == 1;
                User user = new User(userId, userName, isMale);
                Log.d(TAG, "query user:" + user.toString());
            }
            cursor.close();
        }


    }
}
