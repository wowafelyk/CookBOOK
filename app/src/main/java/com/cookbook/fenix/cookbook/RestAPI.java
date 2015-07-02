package com.cookbook.fenix.cookbook;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
public class RestAPI extends AsyncTask<String, String, Recipe[]> {
    public static final String TEST = "test";
    private Activity activity;


    private final String ARRAY_NAME = "recipes";
    private final String RECIPE_NAME = "recipe";
    private final String PUBLISHER = "publisher";
    private final String INGREDIENTS = "ingredients";
    private final String RECIPE_ID = "recipe_id";
    private final String IMG_URL = "image_url";
    private final String SOCIAL_RANK = "social_rank";
    private final String TITLE = "title";


    public RestAPI(Activity activity) {
        this.activity = activity;
    }


    @Override
    protected Recipe[] doInBackground(String... params) {

        BufferedReader reader = null;
        JSONObject jsonResponse;
        Recipe[] recipeArray = new Recipe[30];

        // Send data
        try {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity.getApplicationContext(), " Загрузка даних зачекайте", Toast.LENGTH_SHORT).show();
                }
            });

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
            if (jsonResponse.has(ARRAY_NAME)) {
                recipeArray = parseJSONArray(jsonResponse);
            } else {
                recipeArray[0] = parseJSONObject(jsonResponse);
                recipeArray[1] = null;
            }


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


        return recipeArray;
    }

    @Override
    protected void onPostExecute(Recipe[] result) {
        //Toast.makeText(activity, " Received", Toast.LENGTH_SHORT);
        //TextView text = (TextView) activity.findViewById(R.id.textView2);
        //text.setText(result.toString());

        ListView listView = (ListView) activity.findViewById(R.id.listView);
        RecipeAdapter list;

        if (result[1] != null) {
            list = new RecipeAdapter(activity,R.layout.activity_cook_book,result);
            listView.setAdapter(list);
        } else {
            Recipe r = result[0];
        }

    }


    private Recipe parseJSONObject(JSONObject obj) {
        Recipe result = new Recipe(
                obj.optString(TITLE), obj.optString(RECIPE_ID), obj.optString(SOCIAL_RANK),
                obj.optString(PUBLISHER), obj.optString(IMG_URL));

        try {
            URL url = new URL(result.getImgURL());
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            result.setBmp(bmp);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TEST, result.getPublisher());
        Log.d(TEST, " parseJSONObject");

        return result;
    }

    private Recipe[] parseJSONArray(JSONObject obj) {
        Recipe[] recipes = new Recipe[30];
        try {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity.getApplicationContext(), " Загрузка фото/відео даних", Toast.LENGTH_SHORT).show();
                }
            });
            JSONArray jsonArray = obj.getJSONArray(ARRAY_NAME);
            for (int i = 0; i < Integer.parseInt(obj.getString("count")); i++) {
                recipes[i] = parseJSONObject(jsonArray.getJSONObject(i));
            }


        } catch (JSONException e) {
            Log.d(TEST, " Catch parseJSON");
            e.printStackTrace();
        }

        Log.d(TEST, ((Recipe) (recipes[1])).getPublisher());

        return recipes;
    }

}
