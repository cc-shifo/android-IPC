package com.example.demoipc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 17-11-8.
 */

public class BookService extends Service {
    private static final String TAG = "BookService";

    // 并发读写List
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    // Service用法：创建一个Binder。在onBind中返回时会用到这个Binder。
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "IOS"));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Activity(即client端)绑定服务，服务连接成功后，mBinder作为参数传给onServiceConnected()
        Log.d(TAG, "mBinder type: "
                + mBinder.getClass().getCanonicalName());
        return mBinder;
    }
}
