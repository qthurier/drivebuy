package com.drivebuy.harryapp;

// http://stackoverflow.com/questions/13341560/how-to-create-a-custom-dialog-box-in-android
// http://stackoverflow.com/questions/5393197/show-dialog-from-fragment

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;


import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BringDialog extends DialogFragment implements android.view.View.OnClickListener {
    private PlaceAutocompleteFragment autocompleteFragment1;
    private PlaceAutocompleteFragment autocompleteFragment2;
    private PlaceAutocompleteFragment autocompleteFragment3;
    private PlaceAutocompleteFragment autocompleteFragment4;
    private CardView card1;
    private CardView card2;
    private CardView card3;
    private CardView card4;
    private Dialog dialog;
    private Button save;
    private Button search;
    private String from;
    private String to;
    private String to2;
    private String to3;
    private String deviceId;
    private String small_box, medium_box, large_box;

    private LatLng start;
    private LatLng end;

    ArrayList<String> tasks = new ArrayList<String>();

    ArrayList<HarryAppTask> suggestedTasks = new ArrayList<HarryAppTask>();

    public BringDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (dialog == null) {
            /* retrieve device id */
            TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();

            /*set up the dialog box */
            dialog = new Dialog(getActivity());
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.setContentView(R.layout.bring_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            card1 = (CardView) dialog.findViewById(R.id.card_view1);
            card2 = (CardView) dialog.findViewById(R.id.card_view2);
            card3 = (CardView) dialog.findViewById(R.id.card_view3);
            card4 = (CardView) dialog.findViewById(R.id.card_view4);

            /* set up the adresses box event if they don't appear yet */
            autocompleteFragment1 = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_bring_from);
            autocompleteFragment1.setHint("Bring from");
            autocompleteFragment2 = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_bring_to);
            autocompleteFragment2.setHint("to");
            autocompleteFragment3 = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_bring_to_2);
            autocompleteFragment3.setHint("to");
            autocompleteFragment4 = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_bring_to_3);
            autocompleteFragment4.setHint("to");
            autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Log.i("****", "Origin: " + place.getAddress());
                    from = place.getAddress().toString().split(",")[0];
                    start = place.getLatLng();
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i("****", "An error occurred: " + status);
                }
            });
            autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Log.i("****", "Destination: " + place.getAddress());
                    to = place.getAddress().toString().split(",")[0];
                    end = place.getLatLng();
                    card3.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i("****", "An error occurred: " + status);
                }
            });
            autocompleteFragment3.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Log.i("****", "Destination: " + place.getAddress());
                    to2 = place.getAddress().toString().split(",")[0];
                    end = place.getLatLng();
                    card4.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i("****", "An error occurred: " + status);
                }
            });
            autocompleteFragment4.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Log.i("****", "Destination: " + place.getAddress());
                    to3 = place.getAddress().toString().split(",")[0];
                    end = place.getLatLng();
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i("****", "An error occurred: " + status);
                }
            });

            /* set up the buttons */
            save = (Button) dialog.findViewById(R.id.btn_save_route);
            save.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    saveRoute();
                }
            });
            search = (Button) dialog.findViewById(R.id.btn_search_tasks);
            search.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    suggestedTasks = new ArrayList<HarryAppTask>();

                    if (start == null) {
                        Toast.makeText(getActivity(), "Add a departure address", Toast.LENGTH_LONG).show();
                    } else if (end == null) {
                        Toast.makeText(getActivity(), "Add a destination address", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Looking for tasks on your route..", Toast.LENGTH_LONG).show();
                        listTask();
                    }
                }
            });
        }
        return dialog;
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }


    private void saveRoute() {

        /* TODO: quite simplistic preference at the moment need to improve */
        OkHttpClient client = new OkHttpClient();
        if (to2 == null) to2 = "nothing";
        if (to3 == null) to3 = "nothing";
        FormBody requestBody = new FormBody.Builder()
                .add("device", deviceId)
                .add("from", from)
                .add("to", to)
                .add("to2", to2)
                .add("to3", to3)
                .build();
        String url = getResources().getString(R.string.save_route_url);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        try {
            Response response = client.newCall(request).execute();
            Log.i("****", "Route has been saved");
            Log.i("****", "The Http response is: " + response.toString());
            Toast.makeText(getActivity(), "Route has been saved", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.i("****", "Failed to add task", e);
        }

    }

    private void listTask() {

        /* TODO: add filter based on the item size */
        CheckBox small = (CheckBox) dialog.findViewById(R.id.check_small);
        CheckBox medium = (CheckBox) dialog.findViewById(R.id.check_medium);
        CheckBox large = (CheckBox) dialog.findViewById(R.id.check_large);
        if (small.isChecked()) small_box = "1";
        else small_box = "0";
        if (medium.isChecked()) medium_box = "1";
        else medium_box = "0";
        if (large.isChecked()) large_box = "1";
        else large_box = "0";
        if (to2 == null) to2 = "nothing";
        if (to3 == null) to3 = "nothing";

        /* TODO: add the waypoints (there is a pull request for that) */
        GoogleDirection.withServerKey(getResources().getString(R.string.android_key))
                       .from(start)
                       .to(end)
                       .alternativeRoute(true)
                       .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                if (direction.isOK()) {
                                    /* TODO: take into account the alternative road */
                                    Route route = direction.getRouteList().get(0);
                                    Leg leg = route.getLegList().get(0);
                                    ArrayList<LatLng> pointList = leg.getDirectionPoint();
                                    for(HarryAppTask task: MapsActivity.allTasks) {
                                        boolean pickupAdressIsOnPath = PolyUtil.isLocationOnPath(task.pickupLocation, pointList, true, getResources().getInteger(R.integer.tolerance));
                                        boolean deliveryAdressIsOnPath = PolyUtil.isLocationOnPath(task.deliveryLocation, pointList, true, getResources().getInteger(R.integer.tolerance));
                                        if(pickupAdressIsOnPath && deliveryAdressIsOnPath) {
                                            /* TODO: add a check if pickup is before delivery, travellers salesman problem, can be solved with the optimize parameter */
                                            suggestedTasks.add(task);
                                            tasks.add(task.asJson());
                                        }
                                    }
                                } else {
                                    Log.i("****", "Error direction is not ok");
                                }
                                if (suggestedTasks.size() > 0) {
                                    for(HarryAppTask task: suggestedTasks) {
                                        Log.i("****", task.asJson());
                                    }
                                    dialog.dismiss();
                                    Intent intent = new Intent(getActivity(), LiteListDemoActivity.class);
                                    intent.putExtra("tasks", suggestedTasks);
                                    BringDialog.this.startActivity(intent);
                                } else {
                                    Toast.makeText(getActivity(), "Nothing to pickup bro..", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onDirectionFailure(Throwable t) {
                                Log.i("****", "Error when trying to retrieve direction");
                            }
                });
    }
}