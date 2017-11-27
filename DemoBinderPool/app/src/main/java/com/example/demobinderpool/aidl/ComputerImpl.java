package com.example.demobinderpool.aidl;

import android.os.RemoteException;

/**
 * Created by Administrator on 17-11-27.
 */

public class ComputerImpl extends IComputer.Stub {
    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }
}
