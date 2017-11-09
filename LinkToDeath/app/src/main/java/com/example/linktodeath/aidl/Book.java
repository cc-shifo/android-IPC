package com.example.linktodeath.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 17-11-9.
 * 先在aidl包下建立两个aidl文件(右键选简历AIDL文件，非建立file)，建成功后，studio自动
 * 创建一个与java目录同级的aidl目录。
 * 然后再建立Book.java
 * 最后IBookManager.aidl引入Book
 */

public class Book implements Parcelable {
    public int bookId;
    public String bookName;

    public Book(int bookId, String bookName) {
        this.bookId = bookId;
        this.bookName = bookName;
    }

    // 反序列化
    private Book(Parcel in) {
        this.bookId = in.readInt();
        this.bookName = in.readString();
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

    public final static Creator<com.example.linktodeath.aidl.Book> CREATOR =
            new Creator<com.example.linktodeath.aidl.Book>() {
        @Override
        public com.example.linktodeath.aidl.Book createFromParcel(Parcel source) {
            return new com.example.linktodeath.aidl.Book(source);
        }

        @Override
        public com.example.linktodeath.aidl.Book[] newArray(int size) {
            return new com.example.linktodeath.aidl.Book[size];
        }
    };
}