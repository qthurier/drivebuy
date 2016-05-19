package com.drivebuy.harryapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SendDialog.MyDialogFragmentListener, ListLabelDialog.MyDialogFragmentListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String deviceId;
    private GoogleApiClient apiClient;
    private List<Marker> originMarkerList;
    private List<Marker> destinationMarkerList;
    public ClusterManager<OriginMarker> mOriginClusterManager;
    public ClusterManager<DestinationMarker> mDestinationClusterManager;

    private SendDialog dialogSend;
    private BringDialog dialogBring;
    private ListLabelDialog dialogListLabel;

    private PlaceAutocompleteFragment autocompleteFragment;
    private Location myLocation;

    private double defaultLat = -41;
    private double defaultLon = 174;
    private int defaultZoom = 9;

    private Button send;
    private Button bring;
    private Button tasks;
    private Button items;
    ListView listView;
    Bitmap pictureBitmap;
    Uri pictureFileUri;
    File pictureFile;
    File pictureToStore;
    ProgressDialog processDialog;

    public static final int MEDIA_TYPE_IMAGE = 1;

    public static ArrayList<HarryAppTask> allTasks = new ArrayList<HarryAppTask>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set up google map
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        originMarkerList = new ArrayList<Marker>();
        destinationMarkerList = new ArrayList<Marker>();

        // to avoid "NetworkOnMainThreadException"
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        setUpMapIfNeeded();
        setUpClusterer();
        addItems();

        // retrieve device id
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();





        // register to google cloud messenging
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

        // set up service for geolocation
        buildGoogleApiClient();
        if(apiClient != null){
            apiClient.connect();
        }

        // set up the search bar
        AutocompleteFilter cityFilter = new AutocompleteFilter.Builder()
                                                              .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                                              .build();
        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("change city");
        autocompleteFragment.setFilter(cityFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i("****", "Place: " + place.getName());
                LatLng ll = place.getLatLng();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, mMap.getCameraPosition().zoom));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("****", "An error occurred: " + status);
            }
        });

        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("****", "send!!!");
                showSendDialog();
                /*try {
                    launchCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        });

        items = (Button) findViewById(R.id.items);
        items.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("****", "items!!!");
            }
        });

        bring = (Button) findViewById(R.id.bring);
        bring.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("****", "bring!!!");
                showBringDialog();
            }
        });

        tasks = (Button) findViewById(R.id.tasks);
        tasks.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("****", "tasks!!!");
            }
        });



    }

    private void showSendDialog() {
        FragmentManager fm = getSupportFragmentManager();
        if(dialogSend == null){
            dialogSend = new SendDialog();
            //} else {
            //    dialogSend.mLabel = label;
            //    dialogSend.changeLabel();
        }
        dialogSend.show(fm, "fragment_send_dialog");
    }

    private void showSendDialog(String label) {
        FragmentManager fm = getSupportFragmentManager();
        if(dialogSend == null){
            dialogSend = new SendDialog(label);
        //} else {
        //    dialogSend.mLabel = label;
        //    dialogSend.changeLabel();
        }
        dialogSend.show(fm, "fragment_send_dialog");
    }

    private void showBringDialog() {
        FragmentManager fm = getSupportFragmentManager();
        if(dialogBring == null){
            dialogBring = new BringDialog();
        }
        dialogBring.show(fm, "fragment_bring_dialog");
    }

    private void showListLabel(String[] array) {
        FragmentManager fm = getSupportFragmentManager();
        if(dialogListLabel == null){
            dialogListLabel = new ListLabelDialog(array);
        } else {
            dialogListLabel.mLabels = array;
            dialogListLabel.dismiss();
            dialogListLabel = new ListLabelDialog(array);
        }
        dialogListLabel.show(fm, "fragment_list_label_dialog");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    //http://stackoverflow.com/questions/13435118/robust-way-to-pass-value-back-from-dialog-to-activity-on-android
    @Override
    public void onReturnValue(double origin_lat, double origin_lon, double destination_lat, double destination_lon, String from, String to, String sender) {
        Log.i("onReturnValue", "Got value back from Dialog!");
        OriginMarker origin = new OriginMarker(origin_lat, origin_lon);
        mOriginClusterManager.addItem(origin);
        DestinationMarker destination = new DestinationMarker(destination_lat, destination_lon);
        mDestinationClusterManager.addItem(destination);
        allTasks.add(new HarryAppTask(new LatLng(origin_lat, origin_lon),
                     new LatLng(destination_lat, destination_lon),
                     from,
                     to,
                     sender)
        );
        Log.i("onReturnValue", String.valueOf(allTasks.size()));
    }

    @Override
    public void onReturnValue(String itemValue) {
        showSendDialog(itemValue);
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        LatLng ll = new LatLng(defaultLat, defaultLon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, defaultZoom));

    }


    private void setUpClusterer() {
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mOriginClusterManager = new ClusterManager<OriginMarker>(this, mMap);
        mOriginClusterManager.setRenderer(new OriginIconRender(this.getApplicationContext(), mMap, mOriginClusterManager));

        mDestinationClusterManager = new ClusterManager<DestinationMarker>(this, mMap);
        mDestinationClusterManager.setRenderer(new DestinationIconRender(this.getApplicationContext(), mMap, mDestinationClusterManager));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mOriginClusterManager.onCameraChange(cameraPosition);
                mDestinationClusterManager.onCameraChange(cameraPosition);
            }
        });
    }

    private void addItems() {
        //https://developers.google.com/maps/documentation/android-api/utility/marker-clustering#simple
        OkHttpClient client = new OkHttpClient();
        String url = getResources().getString(R.string.list_tasks_url);
        Request request = new Request.Builder().url(url).get().build();
        try {
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            if (jsonData.length() > 2) {
                JSONArray Jarray = new JSONArray(jsonData);
                for (int i = 0; i < Jarray.length(); i++) {
                    JSONObject Jobject = Jarray.getJSONObject(i);
                    double origin_lat = Double.valueOf(Jobject.getDouble("origin_lat"));
                    double origin_lon = Double.valueOf(Jobject.getDouble("origin_lon"));
                    OriginMarker origin = new OriginMarker(origin_lat, origin_lon);
                    mOriginClusterManager.addItem(origin);
                    double destination_lat = Double.valueOf(Jobject.getDouble("destination_lat"));
                    double destination_lon = Double.valueOf(Jobject.getDouble("destination_lon"));
                    DestinationMarker destination = new DestinationMarker(destination_lat, destination_lon);
                    mDestinationClusterManager.addItem(destination);
                    allTasks.add(new HarryAppTask(new LatLng(origin_lat, origin_lon),
                                                  new LatLng(destination_lat, destination_lon),
                                                  Jobject.getString("city_from"),
                                                  Jobject.getString("city_to"),
                                                  Jobject.getString("sender"))
                                                 );
                }
            }
        } catch (JSONException e) {
            Log.i("****", "Failed to list the tasks", e);
        } catch (IOException e) {
            Log.i("****", "Failed to list the tasks", e);
        }
        Log.i("total number of tasks: ", String.valueOf(allTasks.size()));
    }

    protected synchronized void buildGoogleApiClient() {
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    // 2 - zoom on the current location
    @Override
    public void onConnected(Bundle arg0) {
        myLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if(myLocation != null) {
            double latitude = myLocation.getLatitude();
            double longitude = myLocation.getLongitude();
            LatLng ll = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, mMap.getCameraPosition().zoom));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void launchCamera() throws IOException {
        //http://stackoverflow.com/questions/10165302/dialog-to-pick-image-from-gallery-or-from-camera
        //http://stackoverflow.com/questions/12952859/capturing-images-with-mediastore-action-image-capture-intent-in-android
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        pictureFileUri = HarryAppUtils.getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureFileUri); // set the image file name
        startActivityForResult(intent, 0);
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                pictureBitmap = HarryAppUtils.scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri),1200);
                callCloudVision();
            } catch (IOException e) {
                Log.i("****", "Image picking failed because " + e.getMessage());
            }
        } else {
            Log.i("****", "Image picker gave us a null image.");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == Activity.RESULT_OK){
                    Log.i("****", "Take picture 2");
                    pictureFile = new File(pictureFileUri.getPath()); //original
                    Bitmap bitmap = BitmapFactory.decodeFile(pictureFileUri.getPath());
                    pictureToStore = new File(getCacheDir(), "resized.bmp");
                    try {
                        pictureToStore.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Bitmap newone = HarryAppUtils.scaleBitmapDown(bitmap, 200);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    newone.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(pictureToStore);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    uploadImage(pictureFileUri);
                }
                break;
        }
    }

    private ArrayList<String> convertResponseToStringArray(BatchAnnotateImagesResponse response) {
        ArrayList<String> stringLabels = new ArrayList<String>();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                Log.i("****", String.format("%.3f: %s", label.getScore(), label.getDescription()));
                //stringLabels.add(String.format("%.3f: %s", label.getScore(), label.getDescription()));
                stringLabels.add(label.getDescription().toUpperCase());
            }
        }
        return stringLabels;
    }

    private void callCloudVision() throws IOException {
        // Do the real work in an async task, because we need to use the network anyway
        new CustomAsyncTask(getApplicationContext()).execute();
    }

    public class CustomAsyncTask extends AsyncTask<Object, Void, ArrayList<String>> {
        private Context mContext;

        public CustomAsyncTask (Context context){
            mContext = context;
        }

        @Override
        protected ArrayList<String> doInBackground(Object... params) {
            try {
                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                builder.setVisionRequestInitializer(new
                        VisionRequestInitializer(getResources().getString(R.string.android_key)));
                Vision vision = builder.build();

                BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                        new BatchAnnotateImagesRequest();
                batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                    AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                    // Add the image
                    Image base64EncodedImage = new Image();
                    // Convert the bitmap to a JPEG
                    // Just in case it's a format that Android understands but Cloud Vision
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    // Base64 encode the JPEG
                    base64EncodedImage.encodeContent(imageBytes);
                    annotateImageRequest.setImage(base64EncodedImage);

                    // add the features we want
                    annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                        Feature labelDetection = new Feature();
                        labelDetection.setType("LABEL_DETECTION");
                        labelDetection.setMaxResults(10);
                        add(labelDetection);
                    }});

                    // Add the list of one thing to the request
                    add(annotateImageRequest);
                }});

                Vision.Images.Annotate annotateRequest =
                        vision.images().annotate(batchAnnotateImagesRequest);
                // Due to a bug: requests to Vision API containing large images fail when GZipped.
                annotateRequest.setDisableGZipContent(true);
                Log.i("****", "created Cloud Vision request object, sending request");

                BatchAnnotateImagesResponse response = annotateRequest.execute();
                return convertResponseToStringArray(response);

            } catch (GoogleJsonResponseException e) {
                Log.i("****", "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.i("****", "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return new ArrayList<String>();
        }

        protected void onPostExecute(ArrayList<String> result) {
            String[] array = new String[result.size()];
            array = result.toArray(array);
            Log.i("****", String.valueOf(result.size()));
            showListLabel(array);
            /*Bundle b = new Bundle();
            b.putStringArray("labels", array);
            Intent i = new Intent(mContext, LabelView.class);
            i.putExtras(b);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);*/
        }

    }

}

