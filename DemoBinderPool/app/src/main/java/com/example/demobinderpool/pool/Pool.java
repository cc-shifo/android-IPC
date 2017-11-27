package com.example.demobinderpool.pool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.demobinderpool.aidl.IBinderPool;
import com.example.demobinderpool.server.BinderPoolService;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 17-11-27.
 * pool绑定一个service，绑定成功后serviceConnection返回对应的binder。通过这个binder中queryBinder方法，
 * 我们可以获得其他一些binder。也就是说pool是client与service的中间介，client通过pool去访问service。
 *
 * note:pool可能被多个客户端调用，因此要注意并发问题。
 */

public class Pool {
    private static final String TAG = "Pool";
    // 重新绑定服务时需要用到之前从client传进来的context
    private Context mContext;
    // do not place android context classes in static fields
    @SuppressWarnings("all")
    private static volatile Pool sInstance;
    private IBinderPool mBinderPool;
    // 减计数锁
    private CountDownLatch mBinderPoolConnLatch;

    /**
     * 如果在Pool(Context context)中调用mContext = context.getApplicationContext()
     * 会出现"do not place android context classes in static fields"这样的警告，并且会造成内存泄露，
     * 因为static区域的内存不会被GC回收，造成mContext所引用的内存也不能回收。我们直接以参数的形式向Pool
     * 构造函数传context.getApplicationContext()，不做保存吧。但是不做保存重新绑定时又没有context可以用，
     * 所以application的context必须长期持有，把警告屏蔽了。
     * */
    // do not place android context classes in static fields
    private Pool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    private synchronized void connectBinderPoolService() {
        mBinderPoolConnLatch = new CountDownLatch(1);
        Intent serviceIntent = new Intent(mContext, BinderPoolService.class);
        mContext.bindService(serviceIntent, mBinderPoolConn, Context.BIND_AUTO_CREATE);
        try {
            // 等待计数为0。binder服务连接成功时会调用mBinderPoolConnLatch.countDown()来唤醒这里的等待。
            mBinderPoolConnLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mBinderPoolConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // service是ProxyBinder不是Binder，我们将ProxyBinder转换成AIDL.Stub.Proxy
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // 唤醒等待着的减计数锁
            mBinderPoolConnLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binder Died");
            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
            mBinderPool = null;
            connectBinderPoolService();
        }
    };

    public static Pool getsInstance(Context context) {
        if (sInstance == null && context != null) {
            synchronized (Pool.class) {
                if (sInstance == null) {
                    sInstance = new Pool(context);
                }
            }
        }

        return sInstance;
    }

    public IBinder queryBinder(int binderId) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

}
