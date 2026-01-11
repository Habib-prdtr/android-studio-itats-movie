package com.example.nontonitats;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nontonitats.adapter.SimilarMoviesAdapter;
import com.example.nontonitats.model.Movie;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE = "arg_movie";
    private Movie movie;
    private ImageButton btnBookmarkIcon;
    private boolean isBookmarked = false;
    private static final String BASE_IMAGE_URL = "http://10.0.2.2/api-mobile/public/";

    // Views
    private ImageView imgBackdrop, imgMoviePoster;
    private TextView tvMovieTitle, tvMovieGenre, tvMovieRating, tvMovieDuration, tvMovieYear,
            tvMovieDescription, tvCast, tvDirector;

    // Untuk similar movies
    private RecyclerView rvSimilarMovies;
    private SimilarMoviesAdapter similarMoviesAdapter;
    private List<Movie> similarMovies = new ArrayList<>();

    public static MovieDetailFragment newInstance(Movie movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MOVIE, (Serializable) movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movie = (Movie) getArguments().getSerializable(ARG_MOVIE);
            Log.d("MovieDetailFragment", "Movie received in onCreate: " +
                    (movie != null ? movie.getTitle() : "null"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Log.d("MovieDetailFragment", "onCreateView called");

        initializeViews(view);
        setupMovieData();
        setupClickListeners(view);
        loadSimilarMovies();

        return view;
    }

    private void initializeViews(View view) {
        Log.d("MovieDetailFragment", "Initializing views");

        // Inisialisasi semua view
        imgBackdrop = view.findViewById(R.id.imgBackdrop);
        imgMoviePoster = view.findViewById(R.id.imgMoviePoster);
        tvMovieTitle = view.findViewById(R.id.tvMovieTitle);
        tvMovieGenre = view.findViewById(R.id.tvMovieGenre);
        tvMovieRating = view.findViewById(R.id.tvMovieRating);
        tvMovieDuration = view.findViewById(R.id.tvMovieDuration);
        tvMovieYear = view.findViewById(R.id.tvMovieYear);
        tvMovieDescription = view.findViewById(R.id.tvMovieDescription);

        MaterialButton btnWatchNow = view.findViewById(R.id.btnWatchNow);
        MaterialButton btnTrailer = view.findViewById(R.id.btnTrailer);
        btnBookmarkIcon = view.findViewById(R.id.btnBookmarkIcon);
        ImageButton btnClose = view.findViewById(R.id.btnClose);

        // Similar movies
        rvSimilarMovies = view.findViewById(R.id.rvSimilarMovies);
        rvSimilarMovies.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        similarMoviesAdapter = new SimilarMoviesAdapter(similarMovies, this::onSimilarMovieClick);
        rvSimilarMovies.setAdapter(similarMoviesAdapter);

        Log.d("MovieDetailFragment", "Views initialized: " +
                (tvMovieTitle != null ? "tvMovieTitle not null" : "tvMovieTitle null"));
    }

    private void setupMovieData() {
        if (movie != null) {
            Log.d("MovieDetailFragment", "Setting up movie data for: " + movie.getTitle());
            Log.d("MovieDetailFragment", "Poster URL: " + movie.getPoster_url());

            // Pastikan views tidak null
            if (imgBackdrop == null || imgMoviePoster == null || tvMovieTitle == null) {
                Log.e("MovieDetailFragment", "Some views are null!");
                return;
            }

            // Load backdrop image - pakai poster sebagai backdrop
            String posterUrl = BASE_IMAGE_URL + movie.getPoster_url();
            Log.d("MovieDetailFragment", "Full poster URL: " + posterUrl);

            // Load untuk backdrop
            try {
                Glide.with(requireContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.backdrop_placeholder)
                        .centerCrop()
                        .into(imgBackdrop);
                Log.d("MovieDetailFragment", "Backdrop loaded");
            } catch (Exception e) {
                Log.e("MovieDetailFragment", "Error loading backdrop: " + e.getMessage());
            }

            // Load untuk poster
            try {
                Glide.with(requireContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.poster_placeholder)
                        .centerCrop()
                        .into(imgMoviePoster);
                Log.d("MovieDetailFragment", "Poster loaded");
            } catch (Exception e) {
                Log.e("MovieDetailFragment", "Error loading poster: " + e.getMessage());
            }

            // Set text data
            tvMovieTitle.setText(movie.getTitle());
            Log.d("MovieDetailFragment", "Title set: " + movie.getTitle());

            if (movie.getGenre() != null) {
                tvMovieGenre.setText(movie.getGenre());
                Log.d("MovieDetailFragment", "Genre set: " + movie.getGenre());
            }

            if (movie.getRating() != null) {
                tvMovieRating.setText(String.format("⭐ %.1f", movie.getRating()));
                Log.d("MovieDetailFragment", "Rating set: " + movie.getRating());
            }

            // Tahun rilis
            if (movie.getRelease_date() != null && !movie.getRelease_date().isEmpty()) {
                try {
                    String year = movie.getRelease_date().substring(0, 4);
                    tvMovieYear.setText(year);
                    Log.d("MovieDetailFragment", "Year set: " + year);
                } catch (Exception e) {
                    tvMovieYear.setText("2024");
                    Log.e("MovieDetailFragment", "Error parsing year: " + e.getMessage());
                }
            } else {
                tvMovieYear.setText("2024");
            }

            // Deskripsi
            if (movie.getOverview() != null && !movie.getOverview().isEmpty()) {
                tvMovieDescription.setText(movie.getOverview());
                Log.d("MovieDetailFragment", "Description set");
            } else {
                tvMovieDescription.setText("Deskripsi tidak tersedia.");
            }

            // Durasi
            if (movie.getRating() != null) {
                int durationMinutes = (int) (movie.getRating() * 15 + 60);
                if (durationMinutes < 60) {
                    tvMovieDuration.setText(durationMinutes + "m");
                } else {
                    int hours = durationMinutes / 60;
                    int minutes = durationMinutes % 60;
                    tvMovieDuration.setText(String.format("%dh %02dm", hours, minutes));
                }
                Log.d("MovieDetailFragment", "Duration set");
            } else {
                tvMovieDuration.setText("2h 00m");
            }

            Log.d("MovieDetailFragment", "All data set successfully");
        } else {
            Log.e("MovieDetailFragment", "Movie is null!");
        }
    }

    private void loadSimilarMovies() {
        similarMovies.clear();

        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            List<Movie> allMovies = mainActivity.getAllMovies();

            if (allMovies != null) {
                for (Movie m : allMovies) {
                    if (m.getId() != movie.getId() && similarMovies.size() < 5) {
                        similarMovies.add(m);
                    }
                }
                similarMoviesAdapter.updateList(similarMovies);
                Log.d("MovieDetailFragment", "Loaded " + similarMovies.size() + " similar movies");
            }
        }
    }

    private void onSimilarMovieClick(Movie similarMovie) {
        MovieDetailFragment newFragment = MovieDetailFragment.newInstance(similarMovie);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, newFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.btnClose).setOnClickListener(v -> {
            closeFragment();
        });

        view.findViewById(R.id.btnWatchNow).setOnClickListener(v -> {
            if (movie != null && movie.getVideo_url() != null && !movie.getVideo_url().isEmpty()) {
                Intent intent = new Intent(requireActivity(), PlayerActivity.class);
                intent.putExtra("MOVIE", movie);

                // Kirim semua movies untuk recommendations
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    List<Movie> allMovies = mainActivity.getAllMovies();
                    if (allMovies != null && !allMovies.isEmpty()) {
                        intent.putExtra("ALL_MOVIES", new ArrayList<>(allMovies));
                    }
                }

                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Video tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnTrailer).setOnClickListener(v -> {
            String query = movie.getTitle() + " official trailer";
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query)));
            startActivity(intent);
        });

        // Bookmark
        isBookmarked = BookmarkManager.isBookmarked(getContext(), movie);
        updateBookmarkIcon();

        btnBookmarkIcon.setOnClickListener(v -> {
            toggleBookmark();
        });
    }

    private void toggleBookmark() {
        isBookmarked = !isBookmarked;

        if (isBookmarked) {
            BookmarkManager.saveBookmark(getContext(), movie);
            Toast.makeText(getContext(), "Ditambahkan ke Bookmark", Toast.LENGTH_SHORT).show();
        } else {
            BookmarkManager.removeBookmark(getContext(), movie);
            Toast.makeText(getContext(), "Dihapus dari Bookmark", Toast.LENGTH_SHORT).show();
        }

        updateBookmarkIcon();
    }

    private void updateBookmarkIcon() {
        if (btnBookmarkIcon != null) {
            btnBookmarkIcon.setImageResource(
                    isBookmarked ? R.drawable.ic_bookmark_filled
                            : R.drawable.ic_bookmark_border
            );
        }
    }

    private void shareMovie() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Tonton film " + movie.getTitle() + " di ITATS Movie!\nRating: ⭐" + movie.getRating());
        startActivity(Intent.createChooser(shareIntent, "Bagikan film"));
    }

    private void showRatingDialog() {
        Toast.makeText(getContext(), "Rating feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void closeFragment() {
        // Kembalikan UI MainActivity sebelum menutup fragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).restoreMainUI();
        }

        getParentFragmentManager().beginTransaction()
                .remove(MovieDetailFragment.this)
                .commit();
        requireActivity().findViewById(R.id.fragmentContainer)
                .setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("MovieDetailFragment", "onDestroyView called");
    }
}