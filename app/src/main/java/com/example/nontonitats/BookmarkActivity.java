package com.example.nontonitats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.example.nontonitats.adapter.MovieAdapter;
import com.example.nontonitats.model.Movie;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {

    private RecyclerView rvSaved;
    private MovieAdapter adapter;
    private LinearLayout emptyState;
    private ImageView btnBack;
    private TextView tvMovieCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        // Initialize views
        rvSaved = findViewById(R.id.rvSavedMovies);
        emptyState = findViewById(R.id.emptyState);
        btnBack = findViewById(R.id.btnBackBookmark);
        tvMovieCount = findViewById(R.id.tvMovieCount);

        // Setup RecyclerView
        rvSaved.setLayoutManager(new GridLayoutManager(this, 2));

        // Get saved movies
        ArrayList<Movie> savedMovies = BookmarkManager.getBookmarks(this);

        // Update movie count
        if (savedMovies != null) {
            tvMovieCount.setText(String.valueOf(savedMovies.size()));
        }

        // Show/hide empty state
        if (savedMovies == null || savedMovies.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvSaved.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvSaved.setVisibility(View.VISIBLE);
        }

        // Setup adapter
        adapter = new MovieAdapter(savedMovies != null ? savedMovies : new ArrayList<>(), movie -> {
            MovieDetailFragment fragment = MovieDetailFragment.newInstance(movie);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerSaved, fragment)
                    .addToBackStack("bookmark_detail")
                    .commit();

            findViewById(R.id.fragmentContainerSaved).setVisibility(View.VISIBLE);
        });

        rvSaved.setAdapter(adapter);

        // Back button listener
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.fragmentContainerSaved).getVisibility() == View.VISIBLE) {
            findViewById(R.id.fragmentContainerSaved).setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBookmarks();
    }

    private void refreshBookmarks() {
        ArrayList<Movie> savedMovies = BookmarkManager.getBookmarks(this);

        if (savedMovies != null) {
            tvMovieCount.setText(String.valueOf(savedMovies.size()));

            if (savedMovies.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                rvSaved.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                rvSaved.setVisibility(View.VISIBLE);
                adapter.updateList(savedMovies);
            }
        }
    }
}