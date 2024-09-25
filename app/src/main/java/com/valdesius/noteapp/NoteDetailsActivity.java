package com.valdesius.noteapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.valdesius.noteapp.helpers.FontSizeAdapter;
import com.valdesius.noteapp.helpers.FontStyleAdapter;
import com.valdesius.noteapp.helpers.ListAdapter;
import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteDao;
import com.valdesius.noteapp.models.NoteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class NoteDetailsActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ImageView saveAsPdfButton;
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

    private ImageView shareButton;
    private ImageView calendarButton;
    private Calendar selectedDateTime;

    private ImageView drawingButton;

    private static final int REQUEST_CALENDAR_PERMISSION = 100;

    private void requestCalendarPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, REQUEST_CALENDAR_PERMISSION);
        }
    }

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
verifyStoragePermissions();
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

        listCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listCreate.startAnimation(scaleAnimation);
                showListPicker();
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
    }


    private static final int REQUEST_DRAWING = 400;

    private void updateDateTimeUI() {
        if (selectedDateTime != null) {
            String formattedDateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(selectedDateTime.getTime());
            Toast.makeText(NoteDetailsActivity.this, "Напоминание установлено на " + formattedDateTime, Toast.LENGTH_SHORT).show();
        }
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

    private void saveNoteAsPdf() {
        // Проверка разрешений
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            return;
        }

        // Создание документа PDF
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);

        // Добавление заголовка
        String title = toolbarTitleEditText.getText().toString().trim();
        canvas.drawText(title, 50, 50, paint);

        // Добавление содержимого
        String content = contentEditText.getText().toString().trim();
        String[] lines = content.split("\n");
        float y = 100;
        for (String line : lines) {
            canvas.drawText(line, 50, y, paint);
            y += 30;
        }

        pdfDocument.finishPage(page);

        // Сохранение документа в файл
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "note.pdf");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

        try (OutputStream outputStream = resolver.openOutputStream(uri)) {
            pdfDocument.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при сохранении PDF", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
        Toast.makeText(this, "Заметка сохранена в PDF", Toast.LENGTH_SHORT).show();
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
                saveNoteAsPdf(); // Вызов метода сохранения PDF после получения разрешения
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




}
