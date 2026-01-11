package com.example.nontonitats;

import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nontonitats.adapter.SimilarMoviesAdapter;
import com.example.nontonitats.model.Movie;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    // UI Components
    private ExoPlayer player;
    private PlayerView playerView;
    private ImageButton btnBack, btnSettings, btnFullscreen;
    private TextView tvMovieTitle, tvMovieInfo, tvCurrentTime, tvTotalTime, tvErrorMessage;
    private ProgressBar progressBar;
    private ImageView ivPlayPauseOverlay;
    private View infoScrollView;

    // Tambahan views untuk bagian detail
    private ImageView imgMoviePoster;
    private TextView tvMovieTitleDetailed, tvMovieRatingDetailed, tvMovieYearDetailed,
            tvMovieDurationDetailed, tvMovieGenreDetailed, tvMovieDescription, tvReleaseDate;
    private MaterialButton btnWatchNowBottom;

    // Similar Movies
    private RecyclerView rvRecommendations;
    private SimilarMoviesAdapter similarMoviesAdapter;
    private List<Movie> similarMovies = new ArrayList<>();

    // Semua movies dari MainActivity (dikirim melalui Intent)
    private List<Movie> allMovies = new ArrayList<>();

    // Player state
    private boolean isFullscreen = false;
    private boolean isControlsVisible = true;
    private Movie movie;
    private static final String BASE_IMAGE_URL = "http://10.0.2.2/api-mobile/public/";

    // Gesture detector for double tap
    private GestureDetectorCompat gestureDetector;
    private Handler controlsHandler = new Handler();

    // Runnable to hide controls
    private final Runnable hideControlsRunnable = () -> hideControls();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);

        // Get movie data dan semua movies
        movie = (Movie) getIntent().getSerializableExtra("MOVIE");

        // Ambil semua movies dari intent (dikirim dari DetailFragment)
        // Atau bisa juga dari static method di MainActivity
        allMovies = getMoviesFromMainActivity();

        if (movie == null) {
            Toast.makeText(this, "Movie data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupGestureDetector();
        initializePlayer();
        setupClickListeners();
        updateMovieInfo();
        loadSimilarMovies();
    }

    private List<Movie> getMoviesFromMainActivity() {
        // Method 1: Lewat Intent (dikirim dari DetailFragment)
        if (getIntent().hasExtra("ALL_MOVIES")) {
            return (List<Movie>) getIntent().getSerializableExtra("ALL_MOVIES");
        }

        // Method 2: Lewat Application class atau static method
        // Buat dulu class AppController untuk menyimpan global data
        return getMoviesFromGlobal();
    }

    private List<Movie> getMoviesFromGlobal() {

        // Return empty list jika tidak ada
        return new ArrayList<>();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvMovieInfo = findViewById(R.id.tvMovieInfo);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        progressBar = findViewById(R.id.progressBar);
        ivPlayPauseOverlay = findViewById(R.id.ivPlayPauseOverlay);
        infoScrollView = findViewById(R.id.infoScrollView);

        // Inisialisasi views untuk bagian detail
        imgMoviePoster = findViewById(R.id.imgMoviePoster);
        tvMovieTitleDetailed = findViewById(R.id.tvMovieTitleDetailed);
        tvMovieRatingDetailed = findViewById(R.id.tvMovieRatingDetailed);
        tvMovieYearDetailed = findViewById(R.id.tvMovieYearDetailed);
        tvMovieDurationDetailed = findViewById(R.id.tvMovieDurationDetailed);
        tvMovieGenreDetailed = findViewById(R.id.tvMovieGenreDetailed);
        tvMovieDescription = findViewById(R.id.tvMovieDescription);

        tvReleaseDate = findViewById(R.id.tvReleaseDate);

        // Setup Similar Movies RecyclerView
        rvRecommendations = findViewById(R.id.rvRecommendations);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        similarMoviesAdapter = new SimilarMoviesAdapter(similarMovies, this::onSimilarMovieClick);
        rvRecommendations.setAdapter(similarMoviesAdapter);
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                togglePlayPause();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleControls();
                return true;
            }
        });

        // Set touch listener for player view
        playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void initializePlayer() {
        try {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
            playerView.setKeepScreenOn(true);

            // Setup player listeners
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_READY) {
                        progressBar.setVisibility(View.GONE);
                        player.play();

                        // Update total time
                        long duration = player.getDuration();
                        tvTotalTime.setText(formatTime(duration));

                        // Start progress updates
                        startProgressUpdates();

                        // Show info panel after 3 seconds
                        new Handler().postDelayed(() -> {
                            infoScrollView.setVisibility(View.VISIBLE);
                        }, 3000);

                    } else if (playbackState == Player.STATE_BUFFERING) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else if (playbackState == Player.STATE_ENDED) {
                        showToast("Video selesai");
                        // Tampilkan kembali info panel saat video selesai
                        infoScrollView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    progressBar.setVisibility(View.GONE);
                    tvErrorMessage.setVisibility(View.VISIBLE);
                    Log.e("PlayerActivity", "Player error: " + error.getMessage());
                    tvErrorMessage.setText("Error: " + error.getMessage());
                }
            });

            // Setup progress bar listener
            TimeBar timeBar = playerView.findViewById(R.id.exo_progress);
            if (timeBar != null) {
                timeBar.addListener(new TimeBar.OnScrubListener() {
                    @Override
                    public void onScrubStart(TimeBar timeBar, long position) {
                        controlsHandler.removeCallbacks(hideControlsRunnable);
                    }

                    @Override
                    public void onScrubMove(TimeBar timeBar, long position) {
                        tvCurrentTime.setText(formatTime(position));
                    }

                    @Override
                    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                        player.seekTo(position);
                        resetControlsTimeout();
                    }
                });
            }

            // Load video
            loadVideo();

        } catch (Exception e) {
            Log.e("PlayerActivity", "Error initializing player", e);
            showToast("Error initializing player");
            finish();
        }
    }

    private void loadVideo() {
        if (movie != null && movie.getVideo_url() != null && !movie.getVideo_url().isEmpty()) {
            String videoUrl = BASE_IMAGE_URL + movie.getVideo_url();
            Log.d("PlayerActivity", "Loading video: " + videoUrl);

            try {
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
                player.setMediaItem(mediaItem);
                player.prepare();
                progressBar.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e("PlayerActivity", "Error loading video", e);
                tvErrorMessage.setVisibility(View.VISIBLE);
                tvErrorMessage.setText("Gagal memuat video: " + e.getMessage());
            }
        } else {
            tvErrorMessage.setVisibility(View.VISIBLE);
            tvErrorMessage.setText("Video tidak tersedia");
        }
    }

    private void updateMovieInfo() {
        if (movie != null) {
            // Set movie title di bagian atas
            tvMovieTitle.setText(movie.getTitle());

            // Set movie title di bagian detail
            tvMovieTitleDetailed.setText(movie.getTitle());

            // Build info untuk bagian atas
            StringBuilder info = new StringBuilder();

            if (movie.getRating() != null) {
                double rating = movie.getRating();
                info.append("⭐ ").append(String.format(Locale.getDefault(), "%.1f", rating));
                // Set rating di bagian detail
                tvMovieRatingDetailed.setText(String.format("⭐ %.1f", rating));
            } else {
                info.append("⭐ 0.0");
                tvMovieRatingDetailed.setText("⭐ 0.0");
            }

            String year = "N/A";
            if (movie.getRelease_date() != null && !movie.getRelease_date().isEmpty()) {
                if (movie.getRelease_date().length() >= 4) {
                    year = movie.getRelease_date().substring(0, 4);
                }
                info.append(" • ").append(year);
                // Set tahun di bagian detail
                tvMovieYearDetailed.setText(year);

                // Format tanggal rilis lengkap
                String releaseDate = formatReleaseDate(movie.getRelease_date());
                tvReleaseDate.setText(releaseDate);
            } else {
                info.append(" • N/A");
                tvMovieYearDetailed.setText("N/A");
                tvReleaseDate.setText("Tanggal tidak tersedia");
            }

            // Calculate duration (sama seperti di DetailFragment)
            String durationText = calculateDuration();
            info.append(" • ").append(durationText);

            // Set duration di bagian detail
            tvMovieDurationDetailed.setText(durationText);

            tvMovieInfo.setText(info.toString());

            // Set genre
            if (movie.getGenre() != null && !movie.getGenre().isEmpty()) {
                tvMovieGenreDetailed.setText(movie.getGenre());
            } else {
                tvMovieGenreDetailed.setText("Genre tidak tersedia");
            }

            // Set deskripsi
            if (movie.getOverview() != null && !movie.getOverview().isEmpty()) {
                tvMovieDescription.setText(movie.getOverview());
            } else {
                tvMovieDescription.setText("Deskripsi tidak tersedia.");
            }

            // Load poster image
            loadMoviePoster();

        }
    }

    private String calculateDuration() {
        // Sama seperti di DetailFragment
        if (movie.getRating() != null) {
            int durationMinutes = (int) (movie.getRating() * 15 + 60);
            if (durationMinutes < 60) {
                return durationMinutes + "m";
            } else {
                int hours = durationMinutes / 60;
                int minutes = durationMinutes % 60;
                return String.format("%dh %02dm", hours, minutes);
            }
        }
        return "2h 00m";
    }

    private void loadMoviePoster() {
        if (movie.getPoster_url() != null && !movie.getPoster_url().isEmpty()) {
            String posterUrl = BASE_IMAGE_URL + movie.getPoster_url();
            Log.d("PlayerActivity", "Loading poster: " + posterUrl);
            Glide.with(this)
                    .load(posterUrl)
                    .placeholder(R.drawable.poster_placeholder)
                    .centerCrop()
                    .into(imgMoviePoster);
        } else {
            imgMoviePoster.setImageResource(R.drawable.poster_placeholder);
        }
    }

    private String formatReleaseDate(String date) {
        try {
            if (date == null || date.isEmpty()) {
                return "Tanggal tidak tersedia";
            }

            // Coba format ISO (yyyy-MM-dd)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (Exception e) {
            // Jika parsing gagal, kembalikan string asli
            Log.w("PlayerActivity", "Failed to parse date: " + date);
            return date;
        }
    }

    private void loadSimilarMovies() {
        similarMovies.clear();

        if (allMovies != null && !allMovies.isEmpty()) {
            // 1. Cari film dengan genre yang sama
            List<Movie> sameGenreMovies = new ArrayList<>();
            String mainGenre = getMainGenre();

            for (Movie m : allMovies) {
                if (m.getId() != movie.getId() &&
                        m.getGenre() != null &&
                        !m.getGenre().isEmpty()) {

                    // Cek apakah ada genre yang sama
                    if (hasSameGenre(m.getGenre(), mainGenre)) {
                        sameGenreMovies.add(m);
                    }
                }
            }

            // 2. Jika kurang dari 5, tambahkan film random dengan rating tinggi
            if (sameGenreMovies.size() < 5) {
                List<Movie> sortedMovies = new ArrayList<>(allMovies);
                Collections.sort(sortedMovies, (m1, m2) ->
                        Double.compare(m2.getRating(), m1.getRating()));

                for (Movie m : sortedMovies) {
                    if (m.getId() != movie.getId() &&
                            !sameGenreMovies.contains(m) &&
                            sameGenreMovies.size() < 5) {
                        sameGenreMovies.add(m);
                    }
                }
            }

            // 3. Ambil maksimal 5 film
            similarMovies.addAll(sameGenreMovies.subList(0, Math.min(5, sameGenreMovies.size())));

            similarMoviesAdapter.updateList(similarMovies);
            Log.d("PlayerActivity", "Loaded " + similarMovies.size() + " similar movies");

        } else {
            Log.w("PlayerActivity", "No movies available for recommendations");
        }
    }

    private String getMainGenre() {
        if (movie.getGenre() != null && !movie.getGenre().isEmpty()) {
            String[] genres = movie.getGenre().split(",");
            if (genres.length > 0) {
                return genres[0].trim();
            }
        }
        return "";
    }

    private boolean hasSameGenre(String movieGenre, String mainGenre) {
        if (movieGenre == null || mainGenre == null || mainGenre.isEmpty()) {
            return false;
        }

        String[] genres = movieGenre.split(",");
        for (String genre : genres) {
            if (genre.trim().equalsIgnoreCase(mainGenre)) {
                return true;
            }
        }
        return false;
    }

    private void onSimilarMovieClick(Movie similarMovie) {
        // When a similar movie is clicked, play that movie
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("MOVIE", similarMovie);
        // Kirim juga semua movies untuk recommendations
        intent.putExtra("ALL_MOVIES", new ArrayList<>(allMovies));
        startActivity(intent);
        finish(); // Close current player
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            if (isFullscreen) {
                toggleFullscreen();
            } else {
                finish();
            }
        });

        // Fullscreen button
        btnFullscreen.setOnClickListener(v -> toggleFullscreen());

        // Settings button
        btnSettings.setOnClickListener(v -> showSettingsMenu());

        // Custom play button di player controls
        View customPlayButton = playerView.findViewById(R.id.exo_play);
        if (customPlayButton != null) {
            customPlayButton.setOnClickListener(v -> togglePlayPause());
        }

        // Rewind 10 seconds
        View btnRewind = playerView.findViewById(R.id.btnRewind);
        if (btnRewind != null) {
            btnRewind.setOnClickListener(v -> rewind10Seconds());
        }

        // Forward 10 seconds
        View btnForward = playerView.findViewById(R.id.btnForward);
        if (btnForward != null) {
            btnForward.setOnClickListener(v -> forward10Seconds());
        }

        // Touch listener untuk infoScrollView untuk toggle controls
        infoScrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                toggleControls();
                return true;
            }
            return false;
        });

        // Touch listener untuk player view untuk toggle info panel
        playerView.setOnClickListener(v -> {
            // Jika controls terlihat, sembunyikan info panel juga
            if (isControlsVisible) {
                infoScrollView.setVisibility(View.GONE);
            } else {
                infoScrollView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void togglePlayPause() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                ivPlayPauseOverlay.setImageResource(R.drawable.ic_play);
            } else {
                player.play();
                ivPlayPauseOverlay.setImageResource(R.drawable.ic_pause);
            }

            // Show play/pause overlay briefly
            ivPlayPauseOverlay.setVisibility(View.VISIBLE);
            ivPlayPauseOverlay.animate().alpha(1f).setDuration(200).withEndAction(() -> {
                new Handler().postDelayed(() -> {
                    ivPlayPauseOverlay.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                        ivPlayPauseOverlay.setVisibility(View.GONE);
                    });
                }, 500);
            });

        }
    }

    private void rewind10Seconds() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            player.seekTo(Math.max(0, currentPosition - 10000));
            showToast("Rewind 10 detik");
        }
    }

    private void forward10Seconds() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            player.seekTo(Math.min(duration, currentPosition + 10000));
            showToast("Forward 10 detik");
        }
    }

    private void toggleControls() {
        if (isControlsVisible) {
            hideControls();
        } else {
            showControls();
        }
        resetControlsTimeout();
    }

    private void showControls() {
        findViewById(R.id.topControls).setVisibility(View.VISIBLE);
        findViewById(R.id.bottomControls).setVisibility(View.VISIBLE);
        findViewById(R.id.topGradient).setVisibility(View.VISIBLE);
        findViewById(R.id.bottomGradient).setVisibility(View.VISIBLE);
        isControlsVisible = true;
    }

    private void hideControls() {
        findViewById(R.id.topControls).setVisibility(View.GONE);
        findViewById(R.id.bottomControls).setVisibility(View.GONE);
        findViewById(R.id.topGradient).setVisibility(View.GONE);
        findViewById(R.id.bottomGradient).setVisibility(View.GONE);
        isControlsVisible = false;
    }

    private void resetControlsTimeout() {
        controlsHandler.removeCallbacks(hideControlsRunnable);
        controlsHandler.postDelayed(hideControlsRunnable, 5000); // 5 detik
    }

    private void startProgressUpdates() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    long currentPosition = player.getCurrentPosition();
                    tvCurrentTime.setText(formatTime(currentPosition));

                    // Continue updating every second
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            // Exit fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen);

            // Show info panel when exiting fullscreen
            infoScrollView.setVisibility(View.VISIBLE);
        } else {
            // Enter fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);

            // Hide info panel in fullscreen
            infoScrollView.setVisibility(View.GONE);
        }
        isFullscreen = !isFullscreen;
        resetControlsTimeout();
    }

    private void showSettingsMenu() {
        // Create a simple settings dialog
        String[] items = {"Kualitas: 1080p", "Kecepatan: Normal", "Subtitle: Indonesia", "Audio: Stereo"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Pengaturan Pemutar")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showToast("Mengubah kualitas video");
                            break;
                        case 1:
                            showToast("Mengubah kecepatan pemutaran");
                            break;
                        case 2:
                            showToast("Mengubah bahasa subtitle");
                            break;
                        case 3:
                            showToast("Mengubah pengaturan audio");
                            break;
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private String formatTime(long milliseconds) {
        if (milliseconds < 0) return "00:00";

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null && !player.isPlaying()) {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            player.pause();
        }
        controlsHandler.removeCallbacks(hideControlsRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        controlsHandler.removeCallbacks(hideControlsRunnable);
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            toggleFullscreen();
        } else {
            super.onBackPressed();
        }
    }
}