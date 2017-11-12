package com.example.demoaidlremotecalllist.server;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.os.Process;

import com.example.demoaidlremotecalllist.aidl.Book;
import com.example.demoaidlremotecalllist.aidl.IBookArrivedListener;
import com.example.demoaidlremotecalllist.aidl.IBookManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagerService extends Service {
    private static final String TAG = "DMainActivityService";
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    /**
     * 定义stub()端，记为C端
     * 调用aidl中的方法端，记为B端
     * 普通的对象不能直接在进程间传递，必须通过底层binder来进行传输，而且传输的本质是将原对象反序列化，new
     * 一个新对象，跟传入对象是不同的对象。进程的一端将new stub()对象传给
     * 另外一端的进程后，会被反序列化在接收端产生一个新对象。多次传输在接收端就会多次地产生一个新对象，但是
     * 这些新对象最底层的binder仍然是同一个binder。
     *
     * 对于注册IBookArrivedListener操作，C端传到B端后，B端收到的是Proxy1；解注册操作时B端收到的
     * 是Proxy2，与之前的Proxy1不是同一个对象，所以造成解注册失败。但Proxy1和Proxy2底层的binder是同一个。
     * */
//    private CopyOnWriteArrayList<IBookArrivedListener> mListenerList =
//            new CopyOnWriteArrayList<>();
    RemoteCallbackList<IBookArrivedListener> mListenerList = new RemoteCallbackList<>();

    // 原子类型处理并发问题
    private AtomicBoolean mIsDestroyed = new AtomicBoolean(false);

    public BookManagerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        if (checkCallingOrSelfPermission("com.example.demoaidlremotecalllist" +
                ".ACCESS_BOOK_SERVICE") != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onBind: permission denied");
            return null;
        }
        // 为什么不用ContextCompat.checkSelfPermission()
        return mBinder;
    }

    // 服务端执行 服务端线程池执行aidl接口函数getListBook, addBook等
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getListBook() throws RemoteException {
            Log.d(TAG, "getListBook() on pid: " + Process.myPid());
            Log.d(TAG, "getListBook() on thread: " + Thread.currentThread().getId());
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
//            if (!mListenerList.contains(listener)) {
//                mListenerList.add(listener);
//            } else {
//                Log.d(TAG, "Listener already exists");
//            }
            mListenerList.register(listener);
            int n = mListenerList.beginBroadcast();
            mListenerList.finishBroadcast();
            Log.d(TAG, "register Listener: Listener count=" + n);
        }

        @Override
        public void unregisterListener(IBookArrivedListener listener) throws RemoteException {
//            if (mListenerList.contains(listener)) {
//                mListenerList.remove(listener);
//            } else {
//                Log.d(TAG, "Listener not found");
//            }
            boolean success = mListenerList.unregister(listener);
            if (!success) {
                Log.d(TAG, "Listener not found");
            }
            int n = mListenerList.beginBroadcast();
            mListenerList.finishBroadcast();
            Log.d(TAG, "unregister Listener: Listener count=" + n);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply,
                                  int flags) throws RemoteException {
            Log.d(TAG, "onTransact() on pid: " + Process.myPid());
            Log.d(TAG, "onTransact() on thread: " + Thread.currentThread().getId());
            if (checkCallingOrSelfPermission("com.example.demoaidlremotecalllist" +
                    ".ACCESS_BOOK_SERVICE") != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onTransact: dennied");
                return false;
            }

//            String[] packages = getPackageManager().getPackagesForUid(getCallingPid());
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            if (packages == null || packages.length <= 0
                    || !packages[0].startsWith("com.example")) {
                Log.d(TAG, "onTransact: dennied uid");
                Log.d(TAG, "onTransact: call sent pid=" + getCallingPid());
                if (packages != null)
                    Log.d(TAG, "onTransact: packages=" + packages[0]);
                else
                    Log.d(TAG, "onTransact: packages is null");
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // 结合Stub(）中重写的getListBook()得出，aidl接口中的方法运行在非1的线程中。
        Log.d(TAG, "onCreate() on pid: " + Process.myPid());
        Log.d(TAG, "onCreate() on thread: " + Thread.currentThread().getId());
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

                    int n = mListenerList.beginBroadcast();
                    for (int i = 0; i < n; i++) {
                        /*Binder binder = (Binder)(mListenerList.get(i));
                        IBookArrivedListener listener = IBookArrivedListener.Stub
                                .asInterface(binder);*/
                        /**
                         * 跟踪发现：listener直接是IBookArrivedListener$Stub$Proxy类型，所以
                         * 不需要IBookArrivedListener.Stub.asInterface(listener)的调用。
                         * */
                        IBookArrivedListener listener = mListenerList.getBroadcastItem(i);
                        try {
                            listener.bookArrived(mBookList);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mListenerList.finishBroadcast();
                }
            }
        }).start();
    }

}
