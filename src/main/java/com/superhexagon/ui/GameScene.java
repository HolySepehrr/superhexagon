package com.superhexagon.ui;

import com.superhexagon.model.GameMode;
import com.superhexagon.model.HighScore;
import com.superhexagon.util.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GameScene {
    private Scene scene;
    private Pane root;
    private Polygon hexagon;
    private Polygon player;
    private List<Polygon> obstacles;
    private List<Polygon> obstacleTemplates;
    private List<Line> backgroundLines;
    private double rotationSpeed;
    private int playerSide;
    private double hexagonRotation;
    private double obstacleSpeed;
    private Supplier<SoundManager> soundManager;
    private Consumer<String> sceneSwitcher;
    private Consumer<HighScore> updateHighScore;
    private Supplier<HighScore> getHighScore;
    private Supplier<List<HighScore>> getAllHighScores;
    private Supplier<Integer> getSides;
    private Supplier<GameMode> getGameMode;
    private AnimationTimer gameLoop;
    private long startTime;
    private Text timerText;
    private Text highScoreText; // اضافه کردن متن برای نمایش بهترین رکورد
    private Text newRecordText;
    private Random random;
    private Pane gameOverPane;
    private Pane pausePane; // اضافه کردن پنل برای حالت توقف
    private boolean isPaused; // متغیر برای حالت توقف
    private int patternIndex;
    private int patternStep;
    private long spawnInterval;
    private Pane highScorePane;

    private static final double INITIAL_DISTANCE = 300;
    private static final double MIN_DISTANCE = 50;
    private static final double OBSTACLE_LENGTH = 20;
    private static final double OBSTACLE_WIDTH = 50;
    private static final double COLLISION_TOLERANCE = 5.0;
    private static final double BASE_SPAWN_DISTANCE = 2.25;

    public GameScene(Consumer<String> sceneSwitcher, Consumer<HighScore> updateHighScore, Supplier<HighScore> getHighScore,
                     Supplier<SoundManager> soundManager, Supplier<Integer> getSides, Supplier<GameMode> getGameMode,
                     Supplier<List<HighScore>> getAllHighScores) {
        this.sceneSwitcher = sceneSwitcher;
        this.updateHighScore = updateHighScore;
        this.getHighScore = getHighScore;
        this.soundManager = soundManager;
        this.getSides = getSides;
        this.getGameMode = getGameMode;
        this.getAllHighScores = getAllHighScores;
        this.obstacles = new ArrayList<>();
        this.obstacleTemplates = new ArrayList<>();
        this.backgroundLines = new ArrayList<>();
        this.hexagonRotation = 0;
        this.playerSide = 0;
        this.random = new Random();
        this.patternIndex = 0;
        this.patternStep = 0;
        this.isPaused = false; // مقداردهی اولیه
        createScene();
        createObstacleTemplates();
    }

    private void createScene() {
        root = new Pane();
        scene = new Scene(root, 600, 400, Color.BLACK);

        createBackground();

        hexagon = new Polygon();
        double radius = 50;
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            hexagon.getPoints().addAll(Math.cos(angle) * radius, Math.sin(angle) * radius);
        }
        hexagon.setFill(null);
        hexagon.setStroke(Color.ORANGE);
        hexagon.setStrokeWidth(2);
        hexagon.setTranslateX(300);
        hexagon.setTranslateY(200);

        player = new Polygon(0, -6, -3, 4, 3, 4);
        player.setFill(Color.BLACK);
        updatePlayerPosition();

        timerText = new Text("TIME 00:00");
        timerText.setFont(Font.font("Arial", 20));
        timerText.setFill(Color.WHITE);
        timerText.setTranslateX(500);
        timerText.setTranslateY(30);

        // اضافه کردن متن برای نمایش بهترین رکورد
        HighScore highScore = getHighScore.get();
        double highScoreValue = (highScore != null && highScore.getDifficulty() == getGameMode.get()) ? highScore.getScore() : 0;
        int highScoreMinutes = (int) (highScoreValue / 60);
        int highScoreSeconds = (int) (highScoreValue % 60);
        highScoreText = new Text(String.format("BEST %02d:%02d", highScoreMinutes, highScoreSeconds));
        highScoreText.setFont(Font.font("Arial", 20));
        highScoreText.setFill(Color.WHITE);
        highScoreText.setTranslateX(500);
        highScoreText.setTranslateY(60);

        newRecordText = new Text("NEW RECORD");
        newRecordText.setFont(Font.font("Arial", 20));
        newRecordText.setFill(Color.WHITE);
        newRecordText.setTranslateX(20);
        newRecordText.setTranslateY(30);
        newRecordText.setVisible(false);

        root.getChildren().addAll(backgroundLines);
        root.getChildren().addAll(hexagon, player, timerText, highScoreText, newRecordText);

        GameMode mode = getGameMode.get();
        if (mode == null) {
            throw new IllegalStateException("GameMode cannot be null when creating GameScene");
        }
        resetSpeeds();

        double spawnIntervalSeconds = BASE_SPAWN_DISTANCE / obstacleSpeed;
        spawnInterval = (long) (spawnIntervalSeconds * 1_000_000_000);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                playerSide = (playerSide - 1 + 6) % 6;
                updatePlayerPosition();
                System.out.println("Moved left, playerSide: " + playerSide);
            } else if (event.getCode() == KeyCode.RIGHT) {
                playerSide = (playerSide + 1) % 6;
                updatePlayerPosition();
                System.out.println("Moved right, playerSide: " + playerSide);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                double score = (System.nanoTime() - startTime) / 1_000_000_000.0;
                updateHighScore.accept(new HighScore(getGameMode.get(), score));
                gameLoop.stop();
                soundManager.get().stopBackgroundMusic();
                showGameOverScreen(score);
            } else if (event.getCode() == KeyCode.P) { // اضافه کردن کلید P برای توقف/ادامه
                if (isPaused) {
                    resumeGame();
                } else {
                    pauseGame();
                }
            }
        });

        root.requestFocus();
    }

    private void pauseGame() {
        isPaused = true;
        gameLoop.stop();
        soundManager.get().stopBackgroundMusic();

        pausePane = new Pane();
        pausePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        pausePane.setPrefSize(600, 400);

        Text pauseText = new Text("Paused");
        pauseText.setFont(Font.font("Arial", 40));
        pauseText.setFill(Color.WHITE);
        pauseText.setTranslateX(250);
        pauseText.setTranslateY(150);

        Button resumeButton = new Button("Resume");
        resumeButton.setTranslateX(260);
        resumeButton.setTranslateY(200);
        resumeButton.setOnAction(e -> resumeGame());

        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.setTranslateX(250);
        mainMenuButton.setTranslateY(250);
        mainMenuButton.setOnAction(e -> {
            double score = (System.nanoTime() - startTime) / 1_000_000_000.0;
            updateHighScore.accept(new HighScore(getGameMode.get(), score));
            root.getChildren().remove(pausePane);
            sceneSwitcher.accept("main");
        });

        pausePane.getChildren().addAll(pauseText, resumeButton, mainMenuButton);
        root.getChildren().add(pausePane);
    }

    private void resumeGame() {
        isPaused = false;
        root.getChildren().remove(pausePane);
        gameLoop.start();
        soundManager.get().playBackgroundMusic();
    }

    private void resetSpeeds() {
        GameMode mode = getGameMode.get();
        switch (mode) {
            case EASY:
                rotationSpeed = 1.0;
                obstacleSpeed = 1.0;
                soundManager.get().setBackgroundMusic("/easy_theme.mp3");
                break;
            case MEDIUM:
                rotationSpeed = 1.5;
                obstacleSpeed = 1.5;
                soundManager.get().setBackgroundMusic("/medium_theme.mp3");
                break;
            case HARD:
                rotationSpeed = 2.0;
                obstacleSpeed = 2.0;
                soundManager.get().setBackgroundMusic("/hard_theme.mp3");
                break;
        }
    }

    private void createBackground() {
        double maxRadius = 300;
        Color color1 = Color.rgb(200, 50, 0);
        Color color2 = Color.rgb(100, 30, 0);

        for (int i = 0; i < 6; i++) {
            double startAngle = 60 * i;
            double endAngle = 60 * (i + 1);
            int numLines = 10;
            double angleStep = (endAngle - startAngle) / numLines;

            for (int j = 0; j < numLines; j++) {
                double angle = Math.toRadians(startAngle + j * angleStep);
                double endX = 300 + Math.cos(angle) * maxRadius;
                double endY = 200 + Math.sin(angle) * maxRadius;
                Line line = new Line(300, 200, endX, endY);
                line.setStroke(j % 2 == 0 ? color1 : color2);
                line.setStrokeWidth(5);
                backgroundLines.add(line);
            }
        }
    }

    private void createObstacleTemplates() {
        for (int side = 0; side < 6; side++) {
            double baseAngle = side * 60;
            double distance = INITIAL_DISTANCE;

            double theta1 = Math.toRadians(baseAngle);
            double theta2 = Math.toRadians(baseAngle + 60);

            double xB = distance * Math.cos(theta1);
            double yB = distance * Math.sin(theta1);
            double xC = distance * Math.cos(theta2);
            double yC = distance * Math.sin(theta2);
            double xA = (distance + OBSTACLE_LENGTH) * Math.cos(theta1);
            double yA = (distance + OBSTACLE_LENGTH) * Math.sin(theta1);
            double xD = (distance + OBSTACLE_LENGTH) * Math.cos(theta2);
            double yD = (distance + OBSTACLE_LENGTH) * Math.sin(theta2);

            Polygon obstacle = new Polygon(
                    xA, yA,
                    xB, yB,
                    xC, yC,
                    xD, yD
            );
            obstacle.setFill(Color.ORANGE);
            obstacle.setTranslateX(300);
            obstacle.setTranslateY(200);
            if (side == 0) {
                obstacle.setRotate(0);
            } else if (side == 1) {
                obstacle.setRotate(0);
            } else if (side == 2) {
                obstacle.setRotate(0);
            } else if (side == 3) {
                obstacle.setRotate(180 + 180);
            } else if (side == 4) {
                obstacle.setRotate(0);
            } else if (side == 5) {
                obstacle.setRotate(0);
            }
            obstacle.setUserData(new double[]{baseAngle, distance});
            obstacleTemplates.add(obstacle);
        }
    }

    private List<Integer> getNextObstaclePattern() {
        List<Integer> selectedSides = new ArrayList<>();
        int numObstacles = random.nextInt(5) + 1;

        List<List<Integer>> patterns = new ArrayList<>();
        patterns.add(new ArrayList<>(List.of(0, 1, 2, 3, 4, 5)));
        patterns.add(new ArrayList<>(List.of(0, 3, 1, 4, 2, 5)));
        patterns.add(new ArrayList<>(List.of(0, 2, 4, 1, 3, 5)));
        List<Integer> restrictedRandom = new ArrayList<>();
        restrictedRandom.add(0);
        for (int i = 0; i < 5; i++) {
            restrictedRandom.add(random.nextInt(5) + 1);
        }
        patterns.add(restrictedRandom);

        if (patternStep == 0) {
            patternIndex = random.nextInt(patterns.size());
        }

        List<Integer> currentPattern = patterns.get(patternIndex);
        for (int i = 0; i < numObstacles; i++) {
            int side = currentPattern.get(patternStep % currentPattern.size());
            if (!selectedSides.contains(side)) {
                selectedSides.add(side);
            }
            patternStep++;
            if (patternStep >= currentPattern.size()) {
                patternStep = 0;
            }
        }

        return selectedSides;
    }

    private void spawnObstacle() {
        List<Integer> selectedSides = getNextObstaclePattern();

        for (int side : selectedSides) {
            int templateIndex = side;

            Polygon template = obstacleTemplates.get(templateIndex);
            Polygon obstacle = new Polygon();
            obstacle.getPoints().addAll(template.getPoints());
            obstacle.setFill(template.getFill());
            obstacle.setTranslateX(template.getTranslateX());
            obstacle.setTranslateY(template.getTranslateY());
            obstacle.setRotate(template.getRotate());
            obstacle.setUserData(new double[]{(double) side * 60, INITIAL_DISTANCE});

            obstacles.add(obstacle);
            root.getChildren().add(obstacle);
        }
    }

    private void updatePlayerPosition() {
        double distanceFromCenter = 60;
        double baseAngle = playerSide * 60 + 30;
        double totalAngle = baseAngle + hexagonRotation;
        double x = 300 + Math.cos(Math.toRadians(totalAngle)) * distanceFromCenter;
        double y = 200 + Math.sin(Math.toRadians(totalAngle)) * distanceFromCenter;
        player.setTranslateX(x);
        player.setTranslateY(y);
        player.setRotate(totalAngle + 90);
    }

    private double calculateDistanceToObstacle(Polygon player, Polygon obstacle) {
        double playerX = player.getTranslateX();
        double playerY = player.getTranslateY();
        double obstacleX = obstacle.getTranslateX();
        double obstacleY = obstacle.getTranslateY();
        return Math.sqrt(Math.pow(playerX - obstacleX, 2) + Math.pow(playerY - obstacleY, 2));
    }

    private void showGameOverScreen(double currentScore) {
        gameOverPane = new Pane();
        gameOverPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        gameOverPane.setPrefSize(600, 400);

        Text gameOverText = new Text("Game Over");
        gameOverText.setFont(Font.font("Arial", 40));
        gameOverText.setFill(Color.RED);
        gameOverText.setTranslateX(230);
        gameOverText.setTranslateY(100);

        int currentMinutes = (int) (currentScore / 60);
        int currentSeconds = (int) (currentScore % 60);
        Text currentScoreText = new Text(String.format("Your Score: %02d:%02d", currentMinutes, currentSeconds));
        currentScoreText.setFont(Font.font("Arial", 20));
        currentScoreText.setFill(Color.WHITE);
        currentScoreText.setTranslateX(240);
        currentScoreText.setTranslateY(150);

        HighScore highScore = getHighScore.get();
        double highScoreValue = (highScore != null && highScore.getDifficulty() == getGameMode.get()) ? highScore.getScore() : 0;
        int highScoreMinutes = (int) (highScoreValue / 60);
        int highScoreSeconds = (int) (highScoreValue % 60);
        Text highScoreText = new Text(String.format("High Score (%s): %02d:%02d", getGameMode.get(), highScoreMinutes, highScoreSeconds));
        highScoreText.setFont(Font.font("Arial", 20));
        highScoreText.setFill(Color.WHITE);
        highScoreText.setTranslateX(220);
        highScoreText.setTranslateY(180);

        Button restartButton = new Button("Restart");
        restartButton.setTranslateX(260);
        restartButton.setTranslateY(220);
        restartButton.setOnAction(e -> {
            root.getChildren().remove(gameOverPane);
            resetGame();
            startGame();
        });

        Button changeDifficultyButton = new Button("Change Difficulty");
        changeDifficultyButton.setTranslateX(240);
        changeDifficultyButton.setTranslateY(260);
        changeDifficultyButton.setOnAction(e -> {
            root.getChildren().remove(gameOverPane);
            sceneSwitcher.accept("preparation");
        });

        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.setTranslateX(250);
        mainMenuButton.setTranslateY(300);
        mainMenuButton.setOnAction(e -> {
            root.getChildren().remove(gameOverPane);
            sceneSwitcher.accept("main");
        });

        Button soundToggleButton = new Button(soundManager.get().isSoundEnabled() ? "Sound: ON" : "Sound: OFF");
        soundToggleButton.setTranslateX(250);
        soundToggleButton.setTranslateY(340);
        soundToggleButton.setOnAction(e -> {
            boolean isEnabled = !soundManager.get().isSoundEnabled();
            soundManager.get().setSoundEnabled(isEnabled);
            soundToggleButton.setText(isEnabled ? "Sound: ON" : "Sound: OFF");
            if (isEnabled) {
                soundManager.get().playSpeedIncreaseSound();
            }
        });

        Button highScoreButton = new Button("High Scores");
        highScoreButton.setTranslateX(250);
        highScoreButton.setTranslateY(380);
        highScoreButton.setOnAction(e -> showHighScoreBoard());

        Button historyButton = new Button("Game History"); // اضافه کردن دکمه تاریخچه
        historyButton.setTranslateX(250);
        historyButton.setTranslateY(420);
        historyButton.setOnAction(e -> sceneSwitcher.accept("history"));

        Button settingsButton = new Button("Settings");
        settingsButton.setTranslateX(250);
        settingsButton.setTranslateY(460);
        settingsButton.setOnAction(e -> sceneSwitcher.accept("settings"));

        gameOverPane.getChildren().addAll(gameOverText, currentScoreText, highScoreText, restartButton, changeDifficultyButton, mainMenuButton, soundToggleButton, highScoreButton, historyButton, settingsButton);
        root.getChildren().add(gameOverPane);

        soundManager.get().stopBackgroundMusic();
        soundManager.get().playSpeedIncreaseSound();
    }

    private void showHighScoreBoard() {
        if (highScorePane != null) {
            root.getChildren().remove(highScorePane);
        }

        highScorePane = new Pane();
        highScorePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        highScorePane.setPrefSize(600, 400);

        Text title = new Text("High Scores");
        title.setFont(Font.font("Arial", 40));
        title.setFill(Color.YELLOW);
        title.setTranslateX(220);
        title.setTranslateY(50);

        Text easyScoreText = new Text("Easy: 00:00");
        easyScoreText.setFont(Font.font("Arial", 20));
        easyScoreText.setFill(Color.WHITE);
        easyScoreText.setTranslateX(250);
        easyScoreText.setTranslateY(100);

        Text mediumScoreText = new Text("Medium: 00:00");
        mediumScoreText.setFont(Font.font("Arial", 20));
        mediumScoreText.setFill(Color.WHITE);
        mediumScoreText.setTranslateX(250);
        mediumScoreText.setTranslateY(140);

        Text hardScoreText = new Text("Hard: 00:00");
        hardScoreText.setFont(Font.font("Arial", 20));
        hardScoreText.setFill(Color.WHITE);
        hardScoreText.setTranslateX(250);
        hardScoreText.setTranslateY(180);

        List<HighScore> allHighScores = getAllHighScores.get();
        for (HighScore highScore : allHighScores) {
            double score = highScore.getScore();
            int minutes = (int) (score / 60);
            int seconds = (int) (score % 60);
            switch (highScore.getDifficulty()) {
                case EASY:
                    easyScoreText.setText(String.format("Easy: %02d:%02d", minutes, seconds));
                    break;
                case MEDIUM:
                    mediumScoreText.setText(String.format("Medium: %02d:%02d", minutes, seconds));
                    break;
                case HARD:
                    hardScoreText.setText(String.format("Hard: %02d:%02d", minutes, seconds));
                    break;
            }
        }

        Button backButton = new Button("Back");
        backButton.setTranslateX(270);
        backButton.setTranslateY(300);
        backButton.setOnAction(e -> root.getChildren().remove(highScorePane));

        highScorePane.getChildren().addAll(title, easyScoreText, mediumScoreText, hardScoreText, backButton);
        root.getChildren().add(highScorePane);
    }

    private void resetGame() {
        for (Polygon obstacle : obstacles) {
            root.getChildren().remove(obstacle);
        }
        obstacles.clear();

        playerSide = 0;
        updatePlayerPosition();

        hexagonRotation = 0;
        hexagon.setRotate(hexagonRotation);

        patternIndex = 0;
        patternStep = 0;

        timerText.setText("TIME 00:00");

        resetSpeeds();
        double spawnIntervalSeconds = BASE_SPAWN_DISTANCE / obstacleSpeed;
        spawnInterval = (long) (spawnIntervalSeconds * 1_000_000_000);
    }

    public void startGame() {
        soundManager.get().stopSpeedIncreaseSound();
        soundManager.get().playBackgroundMusic();
        startTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
            private long lastObstacleSpawn = 0;
            private long lastSpeedIncrease = 0;

            @Override
            public void handle(long now) {
                if (isPaused) return; // اگه بازی توقف شده، ادامه نده

                hexagonRotation += rotationSpeed;
                hexagon.setRotate(hexagonRotation);

                for (Line line : backgroundLines) {
                    line.setRotate(hexagonRotation);
                }

                updatePlayerPosition();

                if (now - lastObstacleSpawn > spawnInterval) {
                    spawnObstacle();
                    lastObstacleSpawn = now;
                }

                if (now - lastSpeedIncrease > 10_000_000_000L) {
                    rotationSpeed *= 1.1;
                    obstacleSpeed *= 1.1;
                    double spawnIntervalSeconds = BASE_SPAWN_DISTANCE / obstacleSpeed;
                    spawnInterval = (long) (spawnIntervalSeconds * 1_000_000_000);
                    lastSpeedIncrease = now;
                    System.out.println("Rotation speed increased to: " + rotationSpeed);
                }

                double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
                int minutes = (int) (elapsedSeconds / 60);
                int seconds = (int) (elapsedSeconds % 60);
                timerText.setText(String.format("TIME %02d:%02d", minutes, seconds));

                for (Polygon obstacle : new ArrayList<>(obstacles)) {
                    double[] data = (double[]) obstacle.getUserData();
                    double baseAngle = data[0];
                    double distance = data[1];

                    distance -= obstacleSpeed;
                    if (distance < MIN_DISTANCE) {
                        obstacles.remove(obstacle);
                        root.getChildren().remove(obstacle);
                        continue;
                    }

                    double theta1 = Math.toRadians(baseAngle + hexagonRotation);
                    double theta2 = Math.toRadians(baseAngle + 60 + hexagonRotation);

                    double xB = distance * Math.cos(theta1);
                    double yB = distance * Math.sin(theta1);
                    double xC = distance * Math.cos(theta2);
                    double yC = distance * Math.sin(theta2);
                    double xA = (distance + OBSTACLE_LENGTH) * Math.cos(theta1);
                    double yA = (distance + OBSTACLE_LENGTH) * Math.sin(theta1);
                    double xD = (distance + OBSTACLE_LENGTH) * Math.cos(theta2);
                    double yD = (distance + OBSTACLE_LENGTH) * Math.sin(theta2);

                    obstacle.getPoints().setAll(
                            xA, yA,
                            xB, yB,
                            xC, yC,
                            xD, yD
                    );

                    obstacle.setUserData(new double[]{baseAngle, distance});

                    Shape intersection = Shape.intersect(player, obstacle);
                    double distanceToObstacle = calculateDistanceToObstacle(player, obstacle);
                    if (intersection.getBoundsInLocal().getWidth() != -1 && distanceToObstacle < 65 + COLLISION_TOLERANCE) {
                        System.out.println("Collision detected! Distance: " + distanceToObstacle);
                        soundManager.get().playCollisionSound();
                        double score = (System.nanoTime() - startTime) / 1_000_000_000.0;
                        updateHighScore.accept(new HighScore(getGameMode.get(), score));
                        gameLoop.stop();
                        soundManager.get().stopBackgroundMusic();
                        showGameOverScreen(score);
                    }
                }
            }
        };
        gameLoop.start();
    }

    public Scene getScene() {
        return scene;
    }
}