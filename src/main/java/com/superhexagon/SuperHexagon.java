package com.superhexagon;

import com.superhexagon.model.GameMode;
import com.superhexagon.model.GameRecord;
import com.superhexagon.model.HighScore;
import com.superhexagon.ui.GameScene;
import com.superhexagon.ui.MainMenuScene;
import com.superhexagon.ui.PreparationScene;
import com.superhexagon.ui.SettingsScene;
import com.superhexagon.ui.HistoryScene; // اضافه کردن ایمپورت
import com.superhexagon.util.SoundManager;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SuperHexagon extends Application {
    private Stage primaryStage;
    private SoundManager soundManager;
    private GameMode selectedMode;
    private List<HighScore> highScores;
    private List<GameRecord> gameHistory; // لیست تاریخچه بازی‌ها
    private String playerName;
    private int sides = 6;
    private boolean saveHistoryEnabled = true; // تنظیمات ذخیره تاریخچه

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.soundManager = new SoundManager();
        this.highScores = new ArrayList<>();
        this.gameHistory = new ArrayList<>(); // مقداردهی اولیه تاریخچه
        this.selectedMode = GameMode.EASY;

        primaryStage.setUserData(this);

        primaryStage.setTitle("Super Hexagon");
        showMainMenu();
        primaryStage.show();
    }

    private void showMainMenu() {
        MainMenuScene mainMenuScene = new MainMenuScene(this::switchScene, this::setGameMode);
        Scene scene = mainMenuScene.getScene();
        primaryStage.setScene(scene);
    }

    private void switchScene(String sceneName) {
        switch (sceneName) {
            case "preparation":
                PreparationScene preparationScene = new PreparationScene(this::switchScene, this::setPlayerName, this::getGameMode);
                primaryStage.setScene(preparationScene.getScene());
                break;
            case "game":
                GameScene gameScene = new GameScene(this::switchScene, this::updateHighScore, this::getHighScoreForMode, () -> soundManager, () -> sides, this::getGameMode, this::getAllHighScores);
                primaryStage.setScene(gameScene.getScene());
                gameScene.startGame();
                break;
            case "settings":
                SettingsScene settingsScene = new SettingsScene(this::switchScene, () -> soundManager, this::isSaveHistoryEnabled, this::setSaveHistoryEnabled);
                primaryStage.setScene(settingsScene.getScene());
                break;
            case "history": // اضافه کردن کیس برای HistoryScene
                HistoryScene historyScene = new HistoryScene(this::switchScene, this::getGameHistory);
                primaryStage.setScene(historyScene.getScene());
                break;
            case "main":
                showMainMenu();
                break;
        }
    }

    private void setGameMode(GameMode mode) {
        this.selectedMode = mode;
    }

    public GameMode getGameMode() {
        return selectedMode;
    }

    private void setPlayerName(String name) {
        this.playerName = name;
    }

    private void updateHighScore(HighScore newScore) {
        HighScore existingScore = highScores.stream()
                .filter(score -> score.getDifficulty() == newScore.getDifficulty())
                .findFirst()
                .orElse(null);

        if (existingScore == null || newScore.getScore() > existingScore.getScore()) {
            highScores.removeIf(score -> score.getDifficulty() == newScore.getDifficulty());
            highScores.add(newScore);
        }

        // اضافه کردن بازی به تاریخچه
        if (saveHistoryEnabled && playerName != null) {
            gameHistory.add(new GameRecord(playerName, newScore.getScore()));
        }
    }

    private HighScore getHighScoreForMode() {
        return highScores.stream()
                .filter(score -> score.getDifficulty() == selectedMode)
                .findFirst()
                .orElse(null);
    }

    public List<HighScore> getAllHighScores() {
        return highScores;
    }

    public List<GameRecord> getGameHistory() {
        return gameHistory;
    }

    public boolean isSaveHistoryEnabled() {
        return saveHistoryEnabled;
    }

    public void setSaveHistoryEnabled(boolean enabled) {
        this.saveHistoryEnabled = enabled;
    }

    public static void main(String[] args) {
        launch(args);
    }
}