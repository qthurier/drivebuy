package com.drivebuy.harryapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by delavegas on 07/05/16.
 */
public class HarryAppTask implements Parcelable {
    LatLng deliveryLocation;
    LatLng pickupLocation;
    String from;
    String to;
    String sender;

    public HarryAppTask(LatLng pickupLocation, LatLng deliveryLocation, String from, String to, String sender){
        this.deliveryLocation = deliveryLocation;
        this.pickupLocation = pickupLocation;
        this.from = from;
        this.to = to;
        this.sender = sender;
    }

    public String asJson(){
        return "{sender: \"" + sender + "\", from: \"" + from + "\", to: \"" + to + "\"}";
    }

    public String asRoute(){
        return "from " + from + " to " + to;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(sender);
        dest.writeDouble(pickupLocation.latitude);
        dest.writeDouble(pickupLocation.longitude);
        dest.writeDouble(deliveryLocation.latitude);
        dest.writeDouble(deliveryLocation.longitude);
    }

    public static final Parcelable.Creator<HarryAppTask> CREATOR = new Parcelable.Creator<HarryAppTask>()
    {
        @Override
        public HarryAppTask createFromParcel(Parcel source)
        {
            return new HarryAppTask(source);
        }

        @Override
        public HarryAppTask[] newArray(int size)
        {
            return new HarryAppTask[size];
        }
    };

    public HarryAppTask(Parcel in) {
        this.from = in.readString();
        this.to = in.readString();
        this.sender = in.readString();
        this.pickupLocation = new LatLng(in.readDouble(), in.readDouble());
        this.deliveryLocation = new LatLng(in.readDouble(), in.readDouble());
    }

}
