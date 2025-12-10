package com.example.nontonitats;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nontonitats.api.ApiClient;
import com.example.nontonitats.api.ApiService;
import com.example.nontonitats.request.RegisterRequest;
import com.example.nontonitats.response.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEdit, emailEdit, passwordEdit;
    private Button registerButton;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEdit = findViewById(R.id.editName);
        emailEdit = findViewById(R.id.editEmail);
        passwordEdit = findViewById(R.id.editPassword);
        registerButton = findViewById(R.id.btnRegister);

        apiService = ApiClient.getClient().create(ApiService.class);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = nameEdit.getText().toString();
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        RegisterRequest req = new RegisterRequest(name, email, password);

        apiService.register(req).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // 🔥 TARUH DI SINI
                    String token = response.body().getData().getToken();
                    String userName = response.body().getData().getUser().getName();

                    Toast.makeText(RegisterActivity.this,
                            "Registrasi sukses! Selamat datang " + userName,
                            Toast.LENGTH_SHORT).show();

                    finish(); // kembali ke login
                } else {
                    Toast.makeText(RegisterActivity.this, "Registrasi gagal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
