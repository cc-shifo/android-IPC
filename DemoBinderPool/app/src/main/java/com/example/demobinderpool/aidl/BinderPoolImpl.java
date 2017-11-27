package com.example.demobinderpool.aidl;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by Administrator on 17-11-27.
 */

public class BinderPoolImpl extends IBinderPool.Stub {
    public static final int BINDER_NONE = -1;
    public static final int BINDER_COMPUTE = 1;
    public static final int BINDER_SECURITY_CENTER = 2;

    @Override
    public IBinder queryBinder(int binderId) throws RemoteException {
        IBinder binder = null;

        switch (binderId) {
            case BINDER_SECURITY_CENTER:
                binder = new SecurityCenterImpl();
                break;
            case BINDER_COMPUTE:
                binder = new ComputerImpl();
                break;
            default:
                break;
        }
        return binder;
    }
}
