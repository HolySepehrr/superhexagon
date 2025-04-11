package com.superhexagon.ui;

import com.superhexagon.util.SoundManager;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingsScene {
    private Scene scene;
    private Consumer<String> sceneSwitcher;
    private Supplier<SoundManager> soundManager;
    private Supplier<Boolean> isSaveHistoryEnabled;
    private Consumer<Boolean> setSaveHistoryEnabled;

    private static final Color COLOR_LIGHT = Color.rgb(255, 147, 41);
    private static final Color COLOR_DARK = Color.rgb(200, 100, 0);

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
        scene = new Scene(root, 600, 400, Color.BLACK);

        // عنوان
        Text title = new Text("Settings");
        title.setFont(Font.font("Verdana", 40));
        title.setFill(COLOR_LIGHT);
        title.setTranslateX(230);
        title.setTranslateY(80);

        // باکس عمودی
        VBox buttonBox = new VBox(20);
        buttonBox.setTranslateX(200);  // کمی جابجا کردم که دکمه‌های بزرگ‌تر وسط بمونن
        buttonBox.setTranslateY(120);

        // استایل گرادیانت
        LinearGradient buttonGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, COLOR_LIGHT),
                new Stop(1, COLOR_DARK)
        );
        DropShadow shadow = new DropShadow(10, Color.BLACK);

        // دکمه صدا
        Button soundToggleButton = new Button(soundManager.get().isSoundEnabled() ? "Sound: ON" : "Sound: OFF");
        styleButton(soundToggleButton, buttonGradient, shadow);
        soundToggleButton.setOnAction(e -> {
            boolean isEnabled = !soundManager.get().isSoundEnabled();
            soundManager.get().setSoundEnabled(isEnabled);
            soundToggleButton.setText(isEnabled ? "Sound: ON" : "Sound: OFF");
        });

        // دکمه تاریخچه (کشیده‌تر)
        Button historyToggleButton = new Button(isSaveHistoryEnabled.get() ? "Save History: ON" : "Save History: OFF");
        historyToggleButton.setFont(Font.font("Verdana", 18));
        historyToggleButton.setPrefSize(200, 40);  // عرض به 200 تغییر کرد
        historyToggleButton.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;");
        historyToggleButton.setEffect(shadow);
        historyToggleButton.setOnMouseEntered(e -> historyToggleButton.setStyle("-fx-background-color: linear-gradient(#ffaa40, #e07b00); -fx-text-fill: white; -fx-background-radius: 10;"));
        historyToggleButton.setOnMouseExited(e -> historyToggleButton.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;"));
        historyToggleButton.setOnAction(e -> {
            boolean isEnabled = !isSaveHistoryEnabled.get();
            setSaveHistoryEnabled.accept(isEnabled);
            historyToggleButton.setText(isEnabled ? "Save History: ON" : "Save History: OFF");
        });

        // دکمه بازگشت
        Button backButton = new Button("Back to Main Menu");
        styleButton(backButton, buttonGradient, shadow);
        backButton.setOnAction(e -> sceneSwitcher.accept("main"));

        buttonBox.getChildren().addAll(soundToggleButton, historyToggleButton, backButton);

        // انیمیشن Fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        root.getChildren().addAll(title, buttonBox);
    }

    private void styleButton(Button button, LinearGradient gradient, DropShadow shadow) {
        button.setFont(Font.font("Verdana", 18));
        button.setPrefSize(140, 40);
        button.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;");
        button.setEffect(shadow);
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: linear-gradient(#ffaa40, #e07b00); -fx-text-fill: white; -fx-background-radius: 10;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;"));
    }

    public Scene getScene() {
        return scene;
    }
}