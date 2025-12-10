package com.example.nontonitats;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.MalformedJsonException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nontonitats.api.ApiClient;
import com.example.nontonitats.api.ApiService;
import com.example.nontonitats.request.LoginRequest;
import com.example.nontonitats.response.LoginResponse;
import com.example.nontonitats.response.MeResponse;
import com.example.nontonitats.utils.NetworkMonitor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;

    private ApiService apiService;
    private NetworkMonitor networkMonitor;
    private AlertDialog noInternetDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ⬅️ Auto Login berdasarkan role
        checkAutoLogin();

        emailEditText = findViewById(R.id.editEmail);
        passwordEditText = findViewById(R.id.editPassword);
        loginButton = findViewById(R.id.btnLogin);
        registerButton = findViewById(R.id.btnGoRegister);

        apiService = ApiClient.getClient().create(ApiService.class);

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        loginButton.setOnClickListener(v -> loginUser());

        networkMonitor = new NetworkMonitor(this, new NetworkMonitor.NetworkListener() {
            @Override
            public void onNetworkAvailable() {
                if (noInternetDialog != null && noInternetDialog.isShowing()) {
                    noInternetDialog.dismiss();
                }
            }

            @Override
            public void onNetworkLost() {
                showNoInternetDialog();
            }
        });

        networkMonitor.startMonitor();

    }

    // -------------------- AUTO LOGIN --------------------
    private void checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("APP_PREF", MODE_PRIVATE);
        String savedToken = prefs.getString("TOKEN", null);

        SharedPreferences userPrefs = getSharedPreferences("user_profile", MODE_PRIVATE);
        String savedRole = userPrefs.getString("role", null);

        if (savedToken != null && savedRole != null) {

            if (savedRole.equalsIgnoreCase("admin")) {
                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }

            finish();
        }
    }

    // -------------------- LOGIN --------------------
    private void loginUser() {

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validasi input
        if (email.isEmpty()) {
            emailEditText.setError("Email harus diisi");
            return;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password harus diisi");
            return;
        }

        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    String token = response.body().getData().getToken();

                    SharedPreferences prefs = getSharedPreferences("APP_PREF", MODE_PRIVATE);
                    prefs.edit().putString("TOKEN", token).apply();

                    getUserProfile(token);

                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login gagal: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

                if (t instanceof SocketTimeoutException) {

                    showServerDownDialog();

                } else if (t instanceof IOException) {

                    // Tampilkan POPUP bila tidak ada internet
                    showNoInternetDialog();

                } else if (t instanceof JsonSyntaxException || t instanceof MalformedJsonException) {

                    Toast.makeText(LoginActivity.this,
                            "Response server tidak valid (bukan JSON).",
                            Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(LoginActivity.this,
                            "Error tidak diketahui: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }

        });
    }


    // -------------------- GET USER DATA --------------------
    private void getUserProfile(String token) {

        ApiService api = ApiClient.getClientWithToken(token).create(ApiService.class);

        api.me().enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    SharedPreferences prefs = getSharedPreferences("user_profile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    String name = response.body().getData().getName();
                    String email = response.body().getData().getEmail();
                    String role = response.body().getData().getRole();

                    editor.putString("name", name);
                    editor.putString("email", email);
                    editor.putString("role", role);
                    editor.apply();

                    // Arahkan berdasarkan role
                    if (role.equalsIgnoreCase("admin")) {
                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }

                    finish();
                }
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tidak Ada Koneksi Internet");
        builder.setMessage("Periksa jaringan Anda kemudian coba lagi.");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showServerDownDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Server Bermasalah")
                .setMessage("Server sedang tidak merespons. Coba lagi nanti.")
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkMonitor != null) networkMonitor.stopMonitor();
    }
}
