package com.valdesius.noteapp;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.valdesius.noteapp.helpers.CustomSpinnerAdapter;
import com.valdesius.noteapp.models.Note;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment {
    private TextView textToolbar;
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;
    private TextView emptyListText;
    private NestedScrollView nestedScrollView;
    private Switch toggleThemeSwitch;
    private TextView toggleThemeText;

    private TextView firstLaunchDateTextView;
    private TextView notesCountTextView;
    private Switch resetListSwitch;
    private TextView resetText;

    private Spinner fontSizeSpinner;
    private Spinner fontStyleSpinner;
    private TextView fontSizeText;
    private TextView fontStyleText;

    private TextView textVersion;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!preferences.contains("isNightMode")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isNightMode", true);
            editor.apply();
        }

        textVersion = view.findViewById(R.id.veer);
        resetListSwitch = view.findViewById(R.id.resetListSwitch);
        resetText = view.findViewById(R.id.resetListText);
        SharedPreferences preferences3 = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean resetList = preferences.getBoolean("resetList", false);
        resetListSwitch.setChecked(resetList);
        resetListSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences3.edit();
            editor.putBoolean("resetList", isChecked);
            editor.apply();
        });
        firstLaunchDateTextView = view.findViewById(R.id.firstLaunchDateTextView);
        notesCountTextView = view.findViewById(R.id.notesCountTextView);
        String firstLaunchDate = preferences.getString("firstLaunchDate", "Неизвестно");
        firstLaunchDateTextView.setText("Вы начали использовать мЗаметки: " + firstLaunchDate);

        NoteViewModel noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                notesCountTextView.setText("Количество заметок: " + notes.size());
            }
        });


        boolean isNightMode = preferences.getBoolean("isNightMode", true);
        emptyListText = getActivity().findViewById(R.id.empty_list_text);
        toggleThemeSwitch = view.findViewById(R.id.toggleThemeSwitch);
        toggleThemeText = view.findViewById(R.id.toggleThemeText);
        toggleThemeSwitch.setChecked(isNightMode);
        toggleThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("isNightMode", true);
                toggleThemeSwitch.setTextColor(getResources().getColor(R.color.white));
                firstLaunchDateTextView.setTextColor(getResources().getColor(R.color.white));
                notesCountTextView.setTextColor(getResources().getColor(R.color.white));
                toggleThemeText.setTextColor(getResources().getColor(R.color.white));
                resetText.setTextColor(getResources().getColor(R.color.white));
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("isNightMode", false);
                toggleThemeSwitch.setTextColor(getResources().getColor(R.color.black));
                firstLaunchDateTextView.setTextColor(getResources().getColor(R.color.black));
                notesCountTextView.setTextColor(getResources().getColor(R.color.black));
                toggleThemeText.setTextColor(getResources().getColor(R.color.black));
                resetText.setTextColor(getResources().getColor(R.color.black));
            }


            ObjectAnimator animator = ObjectAnimator.ofFloat(toggleThemeSwitch, "rotationX", 0f, 360f);
            animator.setDuration(500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
            editor.apply();
            updateBottomNavigationView();
            updateFragmentContainerBackground();
            updateToolbarColor();
            updateNestedScrollViewColor();
            getActivity().recreate();
        });

        textToolbar = getActivity().findViewById(R.id.toolbar_title); // Initialize textToolbar here
        searchView = getActivity().findViewById(R.id.search_view); // Initialize searchView here
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation); // Initialize bottomNavigationView here
        fragmentContainer = getActivity().findViewById(R.id.fragment_container); // Initialize fragmentContainer here
        nestedScrollView = getActivity().findViewById(R.id.nestedScrollView); // Initialize nestedScrollView here
        // Инициализация Spinner для размера шрифта
        fontSizeSpinner = view.findViewById(R.id.fontSizeSpinner);
        ArrayAdapter<CharSequence> fontSizeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.font_sizes, android.R.layout.simple_spinner_item);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSizeSpinner.setAdapter(fontSizeAdapter);
        fontSizeSpinner.setSelection(fontSizeAdapter.getPosition(preferences.getString("defaultFontSize", "22")));
        fontSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFontSize = fontSizeAdapter.getItem(position).toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("defaultFontSize", selectedFontSize);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        fontSizeText = view.findViewById(R.id.fontSizeText);
        fontStyleText = view.findViewById(R.id.fontStyleText);
        // Инициализация Spinner для стиля шрифта
        fontStyleSpinner = view.findViewById(R.id.fontStyleSpinner);
        ArrayAdapter<CharSequence> fontStyleAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.font_styles, android.R.layout.simple_spinner_item);
        fontStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontStyleSpinner.setAdapter(fontStyleAdapter);
        fontStyleSpinner.setSelection(fontStyleAdapter.getPosition(preferences.getString("defaultFontStyle", "мЗаметки")));
        fontStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFontStyle = fontStyleAdapter.getItem(position).toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("defaultFontStyle", selectedFontStyle);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToolbarColor();
        updateSearchViewIcon();
        updateBottomNavigationView();
        updateFragmentContainerBackground();
        updateNestedScrollViewColor();
        restoreSpinnerStates();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveSpinnerStates();
    }

    private void updateToolbarColor() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            int textColor = isNightMode ? getResources().getColor(R.color.white) : getResources().getColor(R.color.black);
            int backgroundColor = isNightMode ? getResources().getColor(R.color.colorPrimaryDark3) : getResources().getColor(R.color.grey3);

            if (isNightMode) {
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                if (textToolbar != null) {
                    textToolbar.setTextColor(getResources().getColor(R.color.white)); // Set text color to white in night mode
                    toggleThemeSwitch.setTextColor(getResources().getColor(android.R.color.white));
                    firstLaunchDateTextView.setTextColor(getResources().getColor(R.color.white));
                    notesCountTextView.setTextColor(getResources().getColor(R.color.white));
                    toggleThemeText.setTextColor(getResources().getColor(R.color.white));
                    resetText.setTextColor(getResources().getColor(R.color.white));
                    fontStyleText.setTextColor(getResources().getColor(R.color.white));
                    fontSizeText.setTextColor(getResources().getColor(R.color.white));
                    textVersion.setTextColor(getResources().getColor(R.color.white));
                }
            } else {
                toolbar.setBackgroundColor(getResources().getColor(android.R.color.white));
                firstLaunchDateTextView.setTextColor(getResources().getColor(R.color.black));
                notesCountTextView.setTextColor(getResources().getColor(R.color.black));
                toggleThemeText.setTextColor(getResources().getColor(R.color.black));
                resetText.setTextColor(getResources().getColor(R.color.black));
                fontStyleText.setTextColor(getResources().getColor(R.color.black));
                fontSizeText.setTextColor(getResources().getColor(R.color.black));
                textVersion.setTextColor(getResources().getColor(R.color.black));

                if (textToolbar != null) {
                    textToolbar.setTextColor(getResources().getColor(R.color.black)); // Set text color to black in day mode
                }
            }

            // Update Spinner text color and background color
            String[] fontSizeItems = getResources().getStringArray(R.array.font_sizes);
            String[] fontStyleItems = getResources().getStringArray(R.array.font_styles);

            CustomSpinnerAdapter fontSizeAdapter = new CustomSpinnerAdapter(getActivity(), Arrays.asList(fontSizeItems), textColor, backgroundColor);
            CustomSpinnerAdapter fontStyleAdapter = new CustomSpinnerAdapter(getActivity(), Arrays.asList(fontStyleItems), textColor, backgroundColor);

            Spinner fontSizeSpinner = getActivity().findViewById(R.id.fontSizeSpinner);
            Spinner fontStyleSpinner = getActivity().findViewById(R.id.fontStyleSpinner);

            fontSizeSpinner.setAdapter(fontSizeAdapter);
            fontStyleSpinner.setAdapter(fontStyleAdapter);

            restoreSpinnerStates();
        }
    }

    private void saveSpinnerStates() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();

        Spinner fontSizeSpinner = getActivity().findViewById(R.id.fontSizeSpinner);
        Spinner fontStyleSpinner = getActivity().findViewById(R.id.fontStyleSpinner);

        if (fontSizeSpinner != null) {
            editor.putInt("fontSizePosition", fontSizeSpinner.getSelectedItemPosition());
        }
        if (fontStyleSpinner != null) {
            editor.putInt("fontStylePosition", fontStyleSpinner.getSelectedItemPosition());
        }

        editor.apply();
    }


    private void restoreSpinnerStates() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Spinner fontSizeSpinner = getActivity().findViewById(R.id.fontSizeSpinner);
        Spinner fontStyleSpinner = getActivity().findViewById(R.id.fontStyleSpinner);

        if (fontSizeSpinner != null) {
            int fontSizePosition = preferences.getInt("fontSizePosition", 0);
            fontSizeSpinner.setSelection(fontSizePosition);
        }
        if (fontStyleSpinner != null) {
            int fontStylePosition = preferences.getInt("fontStylePosition", 0);
            fontStyleSpinner.setSelection(fontStylePosition);
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

                    // Устанавливаем цвет текста на статус-баре в темном режиме (по умолчанию белый)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(
                                0,
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        );
                    }
                }
            } else {
                fragmentContainer.setBackgroundColor(getResources().getColor(R.color.grey2));
                if (emptyListText != null) {
                    emptyListText.setTextColor(getResources().getColor(R.color.black));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(android.R.color.white));

                    // Устанавливаем цвет текста на статус-баре в светлом режиме
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        );
                    }
                }
            }
        }
    }

    private void updateNestedScrollViewColor() {
        if (nestedScrollView != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            if (isNightMode) {
                nestedScrollView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark3));
            } else {
                nestedScrollView.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        }
    }
}
