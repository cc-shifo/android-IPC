package com.example.demoaidlremotecalllist.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by Administrator on 17-11-10.
 */

public class MyUtils {
    public static String getProcessName(int pid, Context ctx) {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> rInfo = am.getRunningAppProcesses();
        if (rInfo == null)
            return null;

        for (ActivityManager.RunningAppProcessInfo info : rInfo) {
            if (info.pid == pid) {
                return info.processName;
            }
        }

        return null;
    }
}
