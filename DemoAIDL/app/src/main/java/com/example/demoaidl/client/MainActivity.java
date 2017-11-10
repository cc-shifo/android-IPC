package com.example.demoaidl.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.demoaidl.R;
import com.example.demoaidl.aidl.Book;
import com.example.demoaidl.aidl.IBookArrivedListener;
import com.example.demoaidl.aidl.IBookManager;
import com.example.demoaidl.server.BookManagerService;
import com.example.demoaidl.utils.MyConstants;
import com.example.demoaidl.utils.MyUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "DMainActivity";
    private IBookManager mRemoteBookManager;
    private IBinder mBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (mBinder != null) {
            mBinder.unlinkToDeath(mDeathRecipient, 0);
            unbindService(mConn);
        }
        mBinder = null;
        mRemoteBookManager = null;
        super.onDestroy();
    }

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IBookManager mRemoteBookManager = IBookManager.Stub.asInterface(service);
            mBinder = mRemoteBookManager.asBinder();
            try {
                mBinder.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManager = null;
            Log.d(TAG, "onService Disconnected");
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied");
            mBinder.unlinkToDeath(mDeathRecipient, 0);
            unbindService(mConn);
            mBinder = null;
            mRemoteBookManager = null;
        }
    };

    public void btnGetBooks(View v) {
        // RPC会产生阻塞，所以另外启线程执行get和add操作。
        if (mRemoteBookManager == null)
            return;
        Log.d(TAG, "get book list");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Book> bookList = mRemoteBookManager.getListBook();
                    Log.d(TAG, "query book list, list type: "
                            + bookList.getClass().getCanonicalName());
                    Log.d(TAG, "query book list: " + bookList.toString());
                } catch (RemoteException e) {
                    mRemoteBookManager = null;
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // RPC会产生阻塞，所以另外启线程执行get和add操作。
    public void btnAddBook(View v) {
        if (mRemoteBookManager == null)
            return;
        Log.d(TAG, "add book");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Book mBook = new Book(3, "Android进阶");
                    mRemoteBookManager.addBook(mBook);
                    Log.d(TAG, "add book: " + mBook);
                } catch (RemoteException e) {
                    mRemoteBookManager = null;
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void btnBind(View v) {
        Log.d(TAG, "bind btn clicked");
        if (mRemoteBookManager == null) {
            Intent intent = new Intent(this, BookManagerService.class);
            bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        }
    }

    public void btnUnbind(View v) {
        Log.d(TAG, "unbind btn clicked");
        if (mRemoteBookManager != null) {
            unbindService(mConn);
            mRemoteBookManager = null;
        }
    }

    /**
     * Observer: listener to book arrived
     *
     * Server call this method through Binder.
     * */
    private IBookArrivedListener listener = new IBookArrivedListener.Stub() {
        @Override
        public void bookArrived(Book newBook) throws RemoteException {
            Log.d(TAG, "new book Arrived on pid: " + Process.myPid());
            mNewBookHandler.obtainMessage(MyConstants.MSG_BOOK_ARRIVED, newBook)
                    .sendToTarget();
        }
    };

    private Handler mNewBookHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyConstants.MSG_BOOK_ARRIVED:
                    Log.d(TAG, "handleMessage: 新书到了" + msg.obj);
                    Log.d(TAG, "new book Arrived on pid: " + Process.myPid());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
}
