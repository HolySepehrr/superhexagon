package com.superhexagon.ui;

import com.superhexagon.model.GameMode;
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

public class MainMenuScene {
    private Scene scene;
    private Consumer<String> sceneSwitcher;
    private Consumer<GameMode> setGameMode;

    // رنگ‌ها از تم نارنجی GameScene
    private static final Color COLOR_LIGHT = Color.rgb(255, 147, 41);  // نارنجی روشن
    private static final Color COLOR_DARK = Color.rgb(200, 100, 0);   // نارنجی تیره

    public MainMenuScene(Consumer<String> sceneSwitcher, Consumer<GameMode> setGameMode) {
        this.sceneSwitcher = sceneSwitcher;
        this.setGameMode = setGameMode;
        createScene();
    }

    private void createScene() {
        Pane root = new Pane();
        scene = new Scene(root, 600, 400, Color.BLACK);

        // عنوان
        Text title = new Text("Super Hexagon");
        title.setFont(Font.font("Verdana", 40));
        title.setFill(COLOR_LIGHT);
        title.setTranslateX(180);  // وسط‌چین تقریبی
        title.setTranslateY(80);

        // باکس عمودی برای دکمه‌ها
        VBox buttonBox = new VBox(20);  // فاصله 20 پیکسل بین دکمه‌ها
        buttonBox.setTranslateX(230);
        buttonBox.setTranslateY(120);

        // استایل گرادیانت برای دکمه‌ها
        LinearGradient buttonGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, COLOR_LIGHT),
                new Stop(1, COLOR_DARK)
        );
        DropShadow shadow = new DropShadow(10, Color.BLACK);

        // دکمه Easy
        Button easyButton = createStyledButton("Easy", buttonGradient, shadow);
        easyButton.setOnAction(e -> {
            setGameMode.accept(GameMode.EASY);
            sceneSwitcher.accept("preparation");
        });

        // دکمه Medium
        Button mediumButton = createStyledButton("Medium", buttonGradient, shadow);
        mediumButton.setOnAction(e -> {
            setGameMode.accept(GameMode.MEDIUM);
            sceneSwitcher.accept("preparation");
        });

        // دکمه Hard
        Button hardButton = createStyledButton("Hard", buttonGradient, shadow);
        hardButton.setOnAction(e -> {
            setGameMode.accept(GameMode.HARD);
            sceneSwitcher.accept("preparation");
        });

        // دکمه Settings
        Button settingsButton = createStyledButton("Settings", buttonGradient, shadow);
        settingsButton.setOnAction(e -> sceneSwitcher.accept("settings"));

        // دکمه History
        Button historyButton = createStyledButton("Game History", buttonGradient, shadow);
        historyButton.setOnAction(e -> sceneSwitcher.accept("history"));

        // اضافه کردن دکمه‌ها به باکس
        buttonBox.getChildren().addAll(easyButton, mediumButton, hardButton, settingsButton, historyButton);

        // انیمیشن Fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        root.getChildren().addAll(title, buttonBox);
    }

    // متد برای ساخت دکمه با استایل
    private Button createStyledButton(String text, LinearGradient gradient, DropShadow shadow) {
        Button button = new Button(text);
        button.setFont(Font.font("Verdana", 18));
        button.setPrefSize(140, 40);
        button.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;");
        button.setEffect(shadow);

        // افکت hover
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: linear-gradient(#ffaa40, #e07b00); -fx-text-fill: white; -fx-background-radius: 10;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;"));

        return button;
    }

    public Scene getScene() {
        return scene;
    }
}