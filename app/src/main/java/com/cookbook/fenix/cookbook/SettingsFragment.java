package com.cookbook.fenix.cookbook;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;

/**
 * Created by fenix on 02.07.2015.
 */
public class SettingsFragment extends DialogFragment implements View.OnClickListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefsEditor;

    @Override
    public void onCreate(Bundle savedInstaceState){
        super.onCreate(savedInstaceState);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInsatceState) {
        getDialog().setTitle(R.string.settings);
        View v = inflater.inflate(R.layout.dialog, null);
        RadioButton radioButton = (RadioButton) v.findViewById(R.id.radioButton);
        RadioButton radioButton1 = (RadioButton) v.findViewById(R.id.radioButton1);
        RadioButton radioButton2 = (RadioButton) v.findViewById(R.id.radioButton2);
        RadioButton radioButton3 = (RadioButton) v.findViewById(R.id.radioButton3);
        RadioButton radioButton4 = (RadioButton) v.findViewById(R.id.radioButton4);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefsEditor = sharedPreferences.edit();
        switch (sharedPreferences.getInt(getResources().getString(R.string.column_one), 1)) {
            case 1:
                radioButton.setChecked(true);
                break;
            case 2:
                radioButton1.setChecked(true);
                break;
            default:
                radioButton2.setChecked(true);
                break;
        }
        if (sharedPreferences.getBoolean(getResources().getString(R.string.Top_Rated), true)) {
            radioButton3.setChecked(true);
        } else radioButton4.setChecked(true);



        Button button = (Button) v.findViewById(R.id.button);
        radioButton.setOnClickListener(this);
        radioButton1.setOnClickListener(this);
        radioButton2.setOnClickListener(this);
        radioButton3.setOnClickListener(this);
        radioButton4.setOnClickListener(this);
        button.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.radioButton:
                prefsEditor.putInt(getResources().getString(R.string.column_one), 1);
                prefsEditor.commit();
                ((GridView)getActivity().findViewById(R.id.gridView)).setNumColumns(1);
                break;
            case R.id.radioButton1:
                prefsEditor.putInt(getResources().getString(R.string.column_one), 2);
                prefsEditor.commit();
                ((GridView)getActivity().findViewById(R.id.gridView)).setNumColumns(2);
                break;
            case R.id.radioButton2:
                prefsEditor.putInt(getResources().getString(R.string.column_one), 3);
                prefsEditor.commit();
                ((GridView)getActivity().findViewById(R.id.gridView)).setNumColumns(3);
                break;
            case R.id.radioButton3:
                prefsEditor.putBoolean(getResources().getString(R.string.Top_Rated), true);
                prefsEditor.commit();
                break;
            case R.id.radioButton4:
                prefsEditor.putBoolean(getResources().getString(R.string.Top_Rated), false);
                prefsEditor.commit();
                break;
            case R.id.button:
                dismiss();
                break;
            default:
                break;
        }

    }


    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
