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

        // 来自android:android:authorities
        Uri uri = Uri.parse("content://com.example.democontentprovider.BookContentProvider");
        ContentResolver resolver = getContentResolver();
        resolver.query(uri, null, null, null, null);
        resolver.query(uri, null, null, null, null);
        resolver.query(uri, null, null, null, null);

        // 插入数据
        Uri uriBook = Uri.parse("content://com.example.democontentprovider" +
                ".BookContentProvider/book");
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



        Uri uriUser = Uri.parse("content://com.example.democontentprovider" +
                ".BookContentProvider/user");
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
