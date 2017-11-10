// IBookArrivedListener.aidl
package com.example.demoaidl.aidl;

// Declare any non-default types here with import statements
import com.example.demoaidl.aidl.Book;
interface IBookArrivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void bookArrived(in Book newBook);
}
