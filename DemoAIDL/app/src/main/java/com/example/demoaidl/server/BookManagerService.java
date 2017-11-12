package com.example.demoaidl.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.os.Process;

import com.example.demoaidl.aidl.Book;
import com.example.demoaidl.aidl.IBookArrivedListener;
import com.example.demoaidl.aidl.IBookManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagerService extends Service {
    private static final String TAG = "DMainActivityService";
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<IBookArrivedListener> mListenerList =
            new CopyOnWriteArrayList<>();

    // 原子类型处理并发问题
    private AtomicBoolean mIsDestroyed = new AtomicBoolean(false);

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
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IBookArrivedListener listener) throws RemoteException {
            /**
             * 跟踪发现：listener直接是IBookArrivedListener$Stub$Proxy类型，所以
             * 不需要IBookArrivedListener.Stub.asInterface(listener)的调用。
             * */
            if (!mListenerList.contains(listener)) {
                mListenerList.add(listener);
            } else {
                Log.d(TAG, "Listener already exists");
            }
        }

        @Override
        public void unregisterListener(IBookArrivedListener listener) throws RemoteException {
            if (mListenerList.contains(listener)) {
                mListenerList.remove(listener);
            } else {
                Log.d(TAG, "Listener not found");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // 服务端创建数据，客户端可查看
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "IOS"));

        // 检查是否有新书要通知客户端
        NewBookArrived();
    }

    @Override
    public void onDestroy() {
        mIsDestroyed.set(true);
        super.onDestroy();
    }

    private void NewBookArrived() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 处理当前线程跟主线程(服务线程)的同步问题
                while (!mIsDestroyed.get()) {

                    // 10秒钟处理一次
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < mListenerList.size(); i++) {
                        /*Binder binder = (Binder)(mListenerList.get(i));
                        IBookArrivedListener listener = IBookArrivedListener.Stub
                                .asInterface(binder);*/
                        /**
                         * 跟踪发现：listener直接是IBookArrivedListener$Stub$Proxy类型，所以
                         * 不需要IBookArrivedListener.Stub.asInterface(listener)的调用。
                         * */
                        IBookArrivedListener listener = mListenerList.get(i);
                        try {
                            listener.bookArrived(mBookList);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

}
