package com.cookbook.fenix.cookbook;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


import java.util.ArrayList;


public class CookBOOK extends ActionBarActivity {


    //private String API_KEY = "3e9166ad629eca6587a5e501e4e30961";
    private final String API_KEY = "473dd92f0fbc20142cca69d26013bc65";   // my key
    private final String BUNDLE_RECIPE_ARRAY = "BundleRecipeArray";


    private final String SERVER_SERCH_URL = "http://food2fork.com/api/search";
    private final String SERVER_GET_URL = "http://food2fork.com/api/get";
    public static final String TEST = "CookBOOK";
    private final String DOWNLOADER_THREAD = "CurrentThread";


    //String SEARCH = "http://food2fork.com/api/search?key=3e9166ad629eca6587a5e501e4e30961&q=shredded%20chicken";
    //"http://food2fork.com/api/search?key={API_KEY}&q=shredded%20chicken";


    private Downloader imageDownloader;
    private EditText editText;
    private SharedPreferences prefs;
    public static RecipeAdapter sRecipeAdapter;
    //private LruCache<String, Bitmap> mMemoryCache;
    private ArrayList<Recipe> mRecipeList;
    private RecyclerView mRecyclerView;
    public static GridLayoutManager sGridLayoutManager;

    private boolean sort;
    private int column;
    private String query;
    private Integer page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_book);
        if (BuildConfig.DEBUG) Log.d(TEST, " Thread num = " + Thread.currentThread().hashCode());

        //restore preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        column = prefs.getInt(getResources().getString(R.string.column_one), 1);

        sRecipeAdapter = new RecipeAdapter(this);

       /* init LruCache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;
        if(BuildConfig.DEBUG) Log.d(TEST, "maxMemory= " + maxMemory+ " cacheSize= "+cacheSize);
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                if(Build.VERSION.SDK_INT>=12) {
                    if(BuildConfig.DEBUG) {
                        Log.d(TEST, " size= " + (bitmap.getByteCount() / 1024)
                                + " = " + (bitmap.getHeight() * bitmap.getWidth()));
                    }
                    return bitmap.getByteCount() / 1024;
                }else{
                    return bitmap.getHeight()*bitmap.getWidth();
                }
            }
        };*/

        //TODO: multiple threads
        imageDownloader = (Downloader) getLastCustomNonConfigurationInstance();

        if(imageDownloader ==null) {
            imageDownloader = new Downloader(this);
            imageDownloader.setRecipeAdapter(sRecipeAdapter);
            imageDownloader.start();
        }else{
            Log.d(TEST, "imageDownloader = "+imageDownloader.hashCode());
            imageDownloader.setLink(this);
            imageDownloader.setRecipeAdapter(sRecipeAdapter);
        }
        Log.d(TEST, "sRecipeAdapter " + sRecipeAdapter.hashCode());

        Button buttonSerch = (Button) findViewById(R.id.buttonserch);
        editText = (EditText) findViewById(R.id.editText);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        sGridLayoutManager = new GridLayoutManager(this, column);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(sGridLayoutManager);

        //Обробка натисканнь на Елемент
        sRecipeAdapter.setOnItemClickListener(new RecipeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                String value = RecipeAdapter.linkedList.get(position).getRecipeID();
                get(value, position);
            }
        });
        mRecyclerView.setAdapter(sRecipeAdapter);

        //Обробка натисканнь на кнопку пуску
        buttonSerch.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecipeAdapter.linkedList.clear();
                        sRecipeAdapter.notifyDataSetChanged();
                        Downloader.taskDeque.clear();
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TEST, "life  Resume");
    }

    @Override
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
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance(){
        Log.d(TEST, "imageDownloader = "+imageDownloader.hashCode());
        return imageDownloader;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanseState) {
        super.onRestoreInstanceState(savedInstanseState);
        Log.d(TEST, "life  onRestore");
        mRecipeList = savedInstanseState.getParcelableArrayList(BUNDLE_RECIPE_ARRAY);
        if (mRecipeList != null) {
            sRecipeAdapter = new RecipeAdapter(mRecipeList, this);
            mRecyclerView.setAdapter(sRecipeAdapter);
            imageDownloader.setRecipeAdapter(sRecipeAdapter);
            //TODO: multiple threads
            }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TEST, "life  onSave");
        imageDownloader.setRecipeAdapter(null);
        if (sRecipeAdapter.getItemCount() != 0) {
            mRecipeList = sRecipeAdapter.getData();
            outState.putParcelableArrayList(BUNDLE_RECIPE_ARRAY, mRecipeList);

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
        //send REST query using AsyncTask
        new RestAPI(this,imageDownloader).execute(SERVER_SERCH_URL, data);
    }

    private void get(String id, Integer position) {
        String data = "&key=" + API_KEY;
        data += "&rId=" + id;
        //send REST query using AsyncTask
        new RestAPI(this, position, imageDownloader).execute(SERVER_GET_URL, data);
    }

    public RecipeAdapter getmRecipeAdapter() {
        return sRecipeAdapter;
    }

    /*public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }*/

}
