package com.example.demoaidlremotecalllist.client;

import android.annotation.SuppressLint;
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

import com.example.demoaidlremotecalllist.R;
import com.example.demoaidlremotecalllist.aidl.Book;
import com.example.demoaidlremotecalllist.aidl.IBookArrivedListener;
import com.example.demoaidlremotecalllist.aidl.IBookManager;
import com.example.demoaidlremotecalllist.server.BookManagerService;
import com.example.demoaidlremotecalllist.utils.MyConstants;

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
        /*if (mBinder != null && mBinder.isBinderAlive())
            mBinder.unlinkToDeath(mDeathRecipient, 0);*/

        if (mRemoteBookManager != null && mBinder.isBinderAlive()) {
            mBinder.unlinkToDeath(mDeathRecipient, 0);
            try {
                mRemoteBookManager.unregisterListener(bookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(mConn);
        }
        mBinder = null;
        mRemoteBookManager = null;
        super.onDestroy();
    }

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /**
             * service 是BinderProxy类型，且还是native。记得onBinder()返回的是Binder的子类，
             * 所以service肯定不是IBinder的直接子类。
             */
            mRemoteBookManager = IBookManager.Stub.asInterface(service);
            if (mRemoteBookManager == null)
                return;
            mBinder = mRemoteBookManager.asBinder();
            try {
                mBinder.linkToDeath(mDeathRecipient, 0);
                mRemoteBookManager.registerListener(bookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onService Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onService Disconnected");
            mRemoteBookManager = null;
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            // binderDied()运行在客户端binder线程池，而非主线程中。
            Log.d(TAG, "binderDied on thread: " + Thread.currentThread().getId());
            /*if (mBinder.isBinderAlive())
                mBinder.unlinkToDeath(mDeathRecipient, 0);*/

            /**
             * unbind之后，若binder没有及时被回收mBinder.isBinderAlive()还是返回true
             * 但这个时候，若继续调mBinder.unlinkToDeath()
             * mRemoteBookManager.unregisterListener()等函数会抛出
             * RemoteException异常。这说明client与service端断开后不能简单的评isBinderAlive()
             * 来判断要不要unlinkToDeath()或者unregisterListener()或者unbindService()。
             * 应该以是否断开为标准，用一个链接引用mRemoteBookManager来判断最好。
             * 1.断开了且是Alive()才有必要调用unlinkToDeath()或者unregisterListener()或
             * 者unbindService()。
             * 2.断开了且binder不是Alive()，就没有必要再调用了。
             * 3.链接引用虽然没有置null，但binder不是Alive()了，也没必要再调用了。
             * 二和三情况都是binder不存在的情形。
             * */
            if (mRemoteBookManager != null && mBinder.isBinderAlive()) {
                mBinder.unlinkToDeath(mDeathRecipient, 0);
                try {
                    mRemoteBookManager.unregisterListener(bookArrivedListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(mConn);
                mRemoteBookManager = null;
            }

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
                    // 客户端创建数据，客户端和服务端都可查看得到。
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
        if (mRemoteBookManager != null && mBinder.isBinderAlive()) {
            mBinder.unlinkToDeath(mDeathRecipient, 0);

            try {
                mRemoteBookManager.unregisterListener(bookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(mConn);
            mRemoteBookManager = null;
        }

    }

    /**
     * Observer: listen to book arrived
     *
     * Server call this method through Binder.
     * */
    private IBookArrivedListener bookArrivedListener = new IBookArrivedListener.Stub() {
        @Override
        public void bookArrived(List<Book> newBooks) throws RemoteException {
            // 运行在client端进程。好像可以这样理解：谁定义Stub，被重写的函数就运行在哪一端。
            // 但是是运行在客户端binder线程池中。所以这个函数内不能直接进行UI操作，必须切换到UI线程才行。
            Log.d(TAG, "new book Arrived on pid: " + Process.myPid());
            Log.d(TAG, "new book Arrived on thread: " + Thread.currentThread().getId());
            mNewBookHandler.obtainMessage(MyConstants.MSG_BOOK_ARRIVED, newBooks)
                    .sendToTarget();
        }
    };

    // 注意为什么可以不用考虑这个警告?
    @SuppressLint("HandlerLeak")
    private Handler mNewBookHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyConstants.MSG_BOOK_ARRIVED:
                    Log.d(TAG, "handleMessage: 收到你发送的书到了的消息" + msg.obj);
                    Log.d(TAG, "new book Arrived on pid: " + Process.myPid());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

}
