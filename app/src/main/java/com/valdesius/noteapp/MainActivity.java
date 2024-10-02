package com.valdesius.noteapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    public static final int EDIT_NOTE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNightMode = preferences.getBoolean("isNightMode", true);

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        SharedPreferences preferences2 = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.contains("firstLaunchDate")) {
            String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("firstLaunchDate", currentDate);
            editor.apply();
        }
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    replaceFragment(new HomeFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_settings) {
                    replaceFragment(new SettingsFragment());
                    return true;
                }
                return false;
            }
        });

        // По умолчанию загружаем HomeFragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
