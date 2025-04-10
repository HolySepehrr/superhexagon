package com.superhexagon.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameRecord {
    private final LocalDateTime dateTime;
    private final String playerName;
    private final double duration; // مدت زمان بازی به ثانیه

    public GameRecord(String playerName, double duration) {
        this.dateTime = LocalDateTime.now();
        this.playerName = playerName;
        this.duration = duration;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int minutes = (int) (duration / 60);
        int seconds = (int) (duration % 60);
        return String.format("Date: %s | Player: %s | Duration: %02d:%02d",
                dateTime.format(formatter), playerName, minutes, seconds);
    }
}