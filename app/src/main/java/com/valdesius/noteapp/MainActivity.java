package com.valdesius.noteapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.valdesius.noteapp.helpers.NoteListRecyclerViewHelper;
import com.valdesius.noteapp.models.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView noteRecyclerView;
    private NoteListRecyclerViewHelper noteListAdapter;
    private List<Note> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Заметки");

        // Обработка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Установка цвета статус бара
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        // Инициализация RecyclerView и списка заметок
        noteRecyclerView = findViewById(R.id.note_list_recycler_view);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteList = new ArrayList<>();
        noteList.add(new Note(1, "Первая заметка", "Описание первой заметки"));
        noteList.add(new Note(2, "Вторая заметка", "Описание второй заметки"));

        noteListAdapter = new NoteListRecyclerViewHelper(noteList, this);
        noteRecyclerView.setAdapter(noteListAdapter);

        // Настройка FloatingActionButton для создания
    }
}