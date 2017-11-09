package com.example.linktodeath.main.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.linktodeath.Book;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 17-11-9.
 */

public class BookService extends Service {
    // 并发读写List
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();

        // 样例数据
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "IOS"));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
