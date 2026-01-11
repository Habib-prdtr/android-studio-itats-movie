package com.example.nontonitats;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nontonitats.model.Movie;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private ImageButton btnBack, btnFullscreen;
    private TextView tvMovieTitle, tvCurrentTime, tvTotalTime;
    private ProgressBar progressBar;
    private boolean isFullscreen = false;
    private Movie movie;
    private static final String BASE_IMAGE_URL = "http://10.0.2.2/api-mobile/public/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("PlayerActivity", "onCreate started");

        // Set fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_player);

        // Get movie data dari intent
        movie = (Movie) getIntent().getSerializableExtra("MOVIE");

        if (movie == null) {
            Log.e("PlayerActivity", "Movie data is null!");
            Toast.makeText(this, "Movie data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("PlayerActivity", "Movie title: " + movie.getTitle());
        Log.d("PlayerActivity", "Video URL: " + movie.getVideo_url());

        initializeViews();
        initializePlayer();
        setupClickListeners();
    }

    private void initializeViews() {
        Log.d("PlayerActivity", "Initializing views");

        playerView = findViewById(R.id.playerView);
        btnBack = findViewById(R.id.btnBack);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        progressBar = findViewById(R.id.progressBar);

        // Set movie data dinamis
        if (movie != null) {
            tvMovieTitle.setText(movie.getTitle());
            tvTotalTime.setText("00:00");
            tvCurrentTime.setText("00:00");
        }
    }

    private void initializePlayer() {
        Log.d("PlayerActivity", "Initializing player");

        try {
            // Initialize player FIRST
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
            playerView.setKeepScreenOn(true);

            Log.d("PlayerActivity", "Player initialized");

            // Setup player listener
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Log.d("PlayerActivity", "Playback state changed: " + playbackState);

                    if (playbackState == Player.STATE_READY) {
                        progressBar.setVisibility(View.GONE);
                        player.play();

                        long duration = player.getDuration();
                        tvTotalTime.setText(formatTime(duration));
                        Log.d("PlayerActivity", "Video duration: " + duration + "ms");

                        startUpdatingProgress();
                    } else if (playbackState == Player.STATE_BUFFERING) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else if (playbackState == Player.STATE_ENDED) {
                        Toast.makeText(PlayerActivity.this,
                                "Video selesai", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("PlayerActivity", "Player error: " + error.getMessage());
                    Toast.makeText(PlayerActivity.this,
                            "Error loading video", Toast.LENGTH_SHORT).show();

                    // Fallback ke YouTube
                    openYouTubeFallback();
                }
            });

            // Load video
            loadVideo();

        } catch (Exception e) {
            Log.e("PlayerActivity", "Error initializing player: " + e.getMessage());
            Toast.makeText(this, "Error initializing player", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadVideo() {
        if (movie != null && movie.getVideo_url() != null && !movie.getVideo_url().isEmpty()) {
            String videoUrl = BASE_IMAGE_URL + movie.getVideo_url();
            Log.d("PlayerActivity", "Loading video from URL: " + videoUrl);

            try {
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
                player.setMediaItem(mediaItem);
                player.prepare();
                progressBar.setVisibility(View.VISIBLE);

                Log.d("PlayerActivity", "Video loading started");

            } catch (Exception e) {
                Log.e("PlayerActivity", "Error setting media item: " + e.getMessage());
                Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show();
                openYouTubeFallback();
            }
        } else {
            Log.e("PlayerActivity", "Video URL is null or empty");
            Toast.makeText(this, "Video URL tidak tersedia", Toast.LENGTH_SHORT).show();
            openYouTubeFallback();
        }
    }

    private void openYouTubeFallback() {
        if (movie != null && movie.getTitle() != null) {
            String youtubeQuery = movie.getTitle() + " official trailer";
            Log.d("PlayerActivity", "Opening YouTube for: " + youtubeQuery);

            Toast.makeText(this, "Membuka trailer di YouTube...", Toast.LENGTH_LONG).show();

            try {
                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/results?search_query=" +
                                Uri.encode(youtubeQuery)));
                startActivity(youtubeIntent);
            } catch (Exception e) {
                Log.e("PlayerActivity", "Error opening YouTube: " + e.getMessage());
                Toast.makeText(this, "Tidak dapat membuka YouTube", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void startUpdatingProgress() {
        playerView.post(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    long currentPosition = player.getCurrentPosition();
                    tvCurrentTime.setText(formatTime(currentPosition));
                    playerView.postDelayed(this, 1000);
                }
            }
        });
    }

    private String formatTime(long milliseconds) {
        if (milliseconds < 0) return "00:00";

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
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
        btnFullscreen.setOnClickListener(v -> {
            toggleFullscreen();
        });

        // Play/Pause button
        playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_play).setOnClickListener(v -> {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        });
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            // Exit fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen);
        } else {
            // Enter fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
        }
        isFullscreen = !isFullscreen;
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            toggleFullscreen();
        } else {
            releasePlayer();
            super.onBackPressed();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            Log.d("PlayerActivity", "Player released");
        }
    }
}