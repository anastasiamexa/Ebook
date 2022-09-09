package com.example.ebook;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    // Book attributes
    String  Author, Cover, Description, Title;
    int Pages, Quantity;
    float Price;

    // Constructor for parcelable
    protected Book(Parcel in) {
        Author = in.readString();
        Cover = in.readString();
        Description = in.readString();
        Title = in.readString();
        Pages = in.readInt();
        Quantity = in.readInt();
        Price = in.readFloat();
    }

    // Empty constructor
    protected Book(){}

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    // Getters
    public String getTitle() {
        return Title;
    }

    public String getAuthor() {
        return Author;
    }

    public String getDescription() {
        return Description;
    }

    public int getPages() {
        return Pages;
    }

    public float getPrice() {
        return  Price;
    }

    public int getQuantity() {
        return  Quantity;
    }

    public String getCover() { return Cover; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Author);
        dest.writeString(Cover);
        dest.writeString(Description);
        dest.writeString(Title);
        dest.writeInt(Pages);
        dest.writeInt(Quantity);
        dest.writeFloat(Price);
    }
}
