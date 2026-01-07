package com.example.nontonitats;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nontonitats.adapter.MovieAdapter;
import com.example.nontonitats.api.ApiService;
import com.example.nontonitats.api.ApiClient;
import com.example.nontonitats.model.Movie;
import com.example.nontonitats.response.MovieResponse;
import com.example.nontonitats.utils.NetworkMonitor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMovies;
    private MovieAdapter adapter;
    private List<Movie> allMovies;
//    private TextView btnPopular, btnTopRated, btnUpcoming, btnAll;
    private EditText etSearch;
    private ImageView btnSaved;
    private ImageView btnProfile;
    private ImageView btnSettings;
    private NetworkMonitor networkMonitor;
    private AlertDialog noInternetDialog;
    private View blockerView;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvMovies = findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));

//        btnPopular = findViewById(R.id.btnPopular);
//        btnTopRated = findViewById(R.id.btnTopRated);
//        btnUpcoming = findViewById(R.id.btnUpcoming);
//        btnAll = findViewById(R.id.btnAll);
        etSearch = findViewById(R.id.etSearch);
        btnSaved = findViewById(R.id.btnSaved);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);
        blockerView = findViewById(R.id.blockerView);

        allMovies = new ArrayList<>();

        adapter = new MovieAdapter(new ArrayList<>(), movie -> showMovieDetail(movie));
        rvMovies.setAdapter(adapter);

        loadMoviesFromApi();


        showAllMovies();

//        btnPopular.setOnClickListener(v -> filterMovies("popular"));
//        btnTopRated.setOnClickListener(v -> filterMovies("topRated"));
//        btnUpcoming.setOnClickListener(v -> filterMovies("upcoming"));
//        btnAll.setOnClickListener(v -> showAllMovies());
        btnSaved.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
            startActivity(intent);
        });
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        networkMonitor = new NetworkMonitor(this, new NetworkMonitor.NetworkListener() {
            @Override
            public void onNetworkAvailable() {
                runOnUiThread(() -> {
                    if (noInternetDialog != null && noInternetDialog.isShowing()) {
                        noInternetDialog.dismiss();
                    }

                    blockerView.setVisibility(View.GONE);
                    blockerView.setOnTouchListener(null);
                });
            }

            @Override
            public void onNetworkLost() {
                showNoInternetDialog();
            }
        });

        checkInitialConnection();
        networkMonitor.startMonitor();

        // Live search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Movie> filtered = new ArrayList<>();
                if (s.length() == 0) {
                    filtered.addAll(allMovies);
                } else {
                    for (Movie m : allMovies) {
                        if (m.getTitle().toLowerCase().contains(s.toString().toLowerCase())) {
                            filtered.add(m);
                        }
                    }
                }
                adapter.updateList(filtered);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void showMovieDetail(Movie movie) {
            MovieDetailFragment fragment = MovieDetailFragment.newInstance(movie);
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void filterMovies(String category) {
        List<Movie> filtered = new ArrayList<>();
        for (Movie m : allMovies) {
            if (m.getGenre().equalsIgnoreCase(category));
        }
        adapter.updateList(filtered);
        highlightCategory(category);
    }

    private void showAllMovies() {
        adapter.updateList(allMovies);
//        resetHighlightCategory();
    }

    private void highlightCategory(String category) {
//        btnPopular.setBackgroundResource(R.drawable.bg_chip);
//        btnTopRated.setBackgroundResource(R.drawable.bg_chip);
//        btnUpcoming.setBackgroundResource(R.drawable.bg_chip);
//        btnAll.setBackgroundResource(R.drawable.bg_chip);

        switch (category) {
//            case "popular": btnPopular.setBackgroundResource(R.drawable.bg_chip_selected); break;
//            case "topRated": btnTopRated.setBackgroundResource(R.drawable.bg_chip_selected); break;
//            case "upcoming": btnUpcoming.setBackgroundResource(R.drawable.bg_chip_selected); break;
//            case "all": btnAll.setBackgroundResource(R.drawable.bg_chip_selected); break;
        }
    }

//    private void resetHighlightCategory() {
//        btnPopular.setBackgroundResource(R.drawable.bg_chip);
//        btnTopRated.setBackgroundResource(R.drawable.bg_chip);
//        btnUpcoming.setBackgroundResource(R.drawable.bg_chip);
//        btnAll.setBackgroundResource(R.drawable.bg_chip_selected);
//    }

    private void checkInitialConnection() {
        if (!NetworkMonitor.isConnected(this)) {
            showNoInternetDialog();

            // 🔥 Kunci seluruh UI
            blockerView.setVisibility(View.VISIBLE);
            blockerView.setOnTouchListener((v, e) -> true);
        }
    }

    private void showNoInternetDialog() {

        if (isFinishing() || isDestroyed()) return;
        if (noInternetDialog != null && noInternetDialog.isShowing()) return;

        runOnUiThread(() -> {

            // 🔥 Kunci seluruh UI (tidak bisa klik)
            blockerView.setVisibility(View.VISIBLE);
            blockerView.setOnTouchListener((v, event) -> true); // block semua klik

            noInternetDialog = new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Tidak Ada Koneksi Internet")
                    .setMessage("Periksa jaringan Anda dan coba lagi.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkMonitor != null) networkMonitor.stopMonitor();
    }

    private void loadMoviesFromApi() {
        ApiService apiService = ApiClient
                .getClient()
                .create(ApiService.class);

        apiService.getMovies().enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allMovies.clear();
                    allMovies.addAll(response.body().getData());
                    adapter.updateList(allMovies);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
