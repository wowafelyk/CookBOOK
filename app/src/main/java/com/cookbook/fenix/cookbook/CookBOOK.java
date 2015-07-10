package com.cookbook.fenix.cookbook;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;




import java.util.LinkedList;

import java.util.concurrent.LinkedBlockingDeque;


public class CookBOOK extends ActionBarActivity {


    private String API_KEY = "3e9166ad629eca6587a5e501e4e30961";
    private final String MY_API_KEY = "473dd92f0fbc20142cca69d26013bc65";   // my key
    private final String BUNDLE_RECIPE_ARRAY = "BundleRecipeArray";


    private final String SERVER_SERCH_URL = "http://food2fork.com/api/search";
    private final String SERVER_GET_URL = "http://food2fork.com/api/get";
    public static final String TEST = "CookBOOK";
    private final String DOWNLOADER_THREAD = "CurrentThread";


    //String SEARCH = "http://food2fork.com/api/search?key=3e9166ad629eca6587a5e501e4e30961&q=shredded%20chicken";
    //"http://food2fork.com/api/search?key={API_KEY}&q=shredded%20chicken";

    //private final String BUNDLE_RECIPE_ARRAY = "preferencesRecipeArray";

    private Recipe[] recipeArray = new Recipe[30];

    private Downloader imageDownloader; //
    private GridView gridView;
    private EditText editText;
    private boolean sort;
    private SharedPreferences prefs;
    private int column;
    private String query;
    private Integer page = 1;
    private RecipeAdapter recipeAdapter;

    static LinkedList<Recipe> recipeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_book);
        Log.d(TEST, "CREATE");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //int memoryClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        //Log.d(TEST, "Cache available " + memoryClass);
        Log.d(TEST, " Thread num = " + Thread.currentThread().getId());
        recipeAdapter = new RecipeAdapter(this);

        imageDownloader = (Downloader)getLastCustomNonConfigurationInstance();

        if(imageDownloader ==null) {
            imageDownloader = new Downloader(this);
            imageDownloader.start();
        }else{
            Log.d(TEST, "imageDownloader = "+imageDownloader.hashCode());
            imageDownloader.setLink(this);
        }
        Log.d(TEST,"recipeAdapter "+recipeAdapter.hashCode());
        imageDownloader.setRecipeAdapter(recipeAdapter);
        //imageDownloader.start();

        column = prefs.getInt(getResources().getString(R.string.column_one), 1);
        Button buttonSerch = (Button) findViewById(R.id.buttonserch);
        editText = (EditText) findViewById(R.id.editText);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setNumColumns(column);

        //Обробка натисканнь на Елемент
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                String value = ((Recipe) adapter.getItemAtPosition(position)).getRecipeID();
                get(value, position);
            }
        });


        //Обробка натисканнь на кнопку пуску
        buttonSerch.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recipeAdapter.clear();
                        imageDownloader.taskDeque.clear();
                        page = 1;
                        Editable edit = editText.getText();
                        query = edit.toString();
                        sort = prefs.getBoolean("TOP Rated", true);
                        String s = sort ? "r" : "t";
                        search(query, s, page.toString());
                    }
                }
        );
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        Log.d(TEST, "life  PAUSE");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TEST, "life  STOP");
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d(TEST, "life DESTROY");
    } */

    @Override
    public Object onRetainCustomNonConfigurationInstance(){
        Log.d(TEST, "imageDownloader = "+imageDownloader.hashCode());
        return imageDownloader;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanseState) {
        super.onRestoreInstanceState(savedInstanseState);
        try {
            recipeList = (LinkedList) savedInstanseState.getSerializable(BUNDLE_RECIPE_ARRAY);
            /*for (int i = 0; i < 30; i++) {
                recipeArray[i] = arrayList.get(i);
            } */
            if(recipeList!=null) {

                recipeAdapter = new RecipeAdapter(this, R.layout.item_layout, recipeList);
                gridView.setAdapter(recipeAdapter);
                imageDownloader.setRecipeAdapter(recipeAdapter);
            }
        } catch (NullPointerException e) {
            Log.e(TEST, "on REstore error");
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            //imageDownloader.setStop();
            if ((gridView.getAdapter()) != null) {
                recipeList = recipeAdapter.getData();
                outState.putSerializable(BUNDLE_RECIPE_ARRAY, (recipeList));
            }
        } catch (NullPointerException e) {
            Log.e(TEST, "on onSave error");
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cook_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_serch) {
            page++;
            sort = prefs.getBoolean("TOP Rated", true);
            String s = sort ? "r" : "t";
            search(query, s, page.toString());
        }
        if (id == R.id.action_settings) {
            SettingsFragment sf = new SettingsFragment();
            sf.show(getSupportFragmentManager(), "MySF");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void search(String q, String s, String p) {
        String data = "&key=" + API_KEY;
        //if (q != ""&q != " ")
        data += "&q="+q;
        if (s != null) {
            data += "&sort=" + s;
        }
        if (p != null) {
            data += "&page=" + p;
        }

        new RestAPI(this,imageDownloader).execute(SERVER_SERCH_URL, data);
    }

    private void get(String id, Integer position) {
        String data = "&key=" + API_KEY;
        data += "&rId=" + id;


            new RestAPI(this,getSupportFragmentManager(), position,imageDownloader).execute(SERVER_GET_URL, data);

    }

    public RecipeAdapter getRecipeAdapter(){
        return recipeAdapter;
    }

}
