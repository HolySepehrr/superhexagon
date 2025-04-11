package com.superhexagon.ui;

import com.superhexagon.model.GameMode;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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

public class PreparationScene {
    private Scene scene;
    private Consumer<String> sceneSwitcher;
    private Consumer<String> setPlayerName;
    private Supplier<GameMode> getGameMode;

    // رنگ‌ها از تم نارنجی
    private static final Color COLOR_LIGHT = Color.rgb(255, 147, 41);
    private static final Color COLOR_DARK = Color.rgb(200, 100, 0);

    public PreparationScene(Consumer<String> sceneSwitcher, Consumer<String> setPlayerName, Supplier<GameMode> getGameMode) {
        this.sceneSwitcher = sceneSwitcher;
        this.setPlayerName = setPlayerName;
        this.getGameMode = getGameMode;
        createScene();
    }

    private void createScene() {
        Pane root = new Pane();
        scene = new Scene(root, 600, 400, Color.BLACK);

        // عنوان
        Text title = new Text("Enter Your Name");
        title.setFont(Font.font("Verdana", 40));
        title.setFill(COLOR_LIGHT);
        title.setTranslateX(180);
        title.setTranslateY(80);

        // باکس عمودی برای المان‌ها
        VBox contentBox = new VBox(20);
        contentBox.setTranslateX(200);
        contentBox.setTranslateY(120);

        // فیلد اسم
        TextField nameField = new TextField();
        nameField.setPromptText("Your Name");
        nameField.setFont(Font.font("Verdana", 16));
        nameField.setPrefSize(200, 40);
        nameField.setStyle("-fx-background-color: #c86400; -fx-text-fill: white; -fx-background-radius: 10;");

        // استایل گرادیانت برای دکمه
        LinearGradient buttonGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, COLOR_LIGHT),
                new Stop(1, COLOR_DARK)
        );
        DropShadow shadow = new DropShadow(10, Color.BLACK);

        // دکمه Start
        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font("Verdana", 18));
        startButton.setPrefSize(140, 40);
        startButton.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;");
        startButton.setEffect(shadow);
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: linear-gradient(#ffaa40, #e07b00); -fx-text-fill: white; -fx-background-radius: 10;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;"));
        startButton.setOnAction(e -> {
            setPlayerName.accept(nameField.getText());
            sceneSwitcher.accept("game");
        });

        contentBox.getChildren().addAll(nameField, startButton);

        // انیمیشن Fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        root.getChildren().addAll(title, contentBox);
    }

    public Scene getScene() {
        return scene;
    }
}