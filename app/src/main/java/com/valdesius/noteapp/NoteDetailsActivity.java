package com.valdesius.noteapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.valdesius.noteapp.helpers.ColorAdapter;
import com.valdesius.noteapp.helpers.FontColorAdapter;
import com.valdesius.noteapp.helpers.FontSizeAdapter;
import com.valdesius.noteapp.helpers.FontStyleAdapter;
import com.valdesius.noteapp.helpers.ListAdapter;
import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteDao;
import com.valdesius.noteapp.models.NoteDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.Manifest;
import android.content.pm.PackageManager;

public class NoteDetailsActivity extends AppCompatActivity {
    private int noteId = -1;
    private EditText toolbarTitleEditText;
    private EditText contentEditText;
    private ImageView backButton;
    private NoteDatabase noteDatabase;
    private NoteDao noteDao;
    private ImageView colorChange;
    private ImageView fontChange;
    private ImageView fontStyleChange; // Новое поле для изменения стиля шрифта
    private ImageView bgChange;
    private ImageView listCreate; // Новое поле для создания маркерованного списка
    private NestedScrollView nestedScrollView;
    private String currentBackgroundColor;
    private String currentFontColor;
    private float currentFontSize = 20;
    private String currentFontStyle = "мЗаметки"; // По умолчанию

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_SPEECH_RECOGNIZER = 300;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private ImageView voiceRecordButton;
    private boolean isRecording = false;
    private boolean isRequestingPermission = false;

    private Animation scaleAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);


        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        voiceRecordButton = findViewById(R.id.voice_record);


        noteDatabase = NoteDatabase.getDatabase(this);
        noteDao = noteDatabase.noteDao();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark3));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        backButton = findViewById(R.id.back_button);
        toolbarTitleEditText = findViewById(R.id.toolbarTitleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        colorChange = findViewById(R.id.color_change);
        fontChange = findViewById(R.id.font_change);
        fontStyleChange = findViewById(R.id.font_style_change); // Инициализация кнопки для изменения стиля шрифта
        bgChange = findViewById(R.id.bg_change);
        listCreate = findViewById(R.id.list_create); // Инициализация кнопки для создания маркерованного списка
        nestedScrollView = findViewById(R.id.nestedScrollView);
        back();

        voiceRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceRecordButton.startAnimation(scaleAnimation);
                if (isRecording) {
                    stopRecording();
                } else {
                    if (ContextCompat.checkSelfPermission(NoteDetailsActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        isRequestingPermission = true;
                        ActivityCompat.requestPermissions(NoteDetailsActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                    } else {
                        startRecording();
                    }
                }
            }
        });
        colorChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFontColorPicker();
            }
        });

        fontChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFontSizePicker();
            }
        });

        fontStyleChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFontStylePicker();
            }
        });

        bgChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBackgroundColorPicker();
            }
        });

        listCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListPicker();
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("note_id")) {
            noteId = intent.getIntExtra("note_id", -1);
            loadNote();
        }
    }

    private void startRecording() {
        audioFilePath = getExternalFilesDir(null).getAbsolutePath() + "/audio.3gp";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            startSpeechRecognition();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при начале записи", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SPEECH_RECOGNIZER) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    contentEditText.append(recognizedText + "\n");
                }
            }
            // Проверяем, идет ли запись, и останавливаем её
            if (isRecording) {
                stopRecording();
            }
        }
    }


    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
            } catch (RuntimeException stopException) {
                // Обработка исключения, если stop() вызвано в неправильном состоянии
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                Toast.makeText(this, "Ошибка при остановке записи", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите что-нибудь");
        try {
            startActivityForResult(intent, REQUEST_SPEECH_RECOGNIZER);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка распознавания речи", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isRequestingPermission) {
                    startRecording();
                    isRequestingPermission = false;
                }
            } else {
                Toast.makeText(this, "Разрешение на запись аудио не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_background_color) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void back() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void applyFontSize(float fontSize) {
        contentEditText.setTextSize(fontSize);
        toolbarTitleEditText.setTextSize(fontSize);
    }

    private void applyFontStyle(String fontStyle) {
        Typeface typeface = null;
        switch (fontStyle) {
            case "Arial":
                typeface = ResourcesCompat.getFont(this, R.font.arial);
                break;
            case "Times New Roman":
                typeface = ResourcesCompat.getFont(this, R.font.times);
                break;
            case "Courier":
                typeface = ResourcesCompat.getFont(this, R.font.courier);
                break;
            case "Verdana":
                typeface = ResourcesCompat.getFont(this, R.font.verdana);
                break;
            case "Comic Sans":
                typeface = ResourcesCompat.getFont(this, R.font.comicsans);
                break;
            case "мЗаметки":
                typeface = ResourcesCompat.getFont(this, R.font.montserratalternatesregular);
                break;
            default:
                typeface = Typeface.DEFAULT;
                break;
        }
        contentEditText.setTypeface(typeface);
        toolbarTitleEditText.setTypeface(typeface);
    }

    private int getColorForString(String colorString) {
        switch (colorString) {
            case "Белый":
                return getResources().getColor(R.color.white);
            case "Красный":
                return getResources().getColor(R.color.red);
            case "Зеленый":
                return getResources().getColor(R.color.green);
            case "Синий":
                return getResources().getColor(R.color.sin);
            case "Серый":
                return getResources().getColor(R.color.grey);
            case "Фиолетовый":
                return getResources().getColor(R.color.fiol);
            case "Оранжевый":
                return getResources().getColor(R.color.orange);
            case "Желтый":
                return getResources().getColor(R.color.yellow);
            default:
                return getResources().getColor(R.color.black);
        }
    }

    private void showBackgroundColorPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_background_color_picker, null);
        builder.setView(view);

        ListView colorList = view.findViewById(R.id.color_list);
        String[] colors = getResources().getStringArray(R.array.background_colors);

        ColorAdapter adapter = new ColorAdapter(this, android.R.layout.simple_list_item_1, colors);
        colorList.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        colorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedColor = colors[position];
                int color = getColorForString(selectedColor);
                nestedScrollView.setBackgroundColor(color);
                currentBackgroundColor = selectedColor;
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showFontColorPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_font_color_picker, null);
        builder.setView(view);

        ListView colorList = view.findViewById(R.id.font_color_list);
        String[] colors = getResources().getStringArray(R.array.font_colors);

        FontColorAdapter adapter = new FontColorAdapter(this, android.R.layout.simple_list_item_1, colors);
        colorList.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        colorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedColor = colors[position];
                int color = getColorForString(selectedColor);
                contentEditText.setTextColor(color);
                toolbarTitleEditText.setTextColor(color);
                currentFontColor = selectedColor;
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showFontSizePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_font_size_picker, null);
        builder.setView(view);

        ListView sizeList = view.findViewById(R.id.font_size_list);
        String[] sizes = getResources().getStringArray(R.array.font_sizes);

        FontSizeAdapter adapter = new FontSizeAdapter(this, android.R.layout.simple_list_item_1, sizes);
        sizeList.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        sizeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSize = sizes[position];
                float size = Float.parseFloat(selectedSize);
                currentFontSize = size;
                applyFontSize(currentFontSize);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showFontStylePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_font_style_picker, null);
        builder.setView(view);

        ListView styleList = view.findViewById(R.id.font_style_list);
        String[] styles = getResources().getStringArray(R.array.font_styles);

        FontStyleAdapter adapter = new FontStyleAdapter(this, android.R.layout.simple_list_item_1, styles);
        styleList.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        styleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedStyle = styles[position];
                currentFontStyle = selectedStyle;
                applyFontStyle(currentFontStyle);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void loadNote() {
        new Thread(() -> {
            Note note = noteDao.getNoteById(noteId);
            runOnUiThread(() -> {
                if (note != null) {
                    toolbarTitleEditText.setText(note.getTitle());
                    contentEditText.setText(note.getContent());
                    if (note.getBackgroundColor() != null) {
                        int color = getColorForString(note.getBackgroundColor());
                        nestedScrollView.setBackgroundColor(color);
                        currentBackgroundColor = note.getBackgroundColor();
                    }
                    if (note.getFontColor() != null) {
                        int color = getColorForString(note.getFontColor());
                        contentEditText.setTextColor(color);
                        toolbarTitleEditText.setTextColor(color);
                        currentFontColor = note.getFontColor();
                    }
                    if (note.getFontSize() != 0) {
                        currentFontSize = note.getFontSize();
                        applyFontSize(currentFontSize);
                    }
                    if (note.getFontStyle() != null) {
                        currentFontStyle = note.getFontStyle();
                        applyFontStyle(currentFontStyle);
                    }
                }
            });
        }).start();
    }

    private int getNoteCount() {
        int count = 0;
        try {
            count = noteDao.getNoteCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private void saveNote() {
        String title = toolbarTitleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            title = "Заметка ";
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Введите текст заметки", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note;
        if (noteId != -1) {
            note = new Note(noteId, title, content, currentBackgroundColor, currentFontColor, currentFontSize, currentFontStyle);
        } else {
            note = new Note(title, content, currentBackgroundColor, currentFontColor, currentFontSize, currentFontStyle);
        }

        new Thread(() -> {
            if (noteId != -1) {
                noteDao.update(note);
            } else {
                noteDao.insert(note);
            }
            runOnUiThread(() -> {
                Toast.makeText(NoteDetailsActivity.this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("Удаление заметки");

        // Создайте кастомный TextView
        View view = getLayoutInflater().inflate(R.layout.dialog_message, null);
        TextView messageTextView = view.findViewById(R.id.dialog_message);
        messageTextView.setText("Вы действительно хотите удалить эту заметку?");

        builder.setView(view);
        builder.setIcon(R.drawable.delete6);
        builder.setNegativeButton("Нет", null);
        builder.setPositiveButton("Да", (dialog, whichButton) -> deleteNote());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteNote() {
        if (noteId != -1) {
            new Thread(() -> {
                noteDao.delete(noteDao.getNoteById(noteId));
                runOnUiThread(() -> {
                    Toast.makeText(NoteDetailsActivity.this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }).start();
        } else {
            Toast.makeText(this, "Заметка не найдена", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        String content = contentEditText.getText().toString().trim();

        if (!content.isEmpty()) {
            saveNote();
        } else {
            super.onBackPressed();
        }
    }

    private void createBulletList() {
        String currentText = contentEditText.getText().toString();
        String[] lines = currentText.split("\n");
        StringBuilder bulletList = new StringBuilder();

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                bulletList.append("• ").append(line).append("\n");
            } else {
                bulletList.append(line).append("\n");
            }
        }

        contentEditText.setText(bulletList.toString());
    }

    private void createNumberedList() {
        String currentText = contentEditText.getText().toString();
        String[] lines = currentText.split("\n");
        StringBuilder numberedList = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].trim().isEmpty()) {
                numberedList.append(i + 1).append(". ").append(lines[i]).append("\n");
            } else {
                numberedList.append(lines[i]).append("\n");
            }
        }

        contentEditText.setText(numberedList.toString());
    }

    private void showListPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_list_picker, null);
        builder.setView(view);

        ListView listPicker = view.findViewById(R.id.style_list);
        String[] listTypes = getResources().getStringArray(R.array.list_picker);

        ListAdapter adapter = new ListAdapter(this, android.R.layout.simple_list_item_1, listTypes);
        listPicker.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        listPicker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    createBulletList();
                } else if (position == 1) {
                    createNumberedList();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
