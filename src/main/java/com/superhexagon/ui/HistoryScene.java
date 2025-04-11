package com.superhexagon.ui;

import com.superhexagon.model.GameRecord;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HistoryScene {
    private Scene scene;
    private Consumer<String> sceneSwitcher;
    private Supplier<List<GameRecord>> getGameHistory;

    private static final Color COLOR_LIGHT = Color.rgb(255, 147, 41);
    private static final Color COLOR_DARK = Color.rgb(200, 100, 0);

    public HistoryScene(Consumer<String> sceneSwitcher, Supplier<List<GameRecord>> getGameHistory) {
        this.sceneSwitcher = sceneSwitcher;
        this.getGameHistory = getGameHistory;
        createScene();
    }

    private void createScene() {
        Pane root = new Pane();
        scene = new Scene(root, 600, 400, Color.BLACK);

        // عنوان
        Text title = new Text("Game History");
        title.setFont(Font.font("Verdana", 40));
        title.setFill(COLOR_LIGHT);
        title.setTranslateX(200);
        title.setTranslateY(50);

        // لیست تاریخچه
        VBox historyBox = new VBox(10);
        List<GameRecord> history = getGameHistory.get();
        if (history.isEmpty()) {
            Text noRecords = new Text("No games recorded yet.");
            noRecords.setFont(Font.font("Verdana", 16));
            noRecords.setFill(Color.WHITE);
            historyBox.getChildren().add(noRecords);
        } else {
            for (GameRecord record : history) {
                Text recordText = new Text(record.toString());
                recordText.setFont(Font.font("Verdana", 16));
                recordText.setFill(Color.WHITE);
                historyBox.getChildren().add(recordText);
            }
        }

        ScrollPane scrollPane = new ScrollPane(historyBox);
        scrollPane.setTranslateX(50);
        scrollPane.setTranslateY(80);
        scrollPane.setPrefSize(500, 250);
        scrollPane.setStyle("-fx-background: #000000; -fx-background-color: transparent;");

        // استایل دکمه
        LinearGradient buttonGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, COLOR_LIGHT),
                new Stop(1, COLOR_DARK)
        );
        DropShadow shadow = new DropShadow(10, Color.BLACK);

        // دکمه بازگشت
        Button backButton = new Button("Back to Main Menu");
        backButton.setFont(Font.font("Verdana", 18));
        backButton.setPrefSize(140, 40);
        backButton.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;");
        backButton.setEffect(shadow);
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: linear-gradient(#ffaa40, #e07b00); -fx-text-fill: white; -fx-background-radius: 10;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;"));
        backButton.setTranslateX(230);
        backButton.setTranslateY(340);
        backButton.setOnAction(e -> sceneSwitcher.accept("main"));

        // انیمیشن Fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        root.getChildren().addAll(title, scrollPane, backButton);
    }

    public Scene getScene() {
        return scene;
    }
}