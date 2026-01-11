package com.example.nontonitats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nontonitats.R;
import com.example.nontonitats.model.Genre;
import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> {

    private List<Genre> genreList;
    private OnGenreClickListener listener;
    private String selectedGenre = "All Genres";

    public interface OnGenreClickListener {
        void onGenreClick(Genre genre);
    }

    public GenreAdapter(List<Genre> genreList, OnGenreClickListener listener) {
        this.genreList = genreList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_genre, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Genre genre = genreList.get(position);
        holder.tvGenre.setText(genre.getName());

        // Set background based on selection
        if (genre.getName().equals(selectedGenre)) {
            holder.tvGenre.setBackgroundResource(R.drawable.bg_genre_chip_selected);
            holder.tvGenre.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.white));
        } else {
            holder.tvGenre.setBackgroundResource(R.drawable.bg_genre_chip);
            holder.tvGenre.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(genre.getColorResId()));
        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedGenre(genre.getName());
            if (listener != null) {
                listener.onGenreClick(genre);
            }
        });
    }

    public void setSelectedGenre(String genre) {
        this.selectedGenre = genre;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    public void updateList(List<Genre> newList) {
        genreList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGenre;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGenre = itemView.findViewById(R.id.tvGenre);
        }
    }
}