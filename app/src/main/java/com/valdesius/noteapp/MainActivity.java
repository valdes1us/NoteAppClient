package com.valdesius.noteapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.valdesius.noteapp.models.NoteApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int CREATE_NOTE_REQUEST = 1; // Код запроса для создания заметки
    public static final int EDIT_NOTE_REQUEST = 2;

    private RecyclerView noteRecyclerView;
    private NoteListRecyclerViewHelper noteListAdapter;
    private List<Note> noteList;
    private List<Note> filteredNoteList;
    private Retrofit retrofit;
    private NoteApi noteApi;
    private FloatingActionButton createNoteButton;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                return false; // Можно оставить пустым, если не используем отправку
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });



        // Настройка Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.35:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        noteApi = retrofit.create(NoteApi.class);

        // Загрузка заметок с сервера
        loadNotesFromServer();

        createNoteButton = findViewById(R.id.create_note_btn);
        createNote();

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

    private void filterNotes(String query) {
        if (query.isEmpty()) {
            filteredNoteList = noteList; // Если строка поиска пуста, показываем все заметки
        } else {
            filteredNoteList = new ArrayList<>();
            for (Note note : noteList) {
                if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        note.getBody().toLowerCase().contains(query.toLowerCase())) {
                    filteredNoteList.add(note);
                }
            }
        }
        noteListAdapter.updateNoteList(filteredNoteList);
    }
    private void loadNotesFromServer() {
        Call<List<Note>> call = noteApi.getAllNotes();
        call.enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful()) {
                    noteList = response.body();

                    // Проверка, пустой ли список заметок
                    if (noteList != null && !noteList.isEmpty()) {
                        // Скрыть изображение и текст о пустом списке
                        findViewById(R.id.note_image).setVisibility(View.GONE);
                        findViewById(R.id.empty_list_text).setVisibility(View.GONE);
                    } else {
                        // Показать изображение и текст, если список пуст
                        findViewById(R.id.note_image).setVisibility(View.VISIBLE);
                        findViewById(R.id.empty_list_text).setVisibility(View.VISIBLE);
                    }

                    noteListAdapter = new NoteListRecyclerViewHelper(noteList, MainActivity.this);
                    noteRecyclerView.setAdapter(noteListAdapter);
                } else {
                    Toast.makeText(MainActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                Log.e("RetrofitError", t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Ошибка подключения к серверу: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
            loadNotesFromServer(); // Перезагружаем список заметок после создания новой заметки
        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            loadNotesFromServer(); // Перезагружаем список заметок после редактирования
        }
    }

    private void deleteNoteWithUndo(Note note, int position) {
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

                        // Прячем сообщение о пустом списке, если оно появилось
                        findViewById(R.id.empty_list_text).setVisibility(View.GONE);
                        findViewById(R.id.note_image).setVisibility(View.GONE);
                    }
                });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    // Если действие не было отменено, выполняем удаление с сервера
                    deleteNoteFromServer(note.getNote_id(), position);
                }
            }
        });

        snackbar.show();
    }

    private void deleteNoteFromServer(int noteId, int position) {
        Call<Void> call = noteApi.deleteNote(noteId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Заметка удалена", Toast.LENGTH_SHORT).show();

                    // Проверка на пустоту списка заметок после удаления
                    if (noteList.isEmpty()) {
                        // Показать сообщение и изображение, что список заметок пуст
                        findViewById(R.id.empty_list_text).setVisibility(View.VISIBLE);
                        findViewById(R.id.note_image).setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Ошибка удаления заметки", Toast.LENGTH_SHORT).show();
                    noteListAdapter.notifyItemChanged(position); // Восстанавливаем элемент, если ошибка
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("RetrofitError", t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Ошибка подключения к серверу: " + t.getMessage(), Toast.LENGTH_LONG).show();
                noteListAdapter.notifyItemChanged(position); // Восстанавливаем элемент, если ошибка
            }
        });
    }

}
