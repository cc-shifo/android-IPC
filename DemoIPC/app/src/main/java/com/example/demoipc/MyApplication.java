package com.example.demoipc;

import android.app.Application;
import android.os.Process;
import android.util.Log;

import com.example.demoipc.utils.MyUtils;

/**
 * Created by Administrator on 17-11-7.
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = MyUtils.getProcessName(getApplicationContext(), Process.myPid());
        Log.d(TAG, "my application start, process name: " + processName);
    }
}
