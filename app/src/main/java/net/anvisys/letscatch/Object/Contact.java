package net.anvisys.letscatch.Object;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Amit Bansal on 04-01-2017.
 */
public class Contact implements Parcelable {
    public int ID;
    public String userName,MobileNumber, strImage, location, LatLong;
    public String Status;
    // public byte[] imgByte;


    public Contact() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ID);
        parcel.writeString(userName);
        parcel.writeString(MobileNumber);
        parcel.writeString(strImage);
        parcel.writeString(location);
        parcel.writeString(LatLong);
        parcel.writeString(Status);
    }

    private Contact(Parcel in){
        this.ID = in.readInt();
        this.userName = in.readString();
        this.MobileNumber = in.readString();
        this.strImage = in.readString();
        this.location = in.readString();
        this.LatLong = in.readString();
        this.Status = in.readString();
    }


    public static final Parcelable.Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel parcel) {
            return new Contact(parcel);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}