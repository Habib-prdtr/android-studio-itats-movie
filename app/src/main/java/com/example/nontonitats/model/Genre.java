package com.example.nontonitats.model;

public class Genre {
    private String name;
    private int colorResId;

    public Genre(String name, int colorResId) {
        this.name = name;
        this.colorResId = colorResId;
    }

    public String getName() { return name; }
    public int getColorResId() { return colorResId; }
}