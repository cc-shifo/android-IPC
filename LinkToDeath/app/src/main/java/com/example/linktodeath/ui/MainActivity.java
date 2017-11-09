package com.example.linktodeath.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.linktodeath.R;
import com.example.linktodeath.aidl.Book;
import com.example.linktodeath.aidl.IBookManager;
import com.example.linktodeath.service.BookService;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "DMainActivity";
    private IBookManager mBookManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    // 要防止多次创建活动，mConn多次被创建。
    private ServiceConnection mConn= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "IBinder type: "
                    + service.getClass().getCanonicalName());
            // 关键步骤。
            // 跨进程时，返回的一定是Proxy对象。因为想进行跨进程通信，
            // 必须要经过底层进行中转，而这个中转就是由mRemote.transact来完成的。那么，
            // 查看一下会执行mRemote.transact调用的情形，我们发现，在Proxy类中的重写
            // IBookManager接口的那两个方法会调用mRemote.transact，这么一来mRemote只能是
            // Proxy对象。
            // 所以，跨进程的情况下，asInterface必须返回的是Proxy对象。
            // 没跨进程时，只要是同一个进行下的IBookManager接口对象，客户端能访问到。
            // 客户端想访问服务端的方法不需要经过底层的中转，也就没必要重写getBookList()和addBook()
            // 然重写的内容调用mRemote.transact()，所以asInterface简单的返回就是同一个进行下的
            // IBookManager接口对象就行，我们客户端拿着对象就可以调用里面的方法。

            // asInterface返回的是谁？mRemote又是谁的对象？onTransact()中的this是谁
            // 跨进程，asInterface返回Proxy对象，mRemote就是BookManagerImpl对象，this就是他，
            // 他俩是从onBind中返回的对象的引用。
            // 非跨进程，asInterface返回就是BookManagerImpl对象，不经过BookManagerImpl的transact()。
            mBookManager = IBookManager.Stub.asInterface(service);
            try {
                mBookManager.asBinder().linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // unbind操作后，onServiceDisconnected并没有及时调用。
        // 好像是说当挂在着service的进程挂掉等典型情况，回调才被调用。估计调用stopService()后可以被调用到。
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBookManager = null;
            Log.d(TAG, "disconnected");
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binder Died. thread name: " + Thread.currentThread().getName());
            if (mBookManager == null)
                return;
            unbindService(mConn);
            mBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBookManager = null;
        }
    };

    public void btnBind(View v) {
        Log.d(TAG, "bind btn clicked");
        if (mBookManager == null) {
            Intent intent = new Intent(this, BookService.class);
            bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        }
    }

    public void btnUnbind(View v) {
        Log.d(TAG, "unbind btn clicked");
        if (mBookManager != null) {
            unbindService(mConn);
            mBookManager = null;
        }
    }

    // RPC会产生阻塞，所以另外启线程执行get和add操作。
    public void btnGetBooks(View v) {
        if (mBookManager == null)
            return;
        Log.d(TAG, "get book list");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Book> bookList = mBookManager.getBookList();
                    Log.d(TAG, "query book list, list type: "
                            + bookList.getClass().getCanonicalName());
                    Log.d(TAG, "query book list: " + bookList.toString());
                } catch (RemoteException e) {
                    mBookManager = null;
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // RPC会产生阻塞，所以另外启线程执行get和add操作。
    public void btnAddBook(View v) {
        if (mBookManager == null)
            return;
        Log.d(TAG, "add book");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Book mBook = new Book(3, "Android进阶");
                    mBookManager.addBook(mBook);
                    Log.d(TAG, "add book: " + mBook);
                } catch (RemoteException e) {
                    mBookManager = null;
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (mBookManager != null) {
            mBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBookManager = null;
        }
        unbindService(mConn);
        super.onDestroy();
    }
}
