package com.cookbook.fenix.cookbook;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Class used for managing requests to server http://food2fork.com/
 * <p/>
 * Created by fenix on 01.07.2015.
 */
public class RestAPI extends AsyncTask<String, String, JSONObject> {
    public static final String TEST = "test";
    private Activity activity;
    JSONObject jsonResponse;

    public RestAPI(Activity activity) {
        this.activity = activity;
    }


    @Override
    protected JSONObject doInBackground(String... params) {


        BufferedReader reader = null;

        // Send data
        try {
            // Defined URL  where to send data
            URL url = new URL(params[0]);
            Log.d(TEST, "1");

            // Send POST data request
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(params[1]);
            wr.flush();

            // Get the server response

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "");
            }

            jsonResponse = new JSONObject(sb.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        return jsonResponse;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        //Toast.makeText(activity, " Received", Toast.LENGTH_SHORT);
        TextView text = (TextView) activity.findViewById(R.id.textView3);
        text.setText(result.toString());


    }


}
