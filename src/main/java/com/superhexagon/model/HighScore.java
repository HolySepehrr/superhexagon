package com.superhexagon.model;

public class HighScore {
    private GameMode difficulty;
    private double score;

    public HighScore(GameMode difficulty, double score) {
        this.difficulty = difficulty;
        this.score = score;
    }

    public GameMode getDifficulty() {
        return difficulty;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}