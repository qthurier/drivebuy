package com.drivebuy.harryapp;

// http://stackoverflow.com/questions/13341560/how-to-create-a-custom-dialog-box-in-android
// http://stackoverflow.com/questions/5393197/show-dialog-from-fragment

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.Handler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

public class SendDialog extends DialogFragment implements android.view.View.OnClickListener {
    private PlaceAutocompleteFragment autocompleteFragment1;
    private PlaceAutocompleteFragment autocompleteFragment2;
    private Dialog dialog;
    private Button add;
    private RadioGroup sizes;
    private Button picture;
    private String deviceId;
    private String from;
    private String to;
    private double origin_lat;
    private double origin_lon;
    private double destination_lat;
    private double destination_lon;
    final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpg");
    File picture_file;
    File f;
    Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private TextView mImageDetails;
    public static final String FILE_NAME = "temp.jpg";
    TextView itemLabel;
    EditText mEdit;
    String mLabel;

    private CardView card1;
    private CardView card2;

    public SendDialog() {
        // Empty constructor required for DialogFragment
    }

    public SendDialog(String label) {
        mLabel = label;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (dialog == null) {
            TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
            dialog = new Dialog(getActivity());
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.setContentView(R.layout.send_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //mEdit = (EditText) dialog.findViewById(R.id.edittext);
            //mEdit.setText(mLabel);
            //mEdit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            card1 = (CardView) dialog.findViewById(R.id.send_card_view1);
            card2 = (CardView) dialog.findViewById(R.id.send_card_view2);
            autocompleteFragment1 = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_send_from);
            autocompleteFragment1.setHint("Send from");
            autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Log.i("****", "Origin: " + place.getAddress());
                    LatLng ll = place.getLatLng();
                    origin_lat = ll.latitude;
                    origin_lon = ll.longitude;
                    String[] chunks = place.getAddress().toString().split(",");
                    from = chunks[chunks.length - 2].replaceAll("\\d","");
                }
                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i("****", "An error occurred: " + status);
                }
            });
            autocompleteFragment2 = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_send_to);
            autocompleteFragment2.setHint("to");
            autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Log.i("****", "Destination: " + place.getAddress());
                    LatLng ll = place.getLatLng();
                    destination_lat = ll.latitude;
                    destination_lon = ll.longitude;
                    String[] chunks = place.getAddress().toString().split(",");
                    to = chunks[chunks.length - 2].replaceAll("\\d","");
                }
                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i("****", "An error occurred: " + status);
                }
            });
            List<Integer> filters = new ArrayList<Integer>();
            filters.add(2); // addresses filter
            AutocompleteFilter filter = AutocompleteFilter.create(filters);
            //autocompleteFragment1.setFilter(filter);
            autocompleteFragment2.setFilter(filter);
            add = (Button) dialog.findViewById(R.id.btn_add_task);
            sizes = (RadioGroup) dialog.findViewById(R.id.radio);
            add.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(fileUri == null) {
                        Toast.makeText(getActivity(), "Add a picture", Toast.LENGTH_LONG).show();
                    } else if (from == null) {
                        Toast.makeText(getActivity(), "Add a pick up address", Toast.LENGTH_LONG).show();
                    } else if (to == null) {
                        Toast.makeText(getActivity(), "Add a drop off address", Toast.LENGTH_LONG).show();
                    } else {
                        Log.i("****", "add task!!!");
                        try {
                            addTask();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            picture = (Button) dialog.findViewById(R.id.btn_add_picture);
            picture.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        openGallery();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return dialog;
    }

    private void openGallery() throws IOException {
        //http://stackoverflow.com/questions/10165302/dialog-to-pick-image-from-gallery-or-from-camera
        //http://stackoverflow.com/questions/12952859/capturing-images-with-mediastore-action-image-capture-intent-in-android
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        startActivityForResult(intent, 0);
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // Resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;

    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == Activity.RESULT_OK){
                    Log.i("****", "Take picture 2");
                    picture_file = new File(fileUri.getPath()); //original
                    Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
                    f = new File(getContext().getCacheDir(), "resized.bmp");
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Bitmap newone = getResizedBitmap(bitmap, 200, 150);
                    Bitmap newone = HarryAppUtils.scaleBitmapDown(bitmap, 750);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    newone.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(f);
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
                    //uploadImage(Uri.fromFile(getCameraFile()));
                    //uploadImage(fileUri);
                }

                break;
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    picture_file = new File(selectedImage.getPath());
                }
                break;
        }
    }
    @Override
    public void onClick(View v) {
        dismiss();
    }

    public interface MyDialogFragmentListener {
        public void onReturnValue(double origin_lat, double origin_lon, double destination_lat, double destination_lon, String from, String to, String deviceId);
    }


    private void addTask() throws IOException {
        RadioButton size = (RadioButton) dialog.findViewById(sizes.getCheckedRadioButtonId());
        String task_size = size.getText().toString();
        String task_sender = deviceId;
        String task_origin_lat = String.valueOf(origin_lat);
        String task_origin_lon = String.valueOf(origin_lon);
        String task_destination_lat = String.valueOf(destination_lat);
        String task_destination_lon = String.valueOf(destination_lon);
        OkHttpClient client = new OkHttpClient();
        Log.i("**** from ", from);
        Log.i("**** to ", to);
        MultipartBody requestBody = new MultipartBody.Builder ()
                .setType(MultipartBody.FORM)
                .addFormDataPart("sender", task_sender)
                .addFormDataPart("size", task_size)
                .addFormDataPart("origin_lat", task_origin_lat)
                .addFormDataPart("origin_lon", task_origin_lon)
                .addFormDataPart("destination_lat", task_destination_lat)
                .addFormDataPart("destination_lon", task_destination_lon)
                .addFormDataPart("from", from)
                .addFormDataPart("to", to)
                .addFormDataPart("picture", "picture.jpg", RequestBody.create(MEDIA_TYPE_PNG, f))
                .build();
        String url = getResources().getString(R.string.add_task_url);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("****", "Failed to add task", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i("****", "Task has been added");
                    Log.i("****", "The Http response is: " + response.toString());
                    backgroundThreadShortToast(getActivity(), "Task has been added");
                }
            });
            dialog.dismiss();
        Toast.makeText(getActivity(), "Task has been added", Toast.LENGTH_LONG).show();
        MyDialogFragmentListener activity = (MyDialogFragmentListener) getActivity();
        activity.onReturnValue(origin_lat, origin_lon, destination_lat, destination_lon, from, to, deviceId);

    }

    public static void backgroundThreadShortToast(final Context context,
                                                  final String msg) {
        if (context != null && msg != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri),
                                1200);

                callCloudVision(bitmap);
                //mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.i("****", "Image picking failed because " + e.getMessage());
                //Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i("****", "Image picker gave us a null image.");
            //Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    public void uploadImage(Bitmap bmp) {
        if (true) {
            try {
                // scale the image to save on bandwidth

                callCloudVision(bmp);
                //mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.i("****", "Image picking failed because " + e.getMessage());
                //Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i("****", "Image picker gave us a null image.");
            //Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
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
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
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
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.i("****", "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.i("****", "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                //mImageDetails.setText(result);
                Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format("%.3f: %s", label.getScore(), label.getDescription());
                //Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                backgroundThreadShortToast(getActivity(), message);
                message += "\n";
            }
        } else {
            message += "nothing";
        }

        return message;
    }

    public void changeLabel(){
        mEdit.setText(mLabel);
    }
}