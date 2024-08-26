package com.valdesius.noteapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NoteDetailsActivity extends AppCompatActivity {
    private int noteId = -1;
    private EditText toolbarTitleEditText;
    private EditText contentEditText;

    private Retrofit retrofit;
    private NoteApi noteApi;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark3));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Добавляем кнопку "Назад" в тулбар

        backButton = findViewById(R.id.back_button);
        toolbarTitleEditText = findViewById(R.id.toolbarTitleEditText);
        contentEditText = findViewById(R.id.contentEditText);

        back();
        // Настройка Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.35:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        noteApi = retrofit.create(NoteApi.class);

        // Получение данных о заметке
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("note_id")) {
            noteId = intent.getIntExtra("note_id", -1);
            toolbarTitleEditText.setText(intent.getStringExtra("title"));
            contentEditText.setText(intent.getStringExtra("content"));
        }

    }


    private void back() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed(); // Вызываем onBackPressed для сохранения заметки перед возвратом
            }
        });
    }

    private void saveNote() {
        String title = toolbarTitleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Введите заголовок и текст заметки", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(noteId, title, content);

        if (noteId != -1) {
            // Обновление заметки
            Call<Note> call = noteApi.updateNote(noteId, note);
            call.enqueue(new Callback<Note>() {
                @Override
                public void onResponse(Call<Note> call, Response<Note> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(NoteDetailsActivity.this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish(); // Закрываем активность после сохранения
                    } else {
                        Toast.makeText(NoteDetailsActivity.this, "Ошибка при обновлении заметки", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Note> call, Throwable t) {
                    Log.e("NoteDetailsActivity", "Ошибка: " + t.getMessage());
                    Toast.makeText(NoteDetailsActivity.this, "Ошибка при соединении с сервером", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Создание новой заметки
            Call<Note> call = noteApi.createNote(note);
            call.enqueue(new Callback<Note>() {
                @Override
                public void onResponse(Call<Note> call, Response<Note> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(NoteDetailsActivity.this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish(); // Закрываем активность после сохранения
                    } else {
                        Toast.makeText(NoteDetailsActivity.this, "Ошибка при сохранении заметки", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Note> call, Throwable t) {
                    Log.e("NoteDetailsActivity", "Ошибка: " + t.getMessage());
                    Toast.makeText(NoteDetailsActivity.this, "Ошибка при соединении с сервером", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        String content = contentEditText.getText().toString().trim();

        if (!content.isEmpty()) {
            saveNote(); // Сохранить заметку, если есть текст
        } else {
            super.onBackPressed(); // Просто выйти, если текста нет
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Обработка нажатия кнопки "Назад" на тулбаре
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
