package com.valdesius.noteapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingActivity extends AppCompatActivity {
    private CanvasView canvasView;
    private Button saveButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        canvasView = findViewById(R.id.canvasView);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDrawing();
            }
        });
    }

    private void saveDrawing() {
        Bitmap bitmap = canvasView.getBitmap();
        File file = new File(getExternalFilesDir(null), "drawing.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("drawing_path", file.getAbsolutePath());
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
