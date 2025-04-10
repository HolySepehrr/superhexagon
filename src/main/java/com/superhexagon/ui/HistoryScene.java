package com.superhexagon.ui;

import com.superhexagon.model.GameRecord;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HistoryScene {
    private Scene scene;
    private Consumer<String> sceneSwitcher;
    private Supplier<List<GameRecord>> getGameHistory;

    public HistoryScene(Consumer<String> sceneSwitcher, Supplier<List<GameRecord>> getGameHistory) {
        this.sceneSwitcher = sceneSwitcher;
        this.getGameHistory = getGameHistory;
        createScene();
    }

    private void createScene() {
        Pane root = new Pane();
        scene = new Scene(root, 600, 400);

        Text title = new Text("Game History");
        title.setTranslateX(250);
        title.setTranslateY(30);

        // لیست تاریخچه با قابلیت اسکرول
        VBox historyBox = new VBox(10);
        List<GameRecord> history = getGameHistory.get();
        if (history.isEmpty()) {
            historyBox.getChildren().add(new Text("No games recorded yet."));
        } else {
            for (GameRecord record : history) {
                historyBox.getChildren().add(new Text(record.toString()));
            }
        }

        ScrollPane scrollPane = new ScrollPane(historyBox);
        scrollPane.setTranslateX(50);
        scrollPane.setTranslateY(50);
        scrollPane.setPrefSize(500, 300);

        Button backButton = new Button("Back to Main Menu");
        backButton.setTranslateX(230);
        backButton.setTranslateY(360);
        backButton.setOnAction(e -> sceneSwitcher.accept("main"));

        root.getChildren().addAll(title, scrollPane, backButton);
    }

    public Scene getScene() {
        return scene;
    }
}