package com.cookbook.fenix.cookbook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * Created by fenix on 02.07.2015.
 */
public class RecipeFragment extends DialogFragment {

    private static final String TEST = "DIALOG FRAGMENT";
    private SharedPreferences sharedPreferences;
    private ImageView image;

    static RecipeFragment newInstance(Recipe obj) {
        Log.d(TEST, "Recipe = " + obj.toString());
        RecipeFragment f = new RecipeFragment();
        Bundle args = new Bundle();
        args.putParcelable("Recipe", obj);
        f.setArguments(args);
        return f;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(R.string.Food);
        final Context Theme = new ContextThemeWrapper(getActivity(), R.style.AlertDialog_AppCompat_Light);
        LayoutInflater layoutInflater = inflater.cloneInContext(Theme);
        View v = layoutInflater.inflate(R.layout.activity_dilaog, null);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        TextView title = (TextView) v.findViewById(R.id.textView);
        TextView lable = (TextView) v.findViewById(R.id.textView3);
        TextView publisher = (TextView) v.findViewById(R.id.textView1);
        image = (ImageView) v.findViewById(R.id.imageView2);
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
        TextView ingredients = (TextView) v.findViewById(R.id.addr_edittext);

        Recipe recipe = getArguments().getParcelable("Recipe");

        Log.d(TEST, "Recipe = " + recipe.toString());

        title.setText(recipe.getTitle());
        publisher.setText(recipe.getPublisher());

        //setting bitmap to image
        Downloader.setBitmapFromCache(image, recipe, null, false);

        ratingBar.setRating(Float.parseFloat(recipe.getSocialRank()));
        lable.setText("RATING = " + recipe.getSocialRank());
        Log.d(TEST, "ingredients = " + recipe.getIngredients()[0]);
        StringBuffer str = new StringBuffer();
        int i = 0;
        while (recipe.getIngredients()[i] != null) {
            str.append(recipe.getIngredients()[i++] + "\n");
            Log.d(TEST, "ingredients = " + recipe.getIngredients()[i] + "i=" + i);
        }
        ingredients.setText(str);
        return v;
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
