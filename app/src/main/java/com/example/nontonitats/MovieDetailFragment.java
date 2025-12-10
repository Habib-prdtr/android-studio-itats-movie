package com.example.nontonitats;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nontonitats.model.Movie;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE = "arg_movie";
    private Movie movie;

    private ImageButton btnBookmark;
    private boolean isBookmarked = false;

    public static MovieDetailFragment newInstance(Movie movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movie = (Movie) getArguments().getSerializable(ARG_MOVIE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // FIND VIEW
        ImageView imgMovieDetail = view.findViewById(R.id.imgMovieDetail);
        TextView tvMovieTitleDetail = view.findViewById(R.id.tvMovieTitleDetail);
        TextView tvMovieCategoryDetail = view.findViewById(R.id.tvMovieCategoryDetail);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvRating = view.findViewById(R.id.tvRating);
        TextView tvDuration = view.findViewById(R.id.tvDuration);
        ImageButton btnCloseDetail = view.findViewById(R.id.btnCloseDetail);
        btnBookmark = view.findViewById(R.id.btnBookmark);
        Button btnSearchOnline = view.findViewById(R.id.btnSearchOnline);

        // SET DATA MOVIE
        if (movie != null) {
            imgMovieDetail.setImageResource(movie.getImageRes());
            tvMovieTitleDetail.setText(movie.getTitle());
            tvMovieCategoryDetail.setText(movie.getCategory());
            tvDescription.setText(movie.getDescription());
            tvRating.setText("Rating: " + movie.getRating());
            tvDuration.setText("Durasi: " + movie.getDuration());
        }

        // CEK SUDAH BOOKMARK ATAU BELUM
        isBookmarked = BookmarkManager.isBookmarked(getContext(), movie);
        updateBookmarkIcon();

        // BUTTON CLOSE POPUP
        btnCloseDetail.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .remove(MovieDetailFragment.this)
                    .commit();
            getActivity().findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
        });

        // ACTION BOOKMARK
        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;

            if (isBookmarked) {
                BookmarkManager.saveBookmark(getContext(), movie);
                Toast.makeText(getContext(), "Ditambahkan ke Bookmark", Toast.LENGTH_SHORT).show();
            } else {
                BookmarkManager.removeBookmark(getContext(), movie);
                Toast.makeText(getContext(), "Dihapus dari Bookmark", Toast.LENGTH_SHORT).show();
            }

            updateBookmarkIcon();
        });

        // 🔥 BUTTON BUKA BROWSER (IMPLICIT INTENT)
        btnSearchOnline.setOnClickListener(v -> {
            if (movie != null) {

                String query = movie.getTitle() + " " + movie.getCategory() + " movie";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));

                startActivity(intent);
            }
        });

        return view;
    }

    private void updateBookmarkIcon() {
        if (isBookmarked) {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_border);
        }
    }
}
