package com.superhexagon.ui;

import com.superhexagon.util.SoundManager;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingsScene {
    private Scene scene;
    private Consumer<String> sceneSwitcher;
    private Supplier<SoundManager> soundManager;
    private Supplier<Boolean> isSaveHistoryEnabled;
    private Consumer<Boolean> setSaveHistoryEnabled;

    public SettingsScene(Consumer<String> sceneSwitcher, Supplier<SoundManager> soundManager,
                         Supplier<Boolean> isSaveHistoryEnabled, Consumer<Boolean> setSaveHistoryEnabled) {
        this.sceneSwitcher = sceneSwitcher;
        this.soundManager = soundManager;
        this.isSaveHistoryEnabled = isSaveHistoryEnabled;
        this.setSaveHistoryEnabled = setSaveHistoryEnabled;
        createScene();
    }

    private void createScene() {
        Pane root = new Pane();
        scene = new Scene(root, 600, 400);

        Text title = new Text("Settings");
        title.setTranslateX(250);
        title.setTranslateY(100);

        // دکمه برای روشن/خاموش کردن صدا
        Button soundToggleButton = new Button(soundManager.get().isSoundEnabled() ? "Sound: ON" : "Sound: OFF");
        soundToggleButton.setTranslateX(250);
        soundToggleButton.setTranslateY(150);
        soundToggleButton.setOnAction(e -> {
            boolean isEnabled = !soundManager.get().isSoundEnabled();
            soundManager.get().setSoundEnabled(isEnabled);
            soundToggleButton.setText(isEnabled ? "Sound: ON" : "Sound: OFF");
        });

        // دکمه برای فعال/غیرفعال کردن ذخیره تاریخچه
        Button historyToggleButton = new Button(isSaveHistoryEnabled.get() ? "Save History: ON" : "Save History: OFF");
        historyToggleButton.setTranslateX(250);
        historyToggleButton.setTranslateY(200);
        historyToggleButton.setOnAction(e -> {
            boolean isEnabled = !isSaveHistoryEnabled.get();
            setSaveHistoryEnabled.accept(isEnabled);
            historyToggleButton.setText(isEnabled ? "Save History: ON" : "Save History: OFF");
        });

        Button backButton = new Button("Back to Main Menu");
        backButton.setTranslateX(230);
        backButton.setTranslateY(250);
        backButton.setOnAction(e -> sceneSwitcher.accept("main"));

        root.getChildren().addAll(title, soundToggleButton, historyToggleButton, backButton);
    }

    public Scene getScene() {
        return scene;
    }
}