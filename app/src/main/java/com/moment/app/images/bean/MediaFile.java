package com.moment.app.images.bean;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

@Keep
public class MediaFile implements Parcelable {
    public long fileId;
    public String path;

    public String lowPath;

    public int width;
    public int height;


    //视频
    public String thumbnail;
    public long duration;

    //公共属性
    public long size;
    public String mimeType;
    public long date;


    public boolean isVideo() {
        return !TextUtils.isEmpty(mimeType) && mimeType.contains("video");
    }

    public boolean isGif() {
        return !TextUtils.isEmpty(mimeType) && mimeType.contains("gif");
    }

    public String displayPath(){
        if (Build.VERSION.SDK_INT >= 29){ //>= android q
            return path;
        }
        return lowPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.fileId);
        dest.writeString(this.path);
        dest.writeString(this.lowPath);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.thumbnail);
        dest.writeLong(this.duration);
        dest.writeLong(this.size);
        dest.writeString(this.mimeType);
        dest.writeLong(this.date);
    }

    public MediaFile() {
    }

    protected MediaFile(Parcel in) {
        this.fileId = in.readLong();
        this.path = in.readString();
        this.lowPath = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.thumbnail = in.readString();
        this.duration = in.readLong();
        this.size = in.readLong();
        this.mimeType = in.readString();
        this.date = in.readLong();
    }

    public static final Creator<MediaFile> CREATOR = new Creator<MediaFile>() {
        @Override
        public MediaFile createFromParcel(Parcel source) {
            return new MediaFile(source);
        }

        @Override
        public MediaFile[] newArray(int size) {
            return new MediaFile[size];
        }
    };

    public long getDate() {
        return date;
    }


//    @Override
//    public int compare(MediaFile o1, MediaFile o2) {
//        if (o1.getDate() > o2.getDate()) {    //greater
//            return -1;
//        } else if (o1.getDate() == o2.getDate()) {    //equals
//            return 0;
//        } else {    //less
//            return 1;
//        }
//    }


    @Override
    public String toString() {
        return "MediaFile{" +
                "fileId=" + fileId +
                ", path='" + path + '\'' +
                ", lowPath='" + lowPath + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", thumbnail='" + thumbnail + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", mimeType='" + mimeType + '\'' +
                ", date=" + date +
                '}';
    }
}
