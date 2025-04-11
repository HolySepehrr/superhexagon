package com.superhexagon.util;

import javafx.scene.media.AudioClip;
import java.net.URL;

public class SoundManager {
    private AudioClip backgroundMusic;
    private AudioClip collisionSound;
    private AudioClip speedIncreaseSound;
    private boolean soundEnabled;
    private final DataManager dataManager;

    public SoundManager() {
        dataManager = new DataManager();
        DataManager.GameData gameData = dataManager.loadGameData();
        soundEnabled = gameData.isSoundEnabled();

        backgroundMusic = loadAudioClip("/easy_theme.mp3");
        collisionSound = loadAudioClip("/collision.mp3");
        speedIncreaseSound = loadAudioClip("/speed_increase.wav");
    }

    private AudioClip loadAudioClip(String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            System.err.println("Error: Could not find audio file: " + path);
            return null;
        }
        return new AudioClip(resource.toString());
    }

    public void setBackgroundMusic(String path) {
        AudioClip newBackgroundMusic = loadAudioClip(path);
        if (newBackgroundMusic != null) {
            backgroundMusic = newBackgroundMusic;
        }
    }

    public void playBackgroundMusic() {
        if (soundEnabled && backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void playCollisionSound() {
        if (soundEnabled && collisionSound != null) {
            collisionSound.play();
        }
    }

    public void playSpeedIncreaseSound() {
        if (soundEnabled && speedIncreaseSound != null) {
            speedIncreaseSound.play();
        }
    }

    public void stopSpeedIncreaseSound() {
        if (speedIncreaseSound != null) {
            speedIncreaseSound.stop();
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        DataManager.GameData gameData = dataManager.loadGameData();
        dataManager.saveGameData(gameData.getHighScores(), soundEnabled, gameData.getGameHistory());
    }
}