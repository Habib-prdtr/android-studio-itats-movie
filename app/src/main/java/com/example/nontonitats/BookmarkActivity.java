package com.example.nontonitats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.example.nontonitats.adapter.MovieAdapter;
import com.example.nontonitats.model.Movie;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {

    private RecyclerView rvSaved;
    private MovieAdapter adapter;
    private TextView tvEmpty;

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        rvSaved = findViewById(R.id.rvSavedMovies);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBackBookmark);


        rvSaved.setLayoutManager(new GridLayoutManager(this, 2));

        ArrayList<Movie> savedMovies = BookmarkManager.getBookmarks(this);

        // DEBUG log
        Log.d("BOOKMARK", "Saved count = " + savedMovies.size());

        // jika kosong, tampilkan pesan
        if (savedMovies == null || savedMovies.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        adapter = new MovieAdapter(savedMovies, movie -> {
            MovieDetailFragment fragment = MovieDetailFragment.newInstance(movie);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerSaved, fragment)
                    .commit();

            findViewById(R.id.fragmentContainerSaved).setVisibility(View.VISIBLE);
        });

        rvSaved.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            finish(); // kembali ke MainActivity
        });
    }
}
