package com.valdesius.noteapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.valdesius.noteapp.helpers.NoteListRecyclerViewHelper;
import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteDao;
import com.valdesius.noteapp.models.NoteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int CREATE_NOTE_REQUEST = 1; // Код запроса для создания заметки
    public static final int EDIT_NOTE_REQUEST = 2;

    private RecyclerView noteRecyclerView;
    private NoteListRecyclerViewHelper noteListAdapter;
    private List<Note> noteList;
    private List<Note> filteredNoteList;

    private FloatingActionButton createNoteButton;
    private TextView emptyListText;
    private ImageView noteImage;

    private NoteDatabase noteDatabase;
    private NoteDao noteDao;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация базы данных
        noteDatabase = NoteDatabase.getDatabase(this);
        noteDao = noteDatabase.noteDao();

        // Загрузка заметок из базы данных
        loadNotesFromDatabase();

        // Обработка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Установка цвета статус-бара
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        // Инициализация RecyclerView и списка заметок
        noteRecyclerView = findViewById(R.id.note_list_recycler_view);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });

        // Загрузка заметок с сервера
        loadNotesFromDatabase();
        createNoteButton = findViewById(R.id.create_note_btn);
        createNote();

        emptyListText = findViewById(R.id.empty_list_text);
        noteImage = findViewById(R.id.note_image);

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
    }

    private void loadNotesFromDatabase() {
        new Thread(() -> {
            noteList = noteDao.getAllNotes();
            runOnUiThread(() -> {
                noteListAdapter = new NoteListRecyclerViewHelper(noteList, MainActivity.this);
                noteRecyclerView.setAdapter(noteListAdapter);
                updateEmptyListVisibility();
            });
        }).start();
    }

    private void filterNotes(String query) {
        if (noteListAdapter == null) {
            return; // Если адаптер не инициализирован, выходим из метода
        }

        if (query.isEmpty()) {
            filteredNoteList = noteList; // Если строка поиска пуста, показываем все заметки
        } else {
            filteredNoteList = new ArrayList<>();
            for (Note note : noteList) {
                if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        note.getContent().toLowerCase().contains(query.toLowerCase())) {
                    filteredNoteList.add(note);
                }
            }
        }
        noteListAdapter.updateNoteList(filteredNoteList);
        updateEmptyListVisibility();
    }

    private void createNote() {
        createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteDetailsActivity.class);
                startActivityForResult(intent, CREATE_NOTE_REQUEST); // Запуск активности с ожиданием результата
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_NOTE_REQUEST && resultCode == RESULT_OK) {
            loadNotesFromDatabase(); // Перезагружаем список заметок после создания новой заметки
        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            loadNotesFromDatabase(); // Перезагружаем список заметок после редактирования
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
        if (noteList.isEmpty()) {
            emptyListText.setVisibility(View.VISIBLE);
            noteImage.setVisibility(View.VISIBLE);
        } else {
            emptyListText.setVisibility(View.GONE);
            noteImage.setVisibility(View.GONE);
        }
    }
}
