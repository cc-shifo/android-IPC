package com.example.demoipc.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by Administrator on 17-11-8.
 */

public class MyUtils {
    public static String getProcessName(Context ctx, int pid) {
        ActivityManager am = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApp = am.getRunningAppProcesses();
        if (runningApp == null) {
            return null;
        }

        for (ActivityManager.RunningAppProcessInfo info : runningApp) {
            if (info.pid == pid) {
                return info.processName;
            }
        }

        return null;
    }
}
