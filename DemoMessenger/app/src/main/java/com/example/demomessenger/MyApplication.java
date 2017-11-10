package com.example.demomessenger;

import android.app.Application;
import android.os.Process;
import android.util.Log;

import com.example.demomessenger.utils.MyUtils;

/**
 * Created by Administrator on 17-11-10.
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        String name = MyUtils.getProcessName(Process.myPid(), getApplicationContext());
        Log.d(TAG, "my application start, process name: " + name);
    }
}
