package com.cookbook.fenix.cookbook;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

/**
 * Created by fenix on 02.07.2015.
 */
public class SettingsFragment extends DialogFragment implements View.OnClickListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefsEditor;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInsatceState) {
        getDialog().setTitle(R.string.settings);
        View v = inflater.inflate(R.layout.dialog, null);
        RadioButton radioButton1 = (RadioButton) v.findViewById(R.id.radioButton1);
        RadioButton radioButton2 = (RadioButton) v.findViewById(R.id.radioButton2);
        RadioButton radioButton3 = (RadioButton) v.findViewById(R.id.radioButton3);
        RadioButton radioButton4 = (RadioButton) v.findViewById(R.id.radioButton4);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefsEditor = sharedPreferences.edit();
        radioButton1.setChecked(sharedPreferences.getBoolean(getResources().getString(R.string.GridOutput), false));
        radioButton2.setChecked(sharedPreferences.getBoolean(getResources().getString(R.string.ListOutput), true));
        radioButton3.setChecked(sharedPreferences.getBoolean(getResources().getString(R.string.Top_Rated), true));
        radioButton4.setChecked(sharedPreferences.getBoolean(getResources().getString(R.string.Trending), false));
        Button button = (Button) v.findViewById(R.id.button);
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
            case R.id.radioButton1:
                prefsEditor.putBoolean(getResources().getString(R.string.GridOutput), true);
                prefsEditor.commit();
                break;
            case R.id.radioButton2:
                prefsEditor.putBoolean(getActivity().getResources().getString(R.string.ListOutput), true);
                prefsEditor.commit();
                break;
            case R.id.radioButton3:
                prefsEditor.putBoolean(getActivity().getResources().getString(R.string.Top_Rated), true);
                prefsEditor.commit();
                break;
            case R.id.radioButton4:
                prefsEditor.putBoolean(getActivity().getResources().getString(R.string.Trending), true);
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
