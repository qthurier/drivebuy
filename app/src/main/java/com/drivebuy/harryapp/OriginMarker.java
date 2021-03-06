package com.drivebuy.harryapp;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by delavegas on 03/04/16.
 */
public class OriginMarker implements ClusterItem {
    private final LatLng mPosition;

    public OriginMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public BitmapDescriptor getIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.parcel);
    }

    public String getSnippet() {
        return "Origin";
    }

    public String getTitle() {
        return "Origin";
    }

}

