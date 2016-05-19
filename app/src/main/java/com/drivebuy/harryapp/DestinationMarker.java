package com.drivebuy.harryapp;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by delavegas on 03/04/16.
 */
public class DestinationMarker implements ClusterItem {
    private final LatLng mPosition;

    public DestinationMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public BitmapDescriptor getIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.flag);
    }

    public String getSnippet() {
        return "Destination";
    }

    public String getTitle() {
        return "Destination";
    }

}

