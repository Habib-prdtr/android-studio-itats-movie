package com.example.nontonitats;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nontonitats.adapter.GenreAdapter;
import com.example.nontonitats.adapter.MovieAdapter;
import com.example.nontonitats.adapter.NewReleasesAdapter;
import com.example.nontonitats.adapter.TrendingMovieAdapter;
import com.example.nontonitats.api.ApiService;
import com.example.nontonitats.api.ApiClient;
import com.example.nontonitats.model.Genre;
import com.example.nontonitats.model.Movie;
import com.example.nontonitats.response.MovieResponse;
import com.example.nontonitats.utils.NetworkMonitor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView rvTrending, rvNewReleases, rvAllMovies, rvGenres;
    private TrendingMovieAdapter trendingAdapter;
    private NewReleasesAdapter newReleasesAdapter;
    private MovieAdapter allMoviesAdapter;
    private GenreAdapter genreAdapter;
    private List<Movie> allMovies = new ArrayList<>();
    private List<Movie> trendingMovies = new ArrayList<>();
    private List<Movie> newReleaseMovies = new ArrayList<>();
    private List<Genre> genreList = new ArrayList<>();

    private TextView tvTrendingTitle, tvTrendingSubtitle, tvNewReleasesTitle, tvNewReleasesSubtitle, tvAllMoviesTitle;
    private EditText etSearch;
    private ImageView btnSaved, btnProfile, btnSettings;
    private NetworkMonitor networkMonitor;
    private AlertDialog noInternetDialog;
    private View blockerView;

    // Filter state
    private String currentGenre = "All Genres";
    private Set<String> uniqueGenres = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupAdapters();
        setupClickListeners();
        loadMoviesFromApi();

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

        setupSearch();
    }

    private void initializeViews() {
        // Recycler Views
        rvTrending = findViewById(R.id.rvTrending);
        rvNewReleases = findViewById(R.id.rvNewReleases);
        rvAllMovies = findViewById(R.id.rvAllMovies);
        rvGenres = findViewById(R.id.rvGenres);

        // TextViews untuk section titles
        tvTrendingTitle = findViewById(R.id.tvTrendingTitle);
        tvTrendingSubtitle = findViewById(R.id.tvTrendingSubtitle);
        tvNewReleasesTitle = findViewById(R.id.tvNewReleasesTitle);
        tvNewReleasesSubtitle = findViewById(R.id.tvNewReleasesSubtitle);
        tvAllMoviesTitle = findViewById(R.id.tvAllMoviesTitle);

        // Other views
        etSearch = findViewById(R.id.etSearch);
        btnSaved = findViewById(R.id.btnSaved);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);
        blockerView = findViewById(R.id.blockerView);

        // Setup layout managers
        rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvNewReleases.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setAutoMeasureEnabled(true); // Penting untuk wrap_content
        rvAllMovies.setLayoutManager(gridLayoutManager);
        rvAllMovies.setNestedScrollingEnabled(false);
        rvGenres.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupAdapters() {
        // Trending adapter (horizontal)
        trendingAdapter = new TrendingMovieAdapter(trendingMovies, movie -> showMovieDetail(movie));
        rvTrending.setAdapter(trendingAdapter);

        // New releases adapter
        newReleasesAdapter = new NewReleasesAdapter(newReleaseMovies, movie -> showMovieDetail(movie));
        rvNewReleases.setAdapter(newReleasesAdapter);

        // All movies adapter (grid)
        allMoviesAdapter = new MovieAdapter(allMovies, movie -> showMovieDetail(movie));
        rvAllMovies.setAdapter(allMoviesAdapter);

        // Genre adapter
        genreAdapter = new GenreAdapter(genreList, genre -> filterByGenre(genre));
        rvGenres.setAdapter(genreAdapter);
    }

    private void setupClickListeners() {
        // Navigation buttons
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
    }

    private void loadMoviesFromApi() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getMovies().enqueue(new retrofit2.Callback<MovieResponse>() {
            @Override
            public void onResponse(retrofit2.Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allMovies.clear();
                    allMovies.addAll(response.body().getData());

                    // Process movies for different sections
                    processMovies();

                    // Extract unique genres
                    extractGenres();

                    // Update all adapters
                    updateMovieLists();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<MovieResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to load movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processMovies() {
        // Trending (top 10 by rating)
        trendingMovies.clear();

        List<Movie> sortedByRating = new ArrayList<>(allMovies);
        Collections.sort(sortedByRating, (m1, m2) ->
                Double.compare(m2.getRating(), m1.getRating())
        );

        trendingMovies.addAll(
                sortedByRating.subList(0, Math.min(10, sortedByRating.size()))
        );

        // New releases (by latest id or date)
        newReleaseMovies.clear();

        List<Movie> sortedByDate = new ArrayList<>(allMovies);
        Collections.sort(sortedByDate, (m1, m2) ->
                m2.getRelease_date().compareTo(m1.getRelease_date())
        );

        newReleaseMovies.addAll(
                sortedByDate.subList(0, Math.min(5, sortedByDate.size()))
        );
    }

    private void extractGenres() {
        uniqueGenres.clear();
        genreList.clear();

        // Extract unique genres from all movies
        for (Movie movie : allMovies) {
            if (movie.getGenre() != null && !movie.getGenre().isEmpty()) {
                String[] genres = movie.getGenre().split(",");
                for (String genre : genres) {
                    String trimmedGenre = genre.trim();
                    if (!trimmedGenre.isEmpty() && uniqueGenres.add(trimmedGenre)) {
                        genreList.add(new Genre(trimmedGenre, getGenreColor(trimmedGenre)));
                    }
                }
            }
        }

        // Add "All Genres" option
        genreList.add(0, new Genre("All Genres", getGenreColor("All Genres")));
        genreAdapter.notifyDataSetChanged();
    }

    private int getGenreColor(String genre) {
        Map<String, Integer> genreColors = new HashMap<>();
        genreColors.put("Action Epic", R.color.genre_action);
        genreColors.put("Psychological Drama", R.color.genre_drama);
        genreColors.put("Crime", R.color.genre_comedy);
        genreColors.put("Anime", R.color.genre_horror);
        genreColors.put("Korean", R.color.genre_romance);
        genreColors.put("Sci-Fi", R.color.genre_sci_fi);
        genreColors.put("Adventure", R.color.genre_adventure);
        genreColors.put("Animation", R.color.genre_animation);
        genreColors.put("All Genres", R.color.colorPrimary);

        Integer colorRes = genreColors.get(genre);
        return colorRes != null ? colorRes : R.color.gray_600;
    }

    private void updateMovieLists() {
        // Update trending and new releases
        trendingAdapter.updateList(trendingMovies);
        newReleasesAdapter.updateList(newReleaseMovies);

        // Filter all movies based on current genre
        filterMoviesByGenre();
    }

    private void filterByGenre(Genre genre) {
        currentGenre = genre.getName();

        // Highlight selected genre in adapter
        genreAdapter.setSelectedGenre(currentGenre);

        // Show/hide trending and new releases sections
        updateSectionVisibility();

        // Filter movies
        filterMoviesByGenre();
    }

    private void updateSectionVisibility() {
        if (currentGenre.equals("All Genres")) {
            // Tampilkan semua section
            tvTrendingTitle.setVisibility(View.VISIBLE);
            tvTrendingSubtitle.setVisibility(View.VISIBLE);
            rvTrending.setVisibility(View.VISIBLE);

            tvNewReleasesTitle.setVisibility(View.VISIBLE);
            tvNewReleasesSubtitle.setVisibility(View.VISIBLE);
            rvNewReleases.setVisibility(View.VISIBLE);

            tvAllMoviesTitle.setText("📺 All Movies");
        } else {
            // Sembunyikan trending & new releases, fokus ke genre
            tvTrendingTitle.setVisibility(View.GONE);
            tvTrendingSubtitle.setVisibility(View.GONE);
            rvTrending.setVisibility(View.GONE);

            tvNewReleasesTitle.setVisibility(View.GONE);
            tvNewReleasesSubtitle.setVisibility(View.GONE);
            rvNewReleases.setVisibility(View.GONE);

            // Ubah judul All Movies menjadi "Action Movies", "Drama Movies", dll
            tvAllMoviesTitle.setText("🎬 " + currentGenre + " Movies");

            // Pindahkan All Movies ke posisi atas (di bawah genre)
            // ConstraintLayout akan menangani ini otomatis saat view di atasnya GONE
        }
    }

    private void filterMoviesByGenre() {
        List<Movie> filteredMovies = new ArrayList<>();

        if (currentGenre.equals("All Genres")) {
            filteredMovies.addAll(allMovies);
        } else {
            for (Movie movie : allMovies) {
                if (movie.getGenre() != null && movie.getGenre().toLowerCase().contains(currentGenre.toLowerCase())) {
                    filteredMovies.add(movie);
                }
            }
        }

        allMoviesAdapter.updateList(filteredMovies);
    }

    private void showMovieDetail(Movie movie) {
        MovieDetailFragment fragment = MovieDetailFragment.newInstance(movie);
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();

                if (query.isEmpty()) {
                    // Kembali ke filter genre saat ini
                    filterMoviesByGenre();
                } else {
                    // Search dari semua movie, abaikan filter genre
                    List<Movie> filtered = new ArrayList<>();
                    for (Movie movie : allMovies) {
                        if (movie.getTitle().toLowerCase().contains(query) ||
                                (movie.getGenre() != null && movie.getGenre().toLowerCase().contains(query)) ||
                                (movie.getOverview() != null && movie.getOverview().toLowerCase().contains(query))) {
                            filtered.add(movie);
                        }
                    }
                    allMoviesAdapter.updateList(filtered);

                    // Saat search aktif, sembunyikan trending & new releases
                    tvTrendingTitle.setVisibility(View.GONE);
                    tvTrendingSubtitle.setVisibility(View.GONE);
                    rvTrending.setVisibility(View.GONE);

                    tvNewReleasesTitle.setVisibility(View.GONE);
                    tvNewReleasesSubtitle.setVisibility(View.GONE);
                    rvNewReleases.setVisibility(View.GONE);

                    tvAllMoviesTitle.setText("🔍 Search Results");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Jika search dikosongkan, kembalikan tampilan sesuai genre
                if (s.toString().isEmpty()) {
                    updateSectionVisibility();
                }
            }
        });
    }

    private void checkInitialConnection() {
        if (!NetworkMonitor.isConnected(this)) {
            showNoInternetDialog();
            blockerView.setVisibility(View.VISIBLE);
            blockerView.setOnTouchListener((v, e) -> true);
        }
    }

    private void showNoInternetDialog() {
        if (isFinishing() || isDestroyed()) return;
        if (noInternetDialog != null && noInternetDialog.isShowing()) return;

        runOnUiThread(() -> {
            blockerView.setVisibility(View.VISIBLE);
            blockerView.setOnTouchListener((v, event) -> true);

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
}