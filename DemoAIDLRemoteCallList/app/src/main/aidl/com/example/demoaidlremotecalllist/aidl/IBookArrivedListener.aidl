// IBookArrivedListener.aidl
package com.example.demoaidlremotecalllist.aidl;

// Declare any non-default types here with import statements

import com.example.demoaidlremotecalllist.aidl.Book;
interface IBookArrivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void bookArrived(in List<Book> newBooks);
}
