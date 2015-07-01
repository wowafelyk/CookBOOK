package com.cookbook.fenix.cookbook;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class CookBOOK extends ActionBarActivity {

    private String API_KEY="3e9166ad629eca6587a5e501e4e30961";
    private String MY_API_KEY="473dd92f0fbc20142cca69d26013bc65";

    private final String SERVER_SERCH_URL="http://food2fork.com/api/search";
    private final String SERVER_GET_URL="http://food2fork.com/api/get";
    public static final String TEST = "test";
    String SEARCH="http://food2fork.com/api/search?key=3e9166ad629eca6587a5e501e4e30961&q=shredded%20chicken";
    //"http://food2fork.com/api/search?key={API_KEY}&q=shredded%20chicken";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_book);
        LinearLayout root = (LinearLayout)findViewById(R.id.root);
        Button buttonSerch = (Button)findViewById(R.id.buttonserch);
        buttonSerch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                search("shredded%20chicken",null,null);


                  }
        });
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void search(String q, String s, String p){
        String data = "&key="+API_KEY;
        data+= "&q="+q;
        if(s!=null) {
            Log.d(TEST, " not NULL ");
            data += "&sort=" + s;
        }
        if(p!=null) {
            data += "&page=" + p;
        }

        new RestAPI(this).execute(SERVER_SERCH_URL,data);
    }
    private void get(String id){
        String data = "?key="+API_KEY;
        data+= "&rId="+id;

        new RestAPI(this).execute(SERVER_GET_URL,data);
    }

}
