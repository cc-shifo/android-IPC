package com.example.democustombinder;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;

import java.util.List;

/**
 * Created by Administrator on 17-11-9.
 */

public class BookManagerImpl extends Binder implements IBookManager {
    static final String DESCRIPTOR = "com.example.democustombinder.IBookManager";

    static final int TRANSACTION_getBookList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_addBook = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);

    public BookManagerImpl() {
        this.attachInterface(this, DESCRIPTOR);
    }

    public static IBookManager asInterface(IBinder obj) {
        if ((obj == null)) {
            return null;
        }
        android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
        if (((iin != null) && (iin instanceof IBookManager))) {
            return ((IBookManager) iin);
        }
        return new BookManagerImpl.Proxy(obj);
    }

    @Override
    public IBinder asBinder() {
        return this;
    }

    @Override
    public void addBook(Book book) throws RemoteException {
        // TODO 待实现
    }

    @Override
    public List<Book> getBookList() throws RemoteException {
        // TODO 待实现
        return null;
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        switch (code) {
            case INTERFACE_TRANSACTION:
            {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            case TRANSACTION_getBookList:
            {
                data.enforceInterface(DESCRIPTOR);
                // 执行我们在new IBookManager中实现的方法 getBookList()
                java.util.List<Book> _result = this.getBookList();
                reply.writeNoException();
                reply.writeTypedList(_result);
                return true;
            }
            case TRANSACTION_addBook:
            {
                data.enforceInterface(DESCRIPTOR);
                Book _arg0;
                if ((0!=data.readInt())) {
                    _arg0 = Book.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                // 执行我们在new IBookManager中实现的方法 addBook()
                this.addBook(_arg0);
                reply.writeNoException();
                return true;
            }
        }

        return super.onTransact(code, data, reply, flags);
    }

    private static class Proxy implements IBookManager {
        private IBinder mRemote;

        Proxy(IBinder remote) {
            mRemote = remote;
        }

        @Override
        public IBinder asBinder() {
            return mRemote;
        }

        public String getInterfaceDescriptor() {
            return DESCRIPTOR;
        }

        // 客户端(activity)调用的getBookList就是这个函数。运行在客户端。
        @Override
        public List<Book> getBookList() throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            java.util.List<Book> _result;
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                // 执行远程调用(RPC)，然后当前线程挂起, 等待RPC返回执行结果。mRemote.transact经过底层
                // 调用转换成调用BookManagerImpl的onTransact()函数。
                mRemote.transact(BookManagerImpl.TRANSACTION_getBookList, _data, _reply, 0);
                _reply.readException();
                _result = _reply.createTypedArrayList(Book.CREATOR);
            } finally {
                _reply.recycle();
                _data.recycle();
            }
            return _result;
        }

        // 客户端(activity)将要调用的函数
        @Override
        public void addBook(Book book) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                if ((book != null)) {
                    data.writeInt(1);
                    book.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                // 执行远程调用(RPC)，然后当前线程挂起, 等待RPC返回执行结果。mRemote.transact经过底层
                // 调用转换成调用BookManagerImpl的onTransact()函数。
                mRemote.transact(TRANSACTION_addBook, data, reply, 0);
                reply.readException();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
    }
}
