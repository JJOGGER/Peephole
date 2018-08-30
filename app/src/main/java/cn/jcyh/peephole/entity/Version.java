package cn.jcyh.peephole.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jogger on 2018/8/7.
 */
public class Version implements Parcelable{
    private String number;
    private int limitLevel;
    private String address;

    protected Version(Parcel in) {
        number = in.readString();
        limitLevel = in.readInt();
        address = in.readString();
    }

    public static final Creator<Version> CREATOR = new Creator<Version>() {
        @Override
        public Version createFromParcel(Parcel in) {
            return new Version(in);
        }

        @Override
        public Version[] newArray(int size) {
            return new Version[size];
        }
    };

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getLimitLevel() {
        return limitLevel;
    }

    public void setLimitLevel(int limitLevel) {
        this.limitLevel = limitLevel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Version{" +
                "number='" + number + '\'' +
                ", limitLevel=" + limitLevel +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(number);
        dest.writeInt(limitLevel);
        dest.writeString(address);
    }
}
