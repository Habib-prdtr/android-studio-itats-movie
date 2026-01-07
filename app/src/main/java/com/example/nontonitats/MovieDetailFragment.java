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

import com.bumptech.glide.Glide;
import com.example.nontonitats.model.Movie;

import java.io.Serializable;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE = "arg_movie";
    private Movie movie;

    private ImageButton btnBookmark;
    private boolean isBookmarked = false;

    private static final String BASE_IMAGE_URL = "http://10.0.2.2/api-mobile/public/";

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
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        ImageView imgMovieDetail = view.findViewById(R.id.imgMovieDetail);
        TextView tvMovieTitleDetail = view.findViewById(R.id.tvMovieTitleDetail);
        TextView tvMovieCategoryDetail = view.findViewById(R.id.tvMovieCategoryDetail);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvRating = view.findViewById(R.id.tvRating);
        TextView tvReleaseDate = view.findViewById(R.id.tvReleaseDate);
        ImageButton btnCloseDetail = view.findViewById(R.id.btnCloseDetail);
        btnBookmark = view.findViewById(R.id.btnBookmark);
        Button btnSearchOnline = view.findViewById(R.id.btnSearchOnline);

        if (movie != null) {
            String posterUrl = BASE_IMAGE_URL + movie.getPoster_url();

            Glide.with(requireContext())
                    .load(posterUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imgMovieDetail);

            tvMovieTitleDetail.setText(movie.getTitle());
            tvMovieCategoryDetail.setText(movie.getGenre());
            tvDescription.setText(movie.getOverview());
            tvRating.setText("⭐ " + movie.getRating());
            tvReleaseDate.setText("Rilis: " + movie.getRelease_date());
        }

        isBookmarked = BookmarkManager.isBookmarked(getContext(), movie);
        updateBookmarkIcon();

        btnCloseDetail.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .remove(MovieDetailFragment.this)
                    .commit();
            requireActivity().findViewById(R.id.fragmentContainer)
                    .setVisibility(View.GONE);
        });

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

        btnSearchOnline.setOnClickListener(v -> {
            String query = movie.getTitle() + " movie";
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
            startActivity(intent);
        });

        return view;
    }

    private void updateBookmarkIcon() {
        btnBookmark.setImageResource(
                isBookmarked ? R.drawable.ic_bookmark_filled
                        : R.drawable.ic_bookmark_border
        );
    }
}
