package com.moment.app.ui;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class NaviTab implements Parcelable {
    @DrawableRes
    public int tab_icon_Id;
    @StringRes
    public int tab_name_Id;

    public boolean hide_text = true;

    public boolean no_page;

    public int real_page_index;

    public String page_name;

    //用来处理将来可能会有的动态tab
    public String tab_icon_url;
    public String tab_name_text;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.tab_icon_Id);
        dest.writeInt(this.tab_name_Id);
        dest.writeByte(this.hide_text ? (byte) 1 : (byte) 0);
        dest.writeByte(this.no_page ? (byte) 1 : (byte) 0);
        dest.writeInt(this.real_page_index);
        dest.writeString(this.page_name);
        dest.writeString(this.tab_icon_url);
        dest.writeString(this.tab_name_text);
    }

    public void readFromParcel(Parcel source) {
        this.tab_icon_Id = source.readInt();
        this.tab_name_Id = source.readInt();
        this.hide_text = source.readByte() != 0;
        this.no_page = source.readByte() != 0;
        this.real_page_index = source.readInt();
        this.page_name = source.readString();
        this.tab_icon_url = source.readString();
        this.tab_name_text = source.readString();
    }

    public NaviTab() {
    }

    protected NaviTab(Parcel in) {
        this.tab_icon_Id = in.readInt();
        this.tab_name_Id = in.readInt();
        this.hide_text = in.readByte() != 0;
        this.no_page = in.readByte() != 0;
        this.real_page_index = in.readInt();
        this.page_name = in.readString();
        this.tab_icon_url = in.readString();
        this.tab_name_text = in.readString();
    }

    public static final Creator<NaviTab> CREATOR = new Creator<NaviTab>() {
        @Override
        public NaviTab createFromParcel(Parcel source) {
            return new NaviTab(source);
        }

        @Override
        public NaviTab[] newArray(int size) {
            return new NaviTab[size];
        }
    };
}
