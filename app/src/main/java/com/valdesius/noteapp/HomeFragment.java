package com.valdesius.noteapp;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.valdesius.noteapp.helpers.NoteListRecyclerViewHelper;
import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteDao;
import com.valdesius.noteapp.models.NoteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnThemeChangeListener{

    private static final int CREATE_NOTE_REQUEST = 1; // Код запроса для создания заметки
    public static final int EDIT_NOTE_REQUEST = 2;

    private RecyclerView noteRecyclerView;
    private NoteListRecyclerViewHelper noteListAdapter;
    private List<Note> noteList;
    private List<Note> filteredNoteList;

    private FloatingActionButton createNoteButton;
    private TextView emptyListText;
    private ImageView noteImage;
    private SearchView searchView; // Добавьте SearchView

    private NoteDatabase noteDatabase;
    private NoteDao noteDao;
    private NoteViewModel noteViewModel;

    private RelativeLayout homeBLayout;

    private TextView textToolbar;

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!preferences.contains("isNightMode")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isNightMode", true);
            editor.apply();
        }

        boolean isNightMode = preferences.getBoolean("isNightMode", true);
        // Инициализация базы данных
        noteDatabase = NoteDatabase.getDatabase(requireContext());
        noteDao = noteDatabase.noteDao();

        // Инициализация RecyclerView и списка заметок
        noteRecyclerView = view.findViewById(R.id.note_list_recycler_view);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        noteListAdapter = new NoteListRecyclerViewHelper(new ArrayList<>(), requireContext());
        noteRecyclerView.setAdapter(noteListAdapter);

        // Инициализация ViewModel и наблюдение за изменениями данных
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                noteList = notes;
                noteListAdapter.updateNoteList(noteList);
                updateEmptyListVisibility();
            }
        });

        // Обработка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main2), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createNoteButton = view.findViewById(R.id.create_note_btn);
        createNote();

        emptyListText = view.findViewById(R.id.empty_list_text);
        noteImage = view.findViewById(R.id.note_image);

        textToolbar = getActivity().findViewById(R.id.toolbar_title); // Initialize textToolbar here
        searchView = getActivity().findViewById(R.id.search_view); // Initialize searchView here
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation); // Initialize bottomNavigationView here
        fragmentContainer = getActivity().findViewById(R.id.fragment_container); // Initialize fragmentContainer here

        // Инициализация homeBLayout
        homeBLayout = view.findViewById(R.id.homeB);
        if (homeBLayout == null) {
            Log.e("HomeFragment", "homeBLayout is null after initialization");
        }

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Не обрабатываем перемещения
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Note note = noteList.get(position);
                deleteNoteWithUndo(note, position);  // Изменено для поддержки undo
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                // Определение фона и иконки
                View itemView = viewHolder.itemView;
                Paint p = new Paint();
                p.setColor(getResources().getColor(R.color.red)); // Цвет фона при смахивании

                // Создаем закругленный прямоугольник
                float cornerRadius = 75f; // Радиус закругления
                float left = (float) itemView.getRight() + dX - 100; // Смещение для удлинения прямоугольника
                RectF background = new RectF(left, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom());
                Path path = new Path();
                path.addRoundRect(background, cornerRadius, cornerRadius, Path.Direction.CW);

                // Рисуем закругленный фон
                if (dX < 0) {
                    c.drawPath(path, p);

                    // Добавление текста "Удалить"
                    p.setColor(Color.WHITE); // Цвет текста
                    p.setTextSize(60); // Размер текста
                    p.setTextAlign(Paint.Align.CENTER); // Центрирование текста
                    float textX = (background.left + background.right) / 2.3F; // Положение текста по оси X
                    float textY = (background.top + background.bottom) / 2 - ((p.descent() + p.ascent()) / 2); // Положение текста по оси Y
                    c.drawText("Удалить", textX, textY, p);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(noteRecyclerView);

        return view;
    }


    private void createNote() {
        createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), NoteDetailsActivity.class);
                startActivityForResult(intent, CREATE_NOTE_REQUEST); // Запуск активности с ожиданием результата
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_NOTE_REQUEST && resultCode == RESULT_OK) {
            // Перезагружаем список заметок после создания новой заметки
        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            // Перезагружаем список заметок после редактирования
        }
    }

    @Override
    public void onThemeChanged(int nightMode) {
        if (emptyListText != null) {
            if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                emptyListText.setTextColor(getResources().getColor(R.color.white));
            } else {
                emptyListText.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }





    public void deleteNoteWithUndo(Note note, int position) {
        // Удаляем заметку из списка и обновляем адаптер
        noteList.remove(position);
        noteListAdapter.notifyItemRemoved(position);

        // Показываем Snackbar с возможностью отмены
        Snackbar snackbar = Snackbar.make(noteRecyclerView, "Заметка удалена", Snackbar.LENGTH_LONG)
                .setAction("ОТМЕНИТЬ", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Отмена удаления, возвращаем заметку на прежнее место
                        noteList.add(position, note);
                        noteListAdapter.notifyItemInserted(position);
                        updateEmptyListVisibility();
                    }
                });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    // Если действие не было отменено, выполняем удаление из базы данных
                    deleteNoteFromDatabase(note);
                    updateEmptyListVisibility();
                }
            }
        });

        snackbar.show();
    }

    private void deleteNoteFromDatabase(Note note) {
        new Thread(() -> {
            noteDao.delete(note);
        }).start();
    }

    private void updateEmptyListVisibility() {
        if (noteList == null || noteList.isEmpty()) {
            emptyListText.setVisibility(View.VISIBLE);
            noteImage.setVisibility(View.VISIBLE);
        } else {
            emptyListText.setVisibility(View.GONE);
            noteImage.setVisibility(View.GONE);
        }
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

                    // Устанавливаем цвет текста на статус-баре в темном режиме (по умолчанию белый)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(
                                0,
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        );
                    }
                }
            } else {
                bottomNavigationView.setBackgroundColor(getResources().getColor(android.R.color.white));
                bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.black));
                bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.black));
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
