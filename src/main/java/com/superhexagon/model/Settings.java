package com.superhexagon.model;

public class Settings {
    private boolean musicEnabled;
    private int polygonSides;

    public Settings() {
        this.musicEnabled = true; // مقدار پیش‌فرض
        this.polygonSides = 6; // مقدار پیش‌فرض
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public int getPolygonSides() {
        return polygonSides;
    }

    public void setPolygonSides(int polygonSides) {
        this.polygonSides = polygonSides;
    }
}