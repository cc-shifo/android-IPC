package com.example.democustombinder;

import android.os.IInterface;
import android.os.RemoteException;

import java.util.List;

/**
 * Created by Administrator on 17-11-9.
 */

public interface IBookManager extends IInterface {

    public List<Book> getBookList() throws RemoteException;
    public void addBook(Book book) throws RemoteException;
}
