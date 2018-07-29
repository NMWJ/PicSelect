package com.rx.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {

    private boolean check;
    private String path;
    private int progress;

    public Image(boolean check, String path) {
        this.check = check;
        this.path = path;
    }

    public Image(boolean check, String path, int progress) {
        this.check = check;
        this.path = path;
        this.progress = progress;
    }

    protected Image(Parcel in) {
        check = in.readByte() != 0;
        path = in.readString();
        progress = in.readInt();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (check ? 1 : 0));
        parcel.writeString(path);
        parcel.writeInt(progress);
    }
}
