package com.superhexagon.ui;

import com.superhexagon.model.GameMode;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PreparationScene {
    private Scene scene;
    private Consumer<String> sceneSwitcher;
    private Consumer<String> setPlayerName;
    private Supplier<GameMode> getGameMode;

    public PreparationScene(Consumer<String> sceneSwitcher, Consumer<String> setPlayerName, Supplier<GameMode> getGameMode) {
        this.sceneSwitcher = sceneSwitcher;
        this.setPlayerName = setPlayerName;
        this.getGameMode = getGameMode;
        createScene();
    }

    private void createScene() {
        Pane root = new Pane();
        scene = new Scene(root, 600, 400);

        Text title = new Text("Enter Your Name");
        title.setTranslateX(200);
        title.setTranslateY(100);

        TextField nameField = new TextField();
        nameField.setTranslateX(200);
        nameField.setTranslateY(150);

        Button startButton = new Button("Start Game");
        startButton.setTranslateX(250);
        startButton.setTranslateY(200);
        startButton.setOnAction(e -> {
            setPlayerName.accept(nameField.getText());
            sceneSwitcher.accept("game");
        });

        root.getChildren().addAll(title, nameField, startButton);
    }

    public Scene getScene() {
        return scene;
    }
}