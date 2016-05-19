package com.drivebuy.harryapp;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by delavegas on 03/04/16.
 */
public class MultiCameraListener implements GoogleMap.OnCameraChangeListener {
    private List<GoogleMap.OnCameraChangeListener> mListeners = new ArrayList<GoogleMap.OnCameraChangeListener>();

    public void registerListener (GoogleMap.OnCameraChangeListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition)
    {
        for (GoogleMap.OnCameraChangeListener ccl: mListeners)
        {
            ccl.onCameraChange(cameraPosition);
        }
    }

}
