package com.example.nontonitats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nontonitats.api.ApiClient;
import com.example.nontonitats.api.ApiService;
import com.example.nontonitats.response.UserData;
import com.example.nontonitats.response.UsersResponse;
import com.example.nontonitats.adapter.UsersAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private Button btnLogout;
    private TextView txtTotalUsers, txtActiveUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        recyclerView = findViewById(R.id.recyclerUsers);
        txtTotalUsers = findViewById(R.id.txtTotalUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
    }

    private void loadUsers() {

        SharedPreferences prefs = getSharedPreferences("APP_PREF", MODE_PRIVATE);
        String token = prefs.getString("TOKEN", "");

        ApiService api = ApiClient.getClientWithToken(token).create(ApiService.class);

        api.getAllUsers().enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    List<UserData> userList = response.body().getData();

                    // 👉 Masukkan ke RecyclerView
                    usersAdapter = new UsersAdapter(userList);
                    recyclerView.setAdapter(usersAdapter);

                    // 👉 Hitung total user
                    int totalUsers = userList.size();

                    // 👉 Hitung active user (status = "active")
//                    int activeUsers = 0;
//                    for (UserModel user : userList) {
//                        if (user.getStatus() != null &&
//                                user.getStatus().equalsIgnoreCase("active")) {
//                            activeUsers++;
//                        }
//                    }

                    // 👉 Update tampilan dashboard admin
                    txtTotalUsers.setText(String.valueOf(totalUsers));
//                    txtActiveUsers.setText(String.valueOf(activeUsers));

                } else {
                    Toast.makeText(AdminActivity.this, "Gagal memuat user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void logout() {
        // Hapus token login
        SharedPreferences prefs = getSharedPreferences("APP_PREF", MODE_PRIVATE);
        prefs.edit().remove("TOKEN").apply();

        // Hapus data user
        SharedPreferences profile = getSharedPreferences("user_profile", MODE_PRIVATE);
        profile.edit().clear().apply();

        // Pindah ke LoginActivity
        Intent i = new Intent(AdminActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }


}
