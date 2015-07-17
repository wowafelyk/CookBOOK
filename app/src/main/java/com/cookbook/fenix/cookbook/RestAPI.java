package com.cookbook.fenix.cookbook;

import android.app.Activity;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class used for managing requests to server http://food2fork.com/
 * <p/>
 * Created by fenix on 01.07.2015.
 */
public class RestAPI extends AsyncTask<String, String, Recipe[]> {
    public static final String TEST = "test";
    private WeakReference<Activity> activityWeakReference;
    private Integer itemPosition;
    private Integer numberOfItems;
    private Downloader mImageDownloader;

    private final String ARRAY_NAME = "recipes";
    private final String RECIPE_NAME = "recipe";
    private final String PUBLISHER = "publisher";
    private final String INGREDIENTS = "ingredients";
    private final String RECIPE_ID = "recipe_id";
    private final String IMG_URL = "image_url";
    private final String SOCIAL_RANK = "social_rank";
    private final String TITLE = "title";


    public RestAPI(Activity activity, Downloader downloader) {
        this.activityWeakReference = new WeakReference<Activity>(activity);
        this.mImageDownloader = downloader;
    }

    public RestAPI(Activity activity, Integer position, Downloader downloader) {
        this.activityWeakReference = new WeakReference<Activity>(activity);
        this.mImageDownloader = downloader;
        this.itemPosition = position;
    }


    @Override
    protected Recipe[] doInBackground(String... params) {

        BufferedReader reader = null;
        JSONObject jsonResponse;
        Recipe[] recipeArray = new Recipe[30];

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
            Log.d(TEST, "Serch params = " + params[1]);
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
            Log.d(TEST, "JSON = " + sb.toString());

            //Start Parsing JSON OBJECT
            jsonResponse = new JSONObject(sb.toString());
            Log.d(TEST, "JSON = " + sb.toString());


            /**Cheeking what response we get :
             * -Array(-array of recipes; -nothing was found)
             * -Object
             */

            if (jsonResponse.has(ARRAY_NAME)) {
                numberOfItems = Integer.parseInt(jsonResponse.getString("count"));
                if (numberOfItems == 0) {
                    return recipeArray;
                }
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

        RecipeAdapter mRecipeAdapter;
        Activity activity = activityWeakReference.get();

        if (activity != null) {
            mRecipeAdapter = mImageDownloader.getRecipeAdapter();
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            activityWeakReference = mImageDownloader.getLink();
            activity = activityWeakReference.get();
            mRecipeAdapter = mImageDownloader.getRecipeAdapter();
        }


        if (result[0] == null) {
            try {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activityWeakReference.get(),
                                " По вашому запиту нічого не знайдено", Toast.LENGTH_LONG).show();

                    }
                });
            } catch (NullPointerException e) {
                activityWeakReference = mImageDownloader.getLink();
                activityWeakReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activityWeakReference.get(),
                                " По вашому запиту нічого не знайдено", Toast.LENGTH_LONG).show();

                    }
                });
            }
        } else {
            if (result[1] != null) {
                for (int i = 0; i < numberOfItems; i++) {
                    result[i].getImgURL();
                    RecipeAdapter.linkedList.add(result[i]);
                    Downloader.taskDeque.add(new ImageDownloadTask(result[i],
                            RecipeAdapter.linkedList.indexOf(result[i]), null));
                    //Log.d(TEST, "Count = " + mRecipeAdapter.getItemCount());
                }
                mRecipeAdapter.notifyDataSetChanged();
                //Log.d(TEST, "ТЕСТ = " + RecipeAdapter.linkedList.get(0).getTitle() + " = " + RecipeAdapter.linkedList.get(1).getTitle());
            } else {
                Log.d(TEST, " get set");
                RecipeFragment rf;
                Recipe r = result[0];
                RecipeAdapter.linkedList.set(itemPosition, r);
                rf = new RecipeFragment().newInstance(r);
                rf.show(((CookBOOK) activity).getSupportFragmentManager(), "MyRecipeFragment");
            }
        }

    }


    private Recipe parseJSONObject(JSONObject obj) {

        Log.d(TEST, "ParseJSONObject");
        Recipe result = new Recipe(
                obj.optString(TITLE), obj.optString(RECIPE_ID), obj.optString(SOCIAL_RANK),
                obj.optString(PUBLISHER), obj.optString(IMG_URL));


        if (obj.has(INGREDIENTS)) {
            String[] s = new String[40];
            JSONArray arr = obj.optJSONArray(INGREDIENTS);
            for (int i = 0; i < arr.length(); i++) {
                s[i] = arr.optString(i);
            }
            result.setIngredients(s);
        }
        return result;
    }

    private Recipe[] parseJSONArray(JSONObject obj) {

        try {
            Recipe[] recipes = new Recipe[numberOfItems];
            JSONArray jsonArray = obj.getJSONArray(ARRAY_NAME);

            for (int i = 0; i < numberOfItems; i++) {
                recipes[i] = parseJSONObject(jsonArray.getJSONObject(i));
            }
            return recipes;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d(TEST, ((Recipe) (recipes[1])).getPublisher());
        return null;
    }

}
