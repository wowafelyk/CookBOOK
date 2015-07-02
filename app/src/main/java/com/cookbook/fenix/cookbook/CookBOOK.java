package com.cookbook.fenix.cookbook;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;


public class CookBOOK extends ActionBarActivity {


    private String API_KEY = "3e9166ad629eca6587a5e501e4e30961";
    private final String MY_API_KEY = "473dd92f0fbc20142cca69d26013bc65";
    private final String BUNDLE_RECIPE_ARRAY = "BundleRecipeArray";

    private final String SERVER_SERCH_URL = "http://food2fork.com/api/search";
    private final String SERVER_GET_URL = "http://food2fork.com/api/get";
    public static final String TEST = "test";
    //String SEARCH = "http://food2fork.com/api/search?key=3e9166ad629eca6587a5e501e4e30961&q=shredded%20chicken";
    //"http://food2fork.com/api/search?key={API_KEY}&q=shredded%20chicken";

    //private final String BUNDLE_RECIPE_ARRAY = "preferencesRecipeArray";

    private Recipe[] recipeArray = new Recipe[30];
    private GridView gridView;
    private EditText editText;
    private RecipeAdapter recipeAdapter;
    private boolean sort;
    private SharedPreferences prefs;
    private int column;
    private String query;
    private Integer page;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_book);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        column = prefs.getInt(getResources().getString(R.string.column_one), 1);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setNumColumns(column);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                String value = ((Recipe) adapter.getItemAtPosition(position)).getRecipeID();
                get(value);

            }
        });

        Button buttonSerch = (Button) findViewById(R.id.buttonserch);
        editText = (EditText) findViewById(R.id.editText);

        buttonSerch.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        page = 1;
                        Editable edit = editText.getText();
                        query = edit.toString();
                        sort = prefs.getBoolean("TOP Rated", true);
                        String s = sort ? "r" : "t";
                        search(edit.toString(), s, page.toString());


                    }
                }

        );


    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanseState) {
        super.onRestoreInstanceState(savedInstanseState);
        try {
            ArrayList<Recipe> arrayList = (ArrayList) savedInstanseState.getSerializable(BUNDLE_RECIPE_ARRAY);
            for (int i = 0; i < 30; i++) {
                recipeArray[i] = arrayList.get(i);
            }
            recipeAdapter = new RecipeAdapter(this, R.layout.item_layout, recipeArray);
            gridView.setAdapter(recipeAdapter);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if ((gridView.getAdapter()) != null) {
            recipeArray = ((RecipeAdapter) gridView.getAdapter()).getData();
            outState.putSerializable(BUNDLE_RECIPE_ARRAY, (new ArrayList<>(Arrays.asList(recipeArray))));
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


            sort = prefs.getBoolean("TOP Rated", true);
            String s = sort ? "r" : "t";
            page++;
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
        data += "&q=" + q;
        if (s != null) {
            data += "&sort=" + s;
        }
        if (p != null) {
            data += "&page=" + p;
        }

        new RestAPI(this).execute(SERVER_SERCH_URL, data);
    }

    private void get(String id) {
        String data = "&key=" + API_KEY;
        data += "&rId=" + id;

        new RestAPI(this, getSupportFragmentManager()).execute(SERVER_GET_URL, data);
    }

   /* public int getLayout() {

        int layoutResourceId;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(getResources().getString(R.string.ListOutput), true)) {
            layoutResourceId = R.layout.activity_cook_book;
        } else {
            layoutResourceId = R.layout.grid_activity_cook_book;
        }
        return layoutResourceId;
    }*/

}
