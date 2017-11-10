package com.example.demomessenger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.demomessenger.utils.MyConstants;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "DMainActivity";
    private Messenger mServerMessenger;
    private IBinder mIBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, MessengerService.class);
        Log.d(TAG, "onCreate() on thread id: " + Thread.currentThread().getId());
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConn);
        mIBinder = null;
        mServerMessenger = null;
        super.onDestroy();
    }

    // 测试结果onServiceConnected运行在客户端的主线程中，因此要注意RPC调用造成的阻塞。下面这个实现没有考虑。
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() on thread id: " + Thread.currentThread().getId());
            Log.d(TAG, "onServiceConnected() on pid: " + Process.myPid());
            // IMessenger.Stub.asInterface(target)，返回Proxy, Proxy实现了IMessenger
            mServerMessenger = new Messenger(service);
            mIBinder = mServerMessenger.getBinder();
            try {
                mIBinder.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIBinder = null;
            mServerMessenger =null;
            Log.d(TAG, "onService Disconnected");
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binder Died. thread name: " + Thread.currentThread().getName());
            if (mIBinder == null) {
                return;
            }
            mIBinder.unlinkToDeath(mDeathRecipient, 0);
            unbindService(mConn);
            mIBinder = null;
            mServerMessenger = null;
        }
    };

    private static final class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyConstants.MSG_FROM_SERVICE:
                    /**
                     * Log.d(TAG, "received msg from server: " + msg.getData().getString("msg"));
                     * 获取msg的话，内容为空
                     * */
                    Log.d(TAG, "received msg from server: " + msg.getData().getString("reply"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private Messenger mClientMessenger = new Messenger(new MyHandler());


    public void btnBind(View v) {
        Log.d(TAG, "bind btn clicked");
        if (mIBinder == null) {
            Intent intent = new Intent(this, MessengerService.class);
            bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        }
    }

    public void btnUnbind(View v) {
        Log.d(TAG, "unbind btn clicked");
        if (mIBinder != null) {
            unbindService(mConn);
            mIBinder = null;
        }
    }

    public void btnClientSendMsg(View V) {
        Bundle data = new Bundle();
        data.putString("msg", "Hello, this is client.");
        Message msg = Message.obtain(null, MyConstants.MSG_FROM_CLIENT);
        msg.setData(data);
        // Activity告诉server端：server，等下你要是回复消息，就从这个通道发过来。
        msg.replyTo = mClientMessenger;
        try {
            mServerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
