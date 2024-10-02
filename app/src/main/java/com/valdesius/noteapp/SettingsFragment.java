package com.valdesius.noteapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Field;

public class SettingsFragment extends Fragment {
    private TextView textToolbar;
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private RelativeLayout homeBLayout;
    private FrameLayout fragmentContainer;
    private TextView emptyListText;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isNightMode = preferences.getBoolean("isNightMode", false);

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        emptyListText = getActivity().findViewById(R.id.empty_list_text);
        Button toggleThemeButton = view.findViewById(R.id.toggleThemeButton);
        toggleThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();

                switch (currentNightMode) {
                    case android.content.res.Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        editor.putBoolean("isNightMode", true);
                        break;
                    case android.content.res.Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        editor.putBoolean("isNightMode", false);
                        break;
                }
                editor.apply();
                updateBottomNavigationView();
                updateFragmentContainerBackground();
                getActivity().recreate();
            }
        });

        textToolbar = getActivity().findViewById(R.id.toolbar_title); // Initialize textToolbar here
        searchView = getActivity().findViewById(R.id.search_view); // Initialize searchView here
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation); // Initialize bottomNavigationView here
        fragmentContainer = getActivity().findViewById(R.id.fragment_container); // Initialize fragmentContainer here


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToolbarColor();
        updateSearchViewIcon();
        updateBottomNavigationView();
        updateFragmentContainerBackground();
    }

    private void updateToolbarColor() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            if (isNightMode) {
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                if (textToolbar != null) {
                    textToolbar.setTextColor(getResources().getColor(R.color.white)); // Set text color to white in night mode
                }
            } else {
                toolbar.setBackgroundColor(getResources().getColor(android.R.color.white));
                if (textToolbar != null) {
                    textToolbar.setTextColor(getResources().getColor(R.color.black)); // Set text color to black in day mode
                }
            }
        }
    }

    private void updateSearchViewIcon() {
        if (searchView != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            int searchIconResId = isNightMode
                    ? R.drawable.search_svgrepo_com__1_
                    : R.drawable.search_svgrepo_com;

            try {
                Field searchField = SearchView.class.getDeclaredField("mSearchButton");
                searchField.setAccessible(true);
                Object searchButton = searchField.get(searchView);
                if (searchButton != null && searchButton instanceof ImageView) {
                    ((ImageView) searchButton).setImageDrawable(getResources().getDrawable(searchIconResId));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBottomNavigationView() {
        if (bottomNavigationView != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            if (isNightMode) {
                bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.white));
                bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.white));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            } else {
                bottomNavigationView.setBackgroundColor(getResources().getColor(android.R.color.white));
                bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.black));
                bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.black));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.greyy));
                }
            }
        }
    }

    private void updateFragmentContainerBackground() {
        if (fragmentContainer != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            if (isNightMode) {
                fragmentContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark3));
                if (emptyListText != null) {
                    emptyListText.setTextColor(getResources().getColor(R.color.white));
                }
            } else {
                fragmentContainer.setBackgroundColor(getResources().getColor(R.color.grey2));
                if (emptyListText != null) {
                    emptyListText.setTextColor(getResources().getColor(R.color.black));
                }
            }
        }
    }
}