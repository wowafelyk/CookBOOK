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


/**
 * Created by fenix on 01.07.2015.
 */
public class RecipeAdapter extends ArrayAdapter<Recipe> {

    public final String TEST = "RECIPE Adapter TEST";

    private Context context;
    private int layoutResourceId;
    private Recipe[] data = null;

    public RecipeAdapter(Context context, int layoutResourceId, Recipe[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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

        Log.d(TEST, holder.toString() + "who are you");
        Log.d(TEST, holder.txtTitle.toString() + "who are you");

        Recipe recipe = data[position];

        Log.d(TEST, (data[0]).getPublisher() + "1 check");

        Log.d(TEST, recipe.getTitle() + " 2 check");


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

    public Recipe[] getData() {
        return data;
    }
}

