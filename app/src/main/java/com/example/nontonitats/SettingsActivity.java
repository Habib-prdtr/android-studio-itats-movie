package com.example.nontonitats;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.nontonitats.api.ApiClient;
import com.example.nontonitats.api.ApiService;
import com.example.nontonitats.response.DeleteAccountResponse;
import com.google.android.material.materialswitch.MaterialSwitch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private MaterialSwitch switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Dark mode switch
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Load saved preference
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        // Set switch checked state
        switchDarkMode.setChecked(isDarkMode);

        // Apply theme on startup
        applyDarkMode(isDarkMode);

        // On toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            SharedPreferences.Editor editor = getSharedPreferences("app_settings", MODE_PRIVATE).edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            // Apply theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            recreate();
        });

        LinearLayout btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Hapus Akun")
                    .setMessage("Apakah kamu yakin ingin menghapus akun? Aksi ini tidak bisa dibatalkan.")
                    .setPositiveButton("Ya, Hapus", (dialog, which) -> {

                        // Ambil token
                        SharedPreferences userPrefs = getSharedPreferences("APP_PREF", MODE_PRIVATE);
                        String token = userPrefs.getString("TOKEN", null);

                        if (token == null) {
                            Toast.makeText(this, "Tidak ada token", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Panggil API dengan token
                        ApiService api = ApiClient.getClientWithToken(token).create(ApiService.class);

                        api.deleteAccount().enqueue(new Callback<DeleteAccountResponse>() {
                            @Override
                            public void onResponse(Call<DeleteAccountResponse> call, Response<DeleteAccountResponse> response) {
                                if (response.isSuccessful()) {

                                    // Hapus semua data local
                                    userPrefs.edit().clear().apply();
                                    getSharedPreferences("user_profile", MODE_PRIVATE).edit().clear().apply();

                                    Toast.makeText(SettingsActivity.this,
                                            "Akun berhasil dihapus",
                                            Toast.LENGTH_SHORT).show();

                                    // Pindah ke LoginActivity
                                    Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();

                                } else {
                                    Toast.makeText(SettingsActivity.this, "Gagal menghapus akun", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<DeleteAccountResponse> call, Throwable t) {
                                Toast.makeText(SettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    })
                    .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }

    private void applyDarkMode(boolean enable) {
        if (enable) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
