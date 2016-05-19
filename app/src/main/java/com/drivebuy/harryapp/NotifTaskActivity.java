package com.drivebuy.harryapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NotifTaskActivity extends ActionBarActivity {
    TextView sender;
    TextView from;
    TextView to;
    ImageView picture;
    String taskId;
    String b64EncodedPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_task);
        Bundle bundle = getIntent().getExtras();
        /*String sender_id = bundle.getString("sender");
        String city_from = bundle.getString("city_from");
        String city_to = bundle.getString("city_to");*/
        String sender_id = getIntent().getStringExtra("sender");
        String city_from = getIntent().getStringExtra("city_from");
        String city_to = getIntent().getStringExtra("city_to");
        taskId = getIntent().getStringExtra("task");
        //sender = (TextView) findViewById(R.id.sender_id);
        //sender.setText("Sender: " + sender_id);
        from = (TextView) findViewById(R.id.city_from);
        from.setText(city_from);
        to = (TextView) findViewById(R.id.city_to);
        to.setText(city_to);
        picture = (ImageView) findViewById(R.id.picture);

        FetchTaskPicture();

        byte[] decodedString = Base64.decode(b64EncodedPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        picture.setImageBitmap(decodedByte);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_notif_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void FetchTaskPicture() {
        OkHttpClient client = new OkHttpClient();
        String url = getResources().getString(R.string.fetch_picture_url);
        Request request = new Request.Builder().url(url + "?id=" + taskId).get().build();
        try {
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            JSONObject Jobject = new JSONObject(jsonData);
            b64EncodedPicture = Jobject.getString("picture");
            //Log.i("total number of tasks: ", Jobject.getString("picture"));
        } catch (JSONException e) {
            Log.i("****", "Failed to list the tasks", e);
        } catch (IOException e) {
            Log.i("****", "Failed to list the tasks", e);
        }
    }



}
