package com.example.nontonitats;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.nontonitats.api.ApiClient;
import com.example.nontonitats.api.ApiService;
import com.example.nontonitats.request.UpdateProfileRequest;
import com.example.nontonitats.response.UpdateProfileResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    ImageView ivProfile;
    EditText etName, etEmail;
    Button btnSave, btnDeletePhoto;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ivProfile = findViewById(R.id.ivProfileEdit);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        btnDeletePhoto = findViewById(R.id.btnDeletePhoto);

        prefs = getSharedPreferences("user_profile", MODE_PRIVATE);

        loadSavedData();

        ivProfile.setOnClickListener(v -> pickImage());
        btnSave.setOnClickListener(v -> saveProfile());
        btnDeletePhoto.setOnClickListener(v -> deletePhoto());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // ------------------------------ PILIH GAMBAR -----------------------------------
    private void pickImage() {

        // Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return;
            }
        } else {
            // Android 12 ke bawah
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ivProfile.setImageBitmap(bitmap);

                        saveImageToPrefs(bitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private void saveImageToPrefs(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
        String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        prefs.edit()
                .putString("photo", encodedImage)
                .apply();
    }

    // ------------------------------ SAVE PROFILE -----------------------------------
    private void saveProfile() {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();

        // Update SharedPreferences
        prefs.edit()
                .putString("name", name)
                .putString("email", email)
                .apply();

        // Update ke server
        SharedPreferences appPrefs = getSharedPreferences("APP_PREF", MODE_PRIVATE);
        String token = appPrefs.getString("TOKEN", null);

        if (token != null) {
            ApiService api = ApiClient.getClientWithToken(token).create(ApiService.class);
            UpdateProfileRequest req = new UpdateProfileRequest(name, email);

            api.updateProfile(req).enqueue(new Callback<UpdateProfileResponse>() {
                @Override
                public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                    Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    // ------------------------------ LOAD DATA -----------------------------------
    private void loadSavedData() {
        etName.setText(prefs.getString("name", ""));
        etEmail.setText(prefs.getString("email", ""));

        String encoded = prefs.getString("photo", "");
        if (!encoded.isEmpty()) {
            byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            ivProfile.setImageBitmap(bmp);
        }
    }

    // ------------------------------ DELETE PHOTO -----------------------------------
    private void deletePhoto() {
        prefs.edit().remove("photo").apply();

        ivProfile.setImageResource(R.drawable.ic_profile_large);

        Toast.makeText(this, "Foto dihapus", Toast.LENGTH_SHORT).show();
    }
}
