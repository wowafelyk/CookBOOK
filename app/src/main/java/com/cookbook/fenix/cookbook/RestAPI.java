package com.cookbook.fenix.cookbook;

import android.app.Activity;
import android.app.ActivityManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
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
    private FragmentManager fragmentManager;


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

    public RestAPI(Activity activity, FragmentManager fm) {
        this.activity = activity;
        this.fragmentManager = fm;
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
            Log.d(TEST, "2");
            jsonResponse = new JSONObject(sb.toString());
            Log.d(TEST, "3");
            if (jsonResponse.has(ARRAY_NAME)) {
                recipeArray = parseJSONArray(jsonResponse);
            } else {
                Log.d(TEST, "full_pars" + jsonResponse.toString());
                recipeArray[0] = parseJSONObject(jsonResponse.optJSONObject("recipe"));
                recipeArray[1] = null;
                Log.d(TEST, "JSONObject hasn't array name1");
            }


        } catch (MalformedURLException e) {
            Log.d(TEST, "Crach1 ");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d(TEST, "Crach2 ");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TEST, "Crach3 ");
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

        GridView gridView = (GridView) activity.findViewById(R.id.gridView);
        RecipeAdapter list;

        if (result[1] != null) {
            list = new RecipeAdapter(activity, R.layout.item_layout, result);
            gridView.setAdapter(list);
        } else {
            Recipe r = (Recipe) result[0];
            Log.d(TEST, "Recipe = " + r.toString());
            RecipeFragment rf = new RecipeFragment().newInstance(r);
            rf.show(fragmentManager, "MyRecipeFragment");
        }

    }


    private Recipe parseJSONObject(JSONObject obj) {

        Log.d(TEST, "5");
        Recipe result = new Recipe(
                obj.optString(TITLE), obj.optString(RECIPE_ID), obj.optString(SOCIAL_RANK),
                obj.optString(PUBLISHER), obj.optString(IMG_URL));


        if (obj.has(INGREDIENTS)) {
            //JSONObject obj=obj.optJSONObject("recipe");
            String[] s = new String[30];
            JSONArray arr = obj.optJSONArray(INGREDIENTS);
            for (int i = 0; i < arr.length(); i++) {
                s[i] = arr.optString(i);
            }
            result.setIngredients(s);
        }

        Log.d(TEST, "Publisher = " + result.getPublisher());
        Log.d(TEST, "Title = " + result.getTitle());

        try {
            URL url = new URL(result.getImgURL());
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            result.setBmp(bmp);
            Log.d(TEST, result.getBmp().toString());
            Log.d(TEST, "BMP download");
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
            Log.d(TEST, "4");
            JSONArray jsonArray = obj.getJSONArray(ARRAY_NAME);
            for (int i = 0; i < Integer.parseInt(obj.getString("count")); i++) {
                Log.d(TEST, "int i = " + i);
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
