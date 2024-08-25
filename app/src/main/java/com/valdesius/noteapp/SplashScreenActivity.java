package com.valdesius.noteapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreenActivity extends AppCompatActivity {
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark2));
        // Получаем SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        boolean isFirstLaunch = preferences.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            // Показываем сплэш-скрин
            setContentView(R.layout.activity_splash_screen);

            // Сохраняем, что сплэш-скрин был показан
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstLaunch", false);
            editor.apply();

            // Ожидаем некоторое время и переходим на основной экран


        } else {
            // Сплэш-скрин был уже показан, сразу переходим на основной экран
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            finish();
        }
        startButton = findViewById(R.id.startButton);
        goToMainActivity();
    }

    private void goToMainActivity() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}