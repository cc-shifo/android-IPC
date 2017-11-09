package com.example.democustombinder;

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
import android.widget.Button;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DMainActivity";

    @BindView(R.id.btn_bind)
    Button mBtnBind;
    @BindView(R.id.btn_unbind)
    Button mBtnUnbind;

    Boolean isBinded = false;

    private ServiceConnection mConn= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d(TAG, "IBinder type: " + service.getClass().getCanonicalName());
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
            IBookManager bookManager = BookManagerImpl.asInterface(service);
            try {
                List<Book> bookList = bookManager.getBookList();
                Log.d(TAG, "query book list, list type: "
                        + bookList.getClass().getCanonicalName());
                Log.d(TAG, "query book list: " + bookList.toString());

                Book mBook = new Book(3, "Android进阶");
                bookManager.addBook(mBook);
                Log.d(TAG, "add book: " + mBook);

                bookList = bookManager.getBookList();
                Log.d(TAG, "query book list, list type: "
                        + bookList.getClass().getCanonicalName());
                Log.d(TAG, "query book list: " + bookList.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // unbind操作后，onServiceDisconnected并没有及时调用。
        // 好像是说当挂在着service的进程挂掉等典型情况，回调才被调用。估计调用stopService()后可以被调用到。
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.btn_bind)
    void BindBtnClicked(View v) {
        Log.d(TAG, "bind btn clicked");
        Intent intent = new Intent(MainActivity.this, BookService.class);
        if (!isBinded && bindService(intent, mConn, Context.BIND_AUTO_CREATE)) {
            isBinded = true;
        }
    }

    @OnClick(R.id.btn_unbind)
    void BindBtnUnbind(View v) {
        Log.d(TAG, "unbind btn clicked");
        if (isBinded) {
            unbindService(mConn);
            isBinded = false;
        }
    }

    @Override
    protected void onDestroy() {
        if (isBinded)
            unbindService(mConn);
        super.onDestroy();
    }
}
