package com.superhexagon.ui;

import com.superhexagon.model.GameMode;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import java.util.function.Consumer;

public class MainMenuScene {
    private Scene scene;
    private Consumer<String> sceneSwitcher;
    private Consumer<GameMode> setGameMode;

    public MainMenuScene(Consumer<String> sceneSwitcher, Consumer<GameMode> setGameMode) {
        this.sceneSwitcher = sceneSwitcher;
        this.setGameMode = setGameMode;
        createScene();
    }

    private void createScene() {
        Pane root = new Pane();
        scene = new Scene(root, 600, 400);

        Text title = new Text("Super Hexagon");
        title.setTranslateX(200);
        title.setTranslateY(100);

        Button easyButton = new Button("Easy");
        easyButton.setTranslateX(250);
        easyButton.setTranslateY(150);
        easyButton.setOnAction(e -> {
            setGameMode.accept(GameMode.EASY);
            sceneSwitcher.accept("preparation");
        });

        Button mediumButton = new Button("Medium");
        mediumButton.setTranslateX(250);
        mediumButton.setTranslateY(200);
        mediumButton.setOnAction(e -> {
            setGameMode.accept(GameMode.MEDIUM);
            sceneSwitcher.accept("preparation");
        });

        Button hardButton = new Button("Hard");
        hardButton.setTranslateX(250);
        hardButton.setTranslateY(250);
        hardButton.setOnAction(e -> {
            setGameMode.accept(GameMode.HARD);
            sceneSwitcher.accept("preparation");
        });

        Button settingsButton = new Button("Settings");
        settingsButton.setTranslateX(250);
        settingsButton.setTranslateY(300);
        settingsButton.setOnAction(e -> sceneSwitcher.accept("settings"));

        Button historyButton = new Button("Game History"); // اضافه کردن دکمه تاریخچه
        historyButton.setTranslateX(250);
        historyButton.setTranslateY(350);
        historyButton.setOnAction(e -> sceneSwitcher.accept("history"));

        root.getChildren().addAll(title, easyButton, mediumButton, hardButton, settingsButton, historyButton);
    }

    public Scene getScene() {
        return scene;
    }
}