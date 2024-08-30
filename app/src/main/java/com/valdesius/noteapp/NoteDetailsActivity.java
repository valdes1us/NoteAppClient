package com.valdesius.noteapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.valdesius.noteapp.helpers.ColorAdapter;
import com.valdesius.noteapp.helpers.FontColorAdapter;
import com.valdesius.noteapp.helpers.FontSizeAdapter;
import com.valdesius.noteapp.helpers.FontStyleAdapter;
import com.valdesius.noteapp.models.Note;
import com.valdesius.noteapp.models.NoteDao;
import com.valdesius.noteapp.models.NoteDatabase;

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
    private ImageView boldChange;
    private boolean isBold = false;
    private NestedScrollView nestedScrollView;
    private String currentBackgroundColor;
    private String currentFontColor;
    private float currentFontSize = 18;
    private String currentFontStyle = "Arial"; // По умолчанию

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        noteDatabase = NoteDatabase.getDatabase(this);
        noteDao = noteDatabase.noteDao();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark3));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backButton = findViewById(R.id.back_button);
        toolbarTitleEditText = findViewById(R.id.toolbarTitleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        colorChange = findViewById(R.id.color_change);
        fontChange = findViewById(R.id.font_change);
        fontStyleChange = findViewById(R.id.font_style_change); // Инициализация кнопки для изменения стиля шрифта
        nestedScrollView = findViewById(R.id.nestedScrollView);
        back();

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

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("note_id")) {
            noteId = intent.getIntExtra("note_id", -1);
            loadNote();
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
            showBackgroundColorPicker();
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
                return getResources().getColor(R.color.blue);
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

    private void saveNote() {
        String title = toolbarTitleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Введите заголовок и текст заметки", Toast.LENGTH_SHORT).show();
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


    @Override
    public void onBackPressed() {
        String content = contentEditText.getText().toString().trim();

        if (!content.isEmpty()) {
            saveNote();
        } else {
            super.onBackPressed();
        }
    }
}
