// IBookManager.aidl
package com.example.demoaidl.aidl;

// Declare any non-default types here with import statements

import com.example.demoaidl.aidl.Book;
import com.example.demoaidl.aidl.IBookArrivedListener;
interface IBookManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    List<Book> getListBook();
    void addBook(in Book book);

    void registerListener(IBookArrivedListener listener);
    void unregisterListener(IBookArrivedListener listener);
}
