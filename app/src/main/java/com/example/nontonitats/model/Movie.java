package com.example.nontonitats.model;

import java.io.Serializable;

public class Movie implements Serializable {
    private String title;
    private int imageRes;
    private String category;
    private String description;
    private String rating;
    private String duration;

    public Movie(String title, int imageRes, String category, String description, String rating, String duration) {
        this.title = title;
        this.imageRes = imageRes;
        this.category = category;
        this.description = description;
        this.rating = rating;
        this.duration = duration;
    }

    public String getTitle() { return title; }
    public int getImageRes() { return imageRes; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getRating() { return rating; }
    public String getDuration() { return duration; }
}
