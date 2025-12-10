package com.example.nontonitats;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;


import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView tvUsername, tvEmail;
    ImageView profileImage;
    private LinearLayout btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        profileImage = findViewById(R.id.profileImage);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Logout")
                    .setMessage("Apakah kamu yakin ingin logout?")
                    .setPositiveButton("Ya", (dialog, which) -> {

                        // Hapus token
                        SharedPreferences prefs = getSharedPreferences("APP_PREF", MODE_PRIVATE);
                        prefs.edit().remove("TOKEN").apply();

                        // Pindah ke LoginActivity
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        // Tombol Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Tombol Edit Profile
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        loadProfileData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData(); // refresh data setiap kembali dari Edit Profile
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("user_profile", MODE_PRIVATE);

        String name = prefs.getString("name", "User");
        String email = prefs.getString("email", "example@mail.com");
        String encoded = prefs.getString("photo", "");

        tvUsername.setText(name);
        tvEmail.setText(email);

        // Jika user pernah upload foto → decode base64
        if (!encoded.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                profileImage.setImageBitmap(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

