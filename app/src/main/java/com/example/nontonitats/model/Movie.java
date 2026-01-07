package com.example.nontonitats.model;

import java.io.Serializable;

public class Movie implements Serializable {

    private int id;
    private String title;
    private String overview;
    private String poster_url;
    private double rating;
    private String genre;
    private String release_date;
    private String video_url;

    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getPoster_url() { return poster_url; }
    public double getRating() { return rating; }
    public String getGenre() { return genre; }
    public String getRelease_date() { return release_date; }

    public String getVideo_url() {
        return video_url;
    }
}
