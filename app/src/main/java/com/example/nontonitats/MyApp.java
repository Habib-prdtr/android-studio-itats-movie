package com.example.nontonitats;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        boolean isDark = getSharedPreferences("settings", MODE_PRIVATE)
                .getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES :
                        AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
