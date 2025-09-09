package com.example.mykeyboard;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;

import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}