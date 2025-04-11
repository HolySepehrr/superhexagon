package com.superhexagon.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.superhexagon.model.GameMode;
import com.superhexagon.model.HighScore;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String DATA_FILE = "game_data.json";
    private final Gson gson;

    public static class GameData {
        private List<HighScore> highScores = new ArrayList<>();
        private boolean soundEnabled = true;
        private List<GameRecord> gameHistory = new ArrayList<>();

        public List<HighScore> getHighScores() {
            return highScores;
        }

        public void setHighScores(List<HighScore> highScores) {
            this.highScores = highScores;
        }

        public boolean isSoundEnabled() {
            return soundEnabled;
        }

        public void setSoundEnabled(boolean soundEnabled) {
            this.soundEnabled = soundEnabled;
        }

        public List<GameRecord> getGameHistory() {
            return gameHistory;
        }

        public void setGameHistory(List<GameRecord> gameHistory) {
            this.gameHistory = gameHistory;
        }
    }

    public static class GameRecord {
        private GameMode difficulty;
        private double score;

        public GameRecord(GameMode difficulty, double score) {
            this.difficulty = difficulty;
            this.score = score;
        }

        public GameMode getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(GameMode difficulty) {
            this.difficulty = difficulty;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    public DataManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveGameData(List<HighScore> highScores, boolean soundEnabled, List<GameRecord> gameHistory) {
        GameData gameData = new GameData();
        gameData.setHighScores(highScores);
        gameData.setSoundEnabled(soundEnabled);
        gameData.setGameHistory(gameHistory);
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            gson.toJson(gameData, writer);
        } catch (IOException e) {
            System.err.println("Error saving game data: " + e.getMessage());
        }
    }

    public GameData loadGameData() {
        try (FileReader reader = new FileReader(DATA_FILE)) {
            return gson.fromJson(reader, new TypeToken<GameData>(){}.getType());
        } catch (IOException e) {
            System.err.println("Error loading game data: " + e.getMessage());
            return new GameData();
        }
    }
}