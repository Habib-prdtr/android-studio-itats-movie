package com.example.nontonitats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nontonitats.R;
import com.example.nontonitats.model.Movie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }

    public MovieAdapter(List<Movie> movies, OnItemClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvMovieTitle.setText(movie.getTitle());
        holder.imgMovie.setImageResource(movie.getImageRes());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void updateList(List<Movie> newMovies) {
        movies = newMovies;
        notifyDataSetChanged();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMovie;
        TextView tvMovieTitle;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMovie = itemView.findViewById(R.id.imgMovie);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
        }
    }
}
