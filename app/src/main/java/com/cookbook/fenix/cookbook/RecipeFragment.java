package com.cookbook.fenix.cookbook;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by fenix on 02.07.2015.
 */
public class RecipeFragment extends DialogFragment {

    private static final String TEST = "DIALOG FRAGMENT";
    private SharedPreferences sharedPreferences;

    static RecipeFragment newInstance(Recipe obj) {
        Log.d(TEST, "Recipe = " + obj.toString());
        RecipeFragment f = new RecipeFragment();
        Bundle args = new Bundle();
        args.putSerializable("Recipe", obj);
        f.setArguments(args);
        return f;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(R.string.settings);
        View v = inflater.inflate(R.layout.activity_dilaog, null);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        TextView title = (TextView) v.findViewById(R.id.textView);
        TextView lable = (TextView) v.findViewById(R.id.textView3);
        TextView publisher = (TextView) v.findViewById(R.id.textView1);
        ImageView image = (ImageView) v.findViewById(R.id.imageView2);
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
        TextView ingredients = (TextView) v.findViewById(R.id.addr_edittext);

        Recipe recipe = (Recipe) getArguments().getSerializable("Recipe");
        ;
        Log.d(TEST, "Recipe = " + recipe.toString());

        title.setText(recipe.getTitle());
        publisher.setText(recipe.getPublisher());
        Log.d(TEST, "Recipe = " + recipe.getBmp().toString());
        image.setImageBitmap(recipe.getBmp());
        ratingBar.setRating(Float.parseFloat(recipe.getSocialRank()));
        lable.setText("Рейтинг = " + recipe.getSocialRank());
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
