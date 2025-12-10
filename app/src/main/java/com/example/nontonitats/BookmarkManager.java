package com.example.nontonitats;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.nontonitats.model.Movie;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class BookmarkManager {

    private static final String PREF_NAME = "bookmark_prefs";
    private static final String KEY_BOOKMARKS = "bookmarks";

    public static void saveBookmark(Context context, Movie movie) {
        ArrayList<Movie> list = getBookmarks(context);

        // hindari duplikasi
        for (Movie m : list) {
            if (m.getTitle().equals(movie.getTitle())) return;
        }

        list.add(movie);

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_BOOKMARKS, new Gson().toJson(list)).apply();
    }

    public static ArrayList<Movie> getBookmarks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_BOOKMARKS, null);

        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<Movie>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void removeBookmark(Context context, Movie movie) {
        ArrayList<Movie> list = getBookmarks(context);
        list.removeIf(m -> m.getTitle().equals(movie.getTitle()));

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_BOOKMARKS, new Gson().toJson(list)).apply();
    }

    public static boolean isBookmarked(Context context, Movie movie) {
        ArrayList<Movie> list = getBookmarks(context);

        for (Movie m : list) {
            if (m.getTitle().equals(movie.getTitle())) {
                return true;
            }
        }
        return false;
    }
}
