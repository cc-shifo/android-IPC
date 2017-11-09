package com.example.democustombinder.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by Administrator on 17-11-9.
 */

public class MyUtils {

    public static String getProcessName(Context ctx, int pid) {
        ActivityManager am = (ActivityManager)ctx.getSystemService(ctx.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        if (lists == null)
            return null;
        for (ActivityManager.RunningAppProcessInfo info : lists) {
            if (info.pid == pid)
                return info.processName;
        }

        return null;
    }

}
