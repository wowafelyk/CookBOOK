package com.cookbook.fenix.cookbook;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedList;


/**
 * Created by fenix on 01.07.2015.
 */
public class RecipeAdapter extends ArrayAdapter<Recipe> {

    public final String TEST = "RecipeAdapter : ";

    private Context context;
    private int layoutResourceId;
    private LinkedList<Recipe> data = null;

    public RecipeAdapter(Context context){
        super(context, R.layout.item_layout);
        this.layoutResourceId = R.layout.item_layout;
        this.context = context;
    };


    public RecipeAdapter(Context context, int layoutResourceId, LinkedList<Recipe> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }


    @Override
    public synchronized View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecipeHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RecipeHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imageView);
            holder.txtTitle = (TextView) row.findViewById(R.id.textView);
            holder.txtPublisher = (TextView) row.findViewById(R.id.textView2);

            Log.d(TEST, "Holder TEST 1");
            row.setTag(holder);
        } else {
            Log.d(TEST, "Holder TEST 2");
            holder = (RecipeHolder) row.getTag();
        }
        Log.d(TEST, "Position = "+position);

        Recipe recipe = this.getItem(position);


        holder.txtTitle.setText(recipe.getTitle());
        holder.txtPublisher.setText(recipe.getPublisher());
        holder.imgIcon.setImageBitmap(recipe.getBmp());
        return row;
    }



    static class RecipeHolder {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtPublisher;
    }

    public LinkedList<Recipe> getData() {

            data = new LinkedList<Recipe>();
            for (int i = 0; i < this.getCount(); i++) {
            data.add(this.getItem(i));

        }

        return data;
    }

    public synchronized void alterItem(Recipe r, int position){

        this.remove(this.getItem(position));
        this.insert(r, position);

    }
}

