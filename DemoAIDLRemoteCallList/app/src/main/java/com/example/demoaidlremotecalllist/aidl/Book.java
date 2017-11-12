package com.example.demoaidlremotecalllist.aidl;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 17-11-11.
 */

public class Book implements Parcelable {
    private int bookId;
    private String bookName;

    // 必须的
    public Book() {

    }

    // 必须的
    public Book(int bookId, String bookName) {
        this.bookId = bookId;
        this.bookName = bookName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(bookId);
        dest.writeString(bookName);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    private Book(Parcel in) {
        this.bookId = in.readInt();
        this.bookName = in.readString();
    }

    @Override
    public String toString() {
        return String.format("[bookId:%s, bookName:%s]", bookId, bookName);
    }
}
