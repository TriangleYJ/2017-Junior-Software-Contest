package com.yjprojects.jsctest2.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.yjprojects.jsctest2.R;
import com.yjprojects.jsctest2.SeekBarPreference;
import com.yjprojects.jsctest2.User;

/**
 * Created by jyj on 2017-10-03.
 */

public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    Toolbar toolbar;
    private SeekBarPreference seekBarPreference1;
    private SeekBarPreference seekBarpreference2;
    private EditTextPreference editTextPreference;
    private ListPreference listPreference;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar.setTitle("Setting");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.st_coord, new MyPreferenceFragment()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.setting_main, new LinearLayout(this), false);

        toolbar = (Toolbar) contentView.findViewById(R.id.st_toolbar);

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.st_coord);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

        getWindow().setContentView(contentView);
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

        }
    }




    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals("density")) {
            int result = PreferenceManager.getDefaultSharedPreferences(this).getInt("density", 50);
            int density = 2000 - result * 10;
            User.setDensity(density);
        }

        if(key.equals("quality")) {
            int result = PreferenceManager.getDefaultSharedPreferences(this).getInt("quality", 750);
            if(result == 0) result = 1;
            User.setQuality(result);
        }

        if(key.equals("username")) {
            String result = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "코타나");
            User.setName(result);
        }

        if(key.equals("mode")){
            int result = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("mode", "0"));
            User.setMode(result);

        }
    }


}
