package com.example.demoipc;

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
    @BindView(R.id.btn_click)
    Button mBtnClk;

    IBookManager globalBookManager;
    Boolean isBinded = false;

    private ServiceConnection mConn= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d(TAG, "IBinder type: "
                    + service.getClass().getCanonicalName());

            // 关键步骤
            IBookManager bookManager = IBookManager.Stub.asInterface(service);
            globalBookManager = bookManager;
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
//        mBtnBind.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
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

        // bind后，调用TestUnBindedBinderCall加入一本书，unbind，再查看。
        // binder链接好像是持久有效的。通过unbind操作后，继续往list里面加入书，还是可以查看加入成功。
        try {
            Log.d(TAG, "[unbinded just now]" );
            List<Book> bookList = globalBookManager.getBookList();
            Log.d(TAG, "query book list, list type: "
                    + bookList.getClass().getCanonicalName());
            Log.d(TAG, "query book list: " + bookList.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // 扮演多进程
    @OnClick(R.id.btn_activity3)
    void startActivity3(View v) {
        Intent intent = new Intent(this, EmptyActivity.class);
        startActivity(intent);
    }

    // unbind操作后，看还能不能调用。结果发现还是可以。
    @OnClick(R.id.btn_click)
    void TestUnBindedBinderCall(View v) {
        try {
            Log.d(TAG, "[after do unbind option]" );

            Book mBook = new Book(4, "天书");
            globalBookManager.addBook(mBook);
            Log.d(TAG, "add book: " + mBook);

            List<Book> bookList = globalBookManager.getBookList();
            Log.d(TAG, "query book list, list type: "
                    + bookList.getClass().getCanonicalName());
            Log.d(TAG, "query book list: " + bookList.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (isBinded)
            unbindService(mConn);
        super.onDestroy();
    }
}
