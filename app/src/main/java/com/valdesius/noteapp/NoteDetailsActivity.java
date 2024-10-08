package com.valdesius.noteapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.valdesius.noteapp.helpers.ClickableImageSpan;
import com.valdesius.noteapp.helpers.FontSizeAdapter;
import com.valdesius.noteapp.helpers.FontStyleAdapter;
import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteDao;
import com.valdesius.noteapp.models.NoteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NoteDetailsActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private int noteId = -1;
    private EditText toolbarTitleEditText;
    private EditText contentEditText;
    private ImageView backButton;
    private NoteDatabase noteDatabase;
    private NoteDao noteDao;
    private ImageView colorChange;
    private ImageView fontChange;
    private ImageView fontStyleChange;
    private ImageView bgChange;
    private ImageView listCreate;
    private NestedScrollView nestedScrollView;
    private String currentBackgroundColor;
    private String currentFontColor;
    private float currentFontSize = 22;
    private String currentFontStyle = "мЗаметки";

    private boolean isListMode = false;
    private int listCounter = 1;


    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_SPEECH_RECOGNIZER = 300;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private ImageView voiceRecordButton;
    private boolean isRecording = false;
    private boolean isRequestingPermission = false;

    private Animation scaleAnimation;

    private ImageView shareButton;
    private ImageView calendarButton;
    private Calendar selectedDateTime;

    private static final int REQUEST_CALENDAR_PERMISSION = 100;

    private ImageView spisokButton;
    private LinearLayout todoContainer;
    private LinearLayout collapsedTodoContainer;


    private Spinner checkedItemsSpinner;
    private ArrayAdapter<String> checkedItemsAdapter;
    private List<String> checkedItemsList;
    private void resetListCounterIfNeeded() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean resetList = preferences.getBoolean("resetList", false);
        if (resetList) {
            listCounter = 1;
        }
    }
    private void requestCalendarPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, REQUEST_CALENDAR_PERMISSION);
        }
    }




    private void handleSelectedText() {
        int start = contentEditText.getSelectionStart();
        int end = contentEditText.getSelectionEnd();

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        if (start < 0 || end < 0) {
            return;
        }

        String selectedText = contentEditText.getText().toString().substring(start, end);
        if (!selectedText.isEmpty()) {
            addTodoItem(selectedText);

            // Удаляем выделенный текст из contentEditText
            contentEditText.getText().delete(start, end);

            // Устанавливаем курсор в конец текста
            contentEditText.setSelection(contentEditText.getText().length());
            contentEditText.setText("Список");
        }
    }

    private void addTodoItem(String text) {
        View todoItem = getLayoutInflater().inflate(R.layout.item_todo, null);
        EditText todoText = todoItem.findViewById(R.id.todo_text);
        ImageView deleteButton = todoItem.findViewById(R.id.delete_button);
        CheckBox checkBox = todoItem.findViewById(R.id.todo_checkbox); // Добавьте CheckBox

        todoText.setText(text);

        // Добавьте элемент списка дел в ваш контейнер (например, LinearLayout)
        LinearLayout todoContainer = findViewById(R.id.todo_container);
        todoContainer.addView(todoItem);

        // Добавляем обработчик событий для EditText
        todoText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    // Создаем новый элемент списка дел
                    createNewTodoItem(todoText);
                    contentEditText.setText("Список");
                    return true;
                }
                return false;
            }
        });

        // Добавляем обработчик событий для кнопки "Удалить"
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Удаляем элемент списка дел
                todoContainer.removeView(todoItem);

                // Устанавливаем фокус на contentEditText
                contentEditText.requestFocus();
                contentEditText.setSelection(contentEditText.getText().length());
            }
        });

        // Устанавливаем фокус на текущий элемент списка дел
        todoText.requestFocus();
        todoText.setSelection(todoText.getText().length());
    }


    private void createNewTodoItem(EditText currentTodoText) {
        String newText = currentTodoText.getText().toString();
        if (!newText.isEmpty()) {
            addTodoItem(""); // Создаем новый пустой элемент списка дел
        }
    }









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!preferences.contains("isNightMode")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isNightMode", true);
            editor.apply();
        }

        boolean isNightMode = preferences.getBoolean("isNightMode", true);
        todoContainer = findViewById(R.id.todo_container);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        updateNestedScrollViewColor();
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        voiceRecordButton = findViewById(R.id.voice_record);

        noteDatabase = NoteDatabase.getDatabase(this);
        noteDao = noteDatabase.noteDao();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark3));
        }
        verifyStoragePermissions();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        spisokButton = findViewById(R.id.spisok);
        spisokButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSelectedText();
            }
        });

        listCreate = findViewById(R.id.list_create);
        contentEditText = findViewById(R.id.contentEditText);
        if (contentEditText == null) {
            throw new IllegalStateException("contentEditText is not initialized. Check your layout file.");
        }
        listCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isListMode) {
                    isListMode = false;
                    listCreate.setImageResource(R.drawable.list_ordered_svgrepo_com2); // Иконка для выключенного режима списка
                } else {
                    isListMode = true;
                    if(!isNightMode) {
                        listCreate.setImageResource(R.drawable.list_ordered_svgrepo_com__2_);
                    }
                    else {
                        listCreate.setImageResource(R.drawable.list_ordered_svgrepo_com__1_);
                    }
                    // Иконка для включенного режима списка
                    resetListCounterIfNeeded();
                    contentEditText.append(listCounter + ". ");

                }
            }
        });

        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isListMode && s.toString().endsWith("\n")) {
                    listCounter++;
                    contentEditText.append(listCounter + ". ");
                }
            }
        });

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowInsetsController controller = getWindow().getInsetsController();
                    if (controller != null) {
                        controller.setSystemBarsAppearance(
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.grey));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        getWindow().setNavigationBarDividerColor(getResources().getColor(R.color.grey));
                    }
                    getWindow().getDecorView().setSystemUiVisibility(
                            getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    );
                }

            }
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
            }
        }

        backButton = findViewById(R.id.back_button);
        toolbarTitleEditText = findViewById(R.id.toolbarTitleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        colorChange = findViewById(R.id.color_change);
        fontChange = findViewById(R.id.font_change);
        fontStyleChange = findViewById(R.id.font_style_change); // Инициализация кнопки для изменения стиля шрифта
        bgChange = findViewById(R.id.bg_change);

        nestedScrollView = findViewById(R.id.nestedScrollView);
        back();
        setDefaultFontSizeAndStyle();

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

        shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                shareButton.startAnimation(scaleAnimation);
                shareNote();
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
                bgChange.startAnimation(scaleAnimation);
                showBackgroundColorPicker();
            }
        });



        calendarButton = findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCalendarPermissions();
                showDateTimePicker();
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("note_id")) {
            noteId = intent.getIntExtra("note_id", -1);
            loadNote();
        }

        // Установка цвета шрифта в зависимости от темы
        setFontColorBasedOnTheme();
    }















    private static final int REQUEST_DRAWING = 400;

    private void updateDateTimeUI() {
        if (selectedDateTime != null) {
            String formattedDateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(selectedDateTime.getTime());
            Toast.makeText(NoteDetailsActivity.this, "Напоминание установлено на " + formattedDateTime, Toast.LENGTH_SHORT).show();
        }
    }


    private void setDefaultFontSizeAndStyle() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defaultFontSize = preferences.getString("defaultFontSize", "22");
        String defaultFontStyle = preferences.getString("defaultFontStyle", "мЗаметки");

        currentFontSize = Float.parseFloat(defaultFontSize);
        currentFontStyle = defaultFontStyle;

        applyFontSize(currentFontSize);
        applyFontStyle(currentFontStyle);
    }
    private void addEventToCalendar(Calendar calendar) {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, calendar.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, calendar.getTimeInMillis() + 60 * 60 * 1000); // Длительность события 1 час
        values.put(CalendarContract.Events.TITLE, "Напоминание о заметке");
        values.put(CalendarContract.Events.DESCRIPTION, contentEditText.getText().toString());
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // ID календаря, обычно 1 для основного календаря
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri != null) {
            // Добавление напоминания
            long eventId = Long.parseLong(uri.getLastPathSegment());
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminderValues.put(CalendarContract.Reminders.MINUTES, 15); // Напоминание за 15 минут до события
            cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

            Toast.makeText(this, "Событие добавлено в календарь", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ошибка при добавлении события в календарь", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.AppTheme_Dark_Calendar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                showTimePicker(calendar);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(final Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.AppTheme_Dark_Calendar_Clock, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                selectedDateTime = calendar;
                addEventToCalendar(calendar);
                updateDateTimeUI();
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void shareNote() {
        // Создание скриншота
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bitmap = getScreenShot(rootView);

        // Сохранение скриншота во временный файл
        File file = saveBitmapToFile(bitmap);

        // Отправка скриншота через Intent
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Поделиться заметкой");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Поделиться через"));
    }

    private Bitmap getScreenShot(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        File file = new File(getExternalFilesDir(null), "screenshot.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
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
            case "Cataneo":
                typeface = ResourcesCompat.getFont(this, R.font.cataneo);
                break;
            case "Futuris":
                typeface = ResourcesCompat.getFont(this, R.font.futuris);
                break;
            case "Helvetika":
                typeface = ResourcesCompat.getFont(this, R.font.helvetikacmprs);
                break;
            case "Kelson":
                typeface = ResourcesCompat.getFont(this, R.font.kelson);
                break;
            default:
                typeface = Typeface.DEFAULT;
                break;
        }
        contentEditText.setTypeface(typeface);
        toolbarTitleEditText.setTypeface(typeface);
    }

    private void showBackgroundColorPicker() {
        showColorPickerDialog(true);
    }

    private void showFontColorPicker() {
        showColorPickerDialog(false);
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

    private void saveNote() {
        String title = toolbarTitleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String todoList = getTodoListAsJson(); // Преобразуйте список дел в JSON
        String todoCheckboxStates = getTodoCheckboxStatesAsJson(); // Преобразуйте состояние CheckBox в JSON

        if (title.isEmpty()) {
            title = "Заметка ";
        }

        // Проверяем, есть ли элементы в списке дел
        boolean hasTodoItems = todoContainer.getChildCount() > 0;

        if (content.isEmpty() && !hasTodoItems) {
            Toast.makeText(this, "Введите текст заметки или добавьте элементы в список дел", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note;
        if (noteId != -1) {
            note = new Note(noteId, title, content, currentBackgroundColor, currentFontColor, currentFontSize, currentFontStyle, todoList, todoCheckboxStates);
        } else {
            note = new Note(title, content, currentBackgroundColor, currentFontColor, currentFontSize, currentFontStyle, todoList, todoCheckboxStates);
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

    private String getTodoCheckboxStatesAsJson() {
        // Преобразуйте состояние CheckBox в JSON
        List<Boolean> checkboxStates = new ArrayList<>();
        for (int i = 0; i < todoContainer.getChildCount(); i++) {
            View view = todoContainer.getChildAt(i);
            CheckBox checkBox = view.findViewById(R.id.todo_checkbox);
            checkboxStates.add(checkBox.isChecked());
        }
        return new Gson().toJson(checkboxStates);
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
                    if (note.getTodoList() != null) {
                        loadTodoListFromJson(note.getTodoList());
                    }
                    if (note.getTodoCheckboxStates() != null) {
                        loadTodoCheckboxStatesFromJson(note.getTodoCheckboxStates());
                    }
                }
            });
        }).start();
    }

    private void loadTodoCheckboxStatesFromJson(String json) {
        // Загрузите состояние CheckBox из JSON
        List<Boolean> checkboxStates = new Gson().fromJson(json, new TypeToken<List<Boolean>>() {}.getType());
        for (int i = 0; i < todoContainer.getChildCount(); i++) {
            View view = todoContainer.getChildAt(i);
            CheckBox checkBox = view.findViewById(R.id.todo_checkbox);
            checkBox.setChecked(checkboxStates.get(i));
        }
    }


    private String getTodoListAsJson() {
        // Преобразуйте список дел в JSON
        List<String> todoItems = new ArrayList<>();
        for (int i = 0; i < todoContainer.getChildCount(); i++) {
            View view = todoContainer.getChildAt(i);
            EditText todoText = view.findViewById(R.id.todo_text);
            todoItems.add(todoText.getText().toString());
        }
        return new Gson().toJson(todoItems);
    }

    private void loadTodoListFromJson(String json) {
        // Загрузите список дел из JSON
        List<String> todoItems = new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
        todoContainer.removeAllViews();
        for (String item : todoItems) {
            addTodoItem(item);
        }
    }




    private void showColorPickerDialog(final boolean isBackgroundColor) {
        final int[] selectedColor = {Color.BLACK};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите цвет");

        View view = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        final SeekBar redSeekBar = view.findViewById(R.id.redSeekBar);
        final SeekBar greenSeekBar = view.findViewById(R.id.greenSeekBar);
        final SeekBar blueSeekBar = view.findViewById(R.id.blueSeekBar);

        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedColor[0] = Color.rgb(progress, greenSeekBar.getProgress(), blueSeekBar.getProgress());
                updateColorPreview(view, selectedColor[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedColor[0] = Color.rgb(redSeekBar.getProgress(), progress, blueSeekBar.getProgress());
                updateColorPreview(view, selectedColor[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedColor[0] = Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), progress);
                updateColorPreview(view, selectedColor[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isBackgroundColor) {
                    nestedScrollView.setBackgroundColor(selectedColor[0]);
                    currentBackgroundColor = "#" + Integer.toHexString(selectedColor[0]);
                } else {
                    contentEditText.setTextColor(selectedColor[0]);
                    toolbarTitleEditText.setTextColor(selectedColor[0]);
                    currentFontColor = "#" + Integer.toHexString(selectedColor[0]);
                }
            }
        });
        builder.setNegativeButton("Отмена", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateColorPreview(View view, int color) {
        View colorPreview = view.findViewById(R.id.colorPreview);
        colorPreview.setBackgroundColor(color);
    }

    private int getColorForString(String colorString) {
        try {
            return Color.parseColor(colorString);
        } catch (IllegalArgumentException e) {
            return getResources().getColor(R.color.black);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("Удаление заметки");

        // Создайте кастомный TextView
        View view = getLayoutInflater().inflate(R.layout.dialog_message, null);
        TextView messageTextView = view.findViewById(R.id.dialog_message);
        messageTextView.setText("Вы действительно хотите удалить эту заметку?");

        builder.setView(view);
        builder.setIcon(R.drawable.trash_bin_minimalistic_svgrepo_com);
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









    private void verifyStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // На Android 10 и выше не нужно запрашивать разрешения на чтение/запись
            return;
        }

        int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено

            } else {
                Toast.makeText(this, "Разрешение на доступ к хранилищу не предоставлено", Toast.LENGTH_SHORT).show();
                // Повторный запрос разрешений или уведомление пользователя
                showPermissionDeniedDialog();
            }
        }
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

        if (requestCode == REQUEST_CALENDAR_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено
            } else {
                Toast.makeText(this, "Разрешение на доступ к календарю не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Разрешение на доступ к хранилищу");
        builder.setMessage("Для сохранения заметок в PDF необходимо предоставить разрешение на доступ к хранилищу. Пожалуйста, предоставьте разрешение в настройках приложения.");
        builder.setPositiveButton("Настройки", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNestedScrollViewColor();
    }

    private void updateNestedScrollViewColor() {
        if (nestedScrollView != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            if (isNightMode) {
                nestedScrollView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark3));
            } else {
                nestedScrollView.setBackgroundColor(getResources().getColor(R.color.grey2));
            }
        }
        updateToolbarColor();
        updateToolbarBottomColor();
    }

    private void updateToolbarColor() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            if (isNightMode) {
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            } else {
                toolbar.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        }
    }

    private void updateToolbarBottomColor() {
        RelativeLayout toolbarBottom = findViewById(R.id.toolbar_bottom);
        if (toolbarBottom != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean isNightMode = preferences.getBoolean("isNightMode", false);

            if (isNightMode) {
                toolbarBottom.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            } else {
                toolbarBottom.setBackgroundColor(getResources().getColor(android.R.color.white));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.greyy));
                }
            }
        }
    }

    private void setFontColorBasedOnTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isNightMode = preferences.getBoolean("isNightMode", false);

        if (isNightMode) {
            // Устанавливаем белый цвет шрифта для темной темы
            contentEditText.setTextColor(Color.WHITE);
            toolbarTitleEditText.setTextColor(Color.WHITE);
        } else {
            // Устанавливаем черный цвет шрифта для светлой темы
            contentEditText.setTextColor(Color.BLACK);
            toolbarTitleEditText.setTextColor(Color.BLACK);
        }
    }


}
