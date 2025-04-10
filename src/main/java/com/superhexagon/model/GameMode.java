package com.superhexagon.model;

public enum GameMode {
    EASY("Easy", 20, 3.0, 80),    // سرعت چرخش کمتر، فاصله بیشتر، سرعت موانع کمتر
    MEDIUM("Medium", 30, 2.0, 100), // حالت معمولی
    HARD("Hard", 40, 1.5, 120);    // سرعت چرخش بیشتر، فاصله کمتر، سرعت موانع بیشتر

    private final String name;
    private final double rotationSpeed;
    private final double obstacleSpawnInterval;
    private final double obstacleSpeed;

    GameMode(String name, double rotationSpeed, double obstacleSpawnInterval, double obstacleSpeed) {
        this.name = name;
        this.rotationSpeed = rotationSpeed;
        this.obstacleSpawnInterval = obstacleSpawnInterval;
        this.obstacleSpeed = obstacleSpeed;
    }

    public String getName() {
        return name;
    }

    public double getRotationSpeed() {
        return rotationSpeed;
    }

    public double getObstacleSpawnInterval() {
        return obstacleSpawnInterval;
    }

    public double getObstacleSpeed() {
        return obstacleSpeed;
    }
}