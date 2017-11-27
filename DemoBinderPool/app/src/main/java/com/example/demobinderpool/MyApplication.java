package com.example.demobinderpool;

import android.app.Application;
import android.os.Process;
import android.util.Log;

import com.example.demobinderpool.pool.Pool;
import com.example.demobinderpool.utils.MyUtils;

/**
 * Created by Administrator on 17-11-27.
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        String name = MyUtils.getProcessName(Process.myPid(), getApplicationContext());
        Log.d(TAG, "my application start, process name: " + name);

        new Thread(new Runnable() {

            @Override
            public void run() {
                Pool.getsInstance(getApplicationContext());
            }
        }).start();
    }


}
