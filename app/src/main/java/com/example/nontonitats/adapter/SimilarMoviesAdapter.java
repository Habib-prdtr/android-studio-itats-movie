package com.example.nontonitats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nontonitats.R;
import com.example.nontonitats.model.Movie;

import java.util.List;

public class SimilarMoviesAdapter extends RecyclerView.Adapter<SimilarMoviesAdapter.ViewHolder> {

    private List<Movie> movieList;
    private OnMovieClickListener listener;
    private static final String BASE_IMAGE_URL = "http://10.0.2.2/api-mobile/public/";

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public SimilarMoviesAdapter(List<Movie> movieList, OnMovieClickListener listener) {
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_similar_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvRating.setText(String.format("⭐ %.1f", movie.getRating()));

        // Load poster
        String posterUrl = BASE_IMAGE_URL + movie.getPoster_url();
        Glide.with(holder.itemView.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.poster_placeholder)
                .centerCrop()
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void updateList(List<Movie> newList) {
        movieList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}