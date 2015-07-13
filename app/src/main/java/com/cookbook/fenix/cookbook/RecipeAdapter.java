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

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Created by fenix on 01.07.2015.
 */
public class RecipeAdapter extends ArrayAdapter<Recipe> {

    public final String TEST = "RecipeAdapter : ";

    private Context context;
    private int layoutResourceId;
    private ArrayList<Recipe> data = null;

    public RecipeAdapter(Context context) {
        super(context, R.layout.item_layout);
        this.layoutResourceId = R.layout.item_layout;
        this.context = context;
    }

    ;


    public RecipeAdapter(Context context, int layoutResourceId, ArrayList<Recipe> data) {
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
            holder.txtRank = (TextView) row.findViewById(R.id.textView3);
            row.setTag(holder);
        } else {
            holder = (RecipeHolder) row.getTag();
        }
        Log.d(TEST, "Position = " + position);

        Recipe recipe = this.getItem(position);


        holder.txtTitle.setText(recipe.getTitle());
        holder.txtPublisher.setText(recipe.getPublisher());
        holder.imgIcon.setImageBitmap(recipe.getBitmap());
        holder.txtRank.setText("Rating = " + recipe.getSocialRank());
        return row;
    }


    static class RecipeHolder {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtPublisher;
        TextView txtRank;
    }

    public ArrayList<Recipe> getData() {

        data = new ArrayList<Recipe>(30);
        for (int i = 0; i < this.getCount(); i++) {
            Log.d(TEST, "getData" + this.getItem(i).getPublisher());
            data.add(this.getItem(i));
        }
        Log.d(TEST, "getData size = " + data.size());
        return data;
    }

    public synchronized void alterItem(Recipe r, int position) {

        // suppression exception when arrayAdapter was cleared and task still running
        try {
            this.remove(this.getItem(position));
            this.insert(r, position);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }
}

