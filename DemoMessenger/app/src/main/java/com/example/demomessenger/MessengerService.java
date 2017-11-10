package com.example.demomessenger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.example.demomessenger.utils.MyConstants;

public class MessengerService extends Service {
    private static final String TAG = "DMainActivity ser";

    public MessengerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() on thread id: " + Thread.currentThread().getId());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG, "onBind() on thread id: " + Thread.currentThread().getId());
        Log.d(TAG, "onBind() on pid: " + Process.myPid());
        return mMessenger.getBinder();
    }

    /**
     * 定义一个handler来处理消息。请注意是final类型——不需要被继承的功能；
     * static类型——外部类直接调用内部这个类来构造handler。谁定义了handler，handler就跟哪
     * 个线程绑定，handler的函数就在这个线程里执行。
     */
    private static final class MsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyConstants.MSG_FROM_CLIENT:
                    Log.d(TAG, "received msg from Client: " + msg.getData().getString("msg"));
                    Messenger client = msg.replyTo;
                    Message replyMsgs = Message.obtain(null, MyConstants.MSG_FROM_SERVICE);
                    Bundle bundle = new Bundle();
                    bundle.putString("reply", "嗯，你的消息我已经收到，稍后回复你");
                    replyMsgs.setData(bundle);
                    try {
                        client.send(replyMsgs);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    // 定义一个Messenger来充当Message的传输通道
    private final Messenger mMessenger = new Messenger(new MsgHandler());
}
