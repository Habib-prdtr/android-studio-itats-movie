package com.example.nontonitats.response;

import com.example.nontonitats.model.Movie;

import java.util.List;

public class MovieResponse {
    private boolean success;
    private List<Movie> data;

    public List<Movie> getData() {
        return data;
    }
}

