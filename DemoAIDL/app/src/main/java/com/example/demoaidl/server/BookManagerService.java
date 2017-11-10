package com.example.demoaidl.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.os.Process;

import com.example.demoaidl.aidl.Book;
import com.example.demoaidl.aidl.IBookManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BookManagerService extends Service {
    private static final String TAG = "BookManagerService";
    private CopyOnWriteArrayList<Book> bookList = new CopyOnWriteArrayList<>();

    public BookManagerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    // 服务端执行
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getListBook() throws RemoteException {
            Log.d(TAG, "getListBook() on pid: " + Process.myPid());
            return bookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            bookList.add(book);
        }
    };
}
