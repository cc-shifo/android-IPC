package com.example.demobinderpool.client;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.demobinderpool.R;
import com.example.demobinderpool.aidl.BinderPoolImpl;
import com.example.demobinderpool.aidl.IComputer;
import com.example.demobinderpool.aidl.ISecurityCenter;
import com.example.demobinderpool.pool.Pool;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ISecurityCenter mSecurityCenter;
    private IComputer mComputer;
    private Handler mHandler;
    private TextView textViewEncrypt;
    private TextView textViewDecrypt;
    private TextView textViewSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewEncrypt = (TextView)findViewById(R.id.tv_encrypt);
        textViewDecrypt = (TextView)findViewById(R.id.tv_decrypt);
        textViewSum = (TextView)findViewById(R.id.tv_sum);

        mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }).start();
    }

    private void doWork() {
        Pool pool = Pool.getsInstance(MainActivity.this);

        Log.d(TAG, "visit ISecurityCenter");
        // pool.queryBinder返回的到底是什么？是BinderProxy？还是直接的Binder？还是
        // 是ISecurityCenter.Stub.Proxy
        // 单步结果：返回的是BinderProxy
        IBinder securityProxyBinder = pool.queryBinder(BinderPoolImpl.BINDER_SECURITY_CENTER);
        String msg = "helloworld-安卓";
        System.out.println("content:" + msg);
        // 调用SecurityCenterImpl中实现的方法
        try {
            /**
             * java.lang.ClassCastException: android.os.BinderProxy cannot be cast
             * to com.example.demobinderpool.aidl.ISecurityCenter
             * */
            // mSecurityCenter = (ISecurityCenter)securityProxyBinder;
            mSecurityCenter = ISecurityCenter.Stub.asInterface(securityProxyBinder);
            final String password = mSecurityCenter.encrypt(msg);
            System.out.println(TAG + " encrypt:" + password);
            final String deString = mSecurityCenter.decrypt(password);
            System.out.println(TAG + " decrypt:" + deString);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    textViewEncrypt.setText(password);
                    textViewDecrypt.setText(deString);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "visit IComputer");
        // pool.queryBinder返回的到底是什么？是BinderProxy？还是直接的Binder？还是
        // 是BinderPoolImpl.Stub.Proxy
        IBinder computeProxyBinder = pool.queryBinder(BinderPoolImpl.BINDER_COMPUTE);
        // 调用ComputerImp中实现的方法
        try {
            // mComputer = (IComputer) computeProxyBinder;
            mComputer = IComputer.Stub.asInterface(computeProxyBinder);
            final int sum = mComputer.add(3, 5);
            System.out.println(TAG + " 3+5=" + sum);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // textViewSum.setText(sum);会导致进程跑飞了
                    textViewSum.setText(String.valueOf(sum));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
