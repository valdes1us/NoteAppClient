package com.valdesius.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesius.noteapp.helpers.NoteListRecyclerViewHelper;
import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteApi;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView noteRecyclerView;
    private NoteListRecyclerViewHelper noteListAdapter;
    private List<Note> noteList;
    private Retrofit retrofit;
    private NoteApi noteApi;
    private FloatingActionButton createNoteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Заметки");
        toolbar.setNavigationIcon(R.drawable.note); // Установка иконки

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
    }


    private void loadNotesFromServer() {
        Call<List<Note>> call = noteApi.getAllNotes();
        call.enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful()) {
                    noteList = response.body();
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
                startActivity(intent);
            }
        });
    }
}
