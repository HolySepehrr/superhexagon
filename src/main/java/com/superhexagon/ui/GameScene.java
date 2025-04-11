package com.superhexagon.ui;

import com.superhexagon.model.GameMode;
import com.superhexagon.model.HighScore;
import com.superhexagon.util.DataManager;
import com.superhexagon.util.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;

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
    private List<Polygon> backgroundSections;
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
    private Text highScoreText;
    private Text newRecordText;
    private Random random;
    private Pane gameOverPane;
    private Pane pausePane;
    private boolean isPaused;
    private int patternIndex;
    private int patternStep;
    private long spawnInterval;
    private Pane highScorePane;
    private final DataManager dataManager;

    private Color colorLight = Color.rgb(255, 147, 41);
    private Color colorDark = Color.rgb(200, 100, 0);
    private Color colorObstacle = Color.rgb(255, 120, 0);
    private long lastColorChange = 0;
    private static final long COLOR_CHANGE_INTERVAL = 10_000_000_000L;
    private List<Color[]> colorThemes;

    private static final double INITIAL_DISTANCE = 300;
    private static final double MIN_DISTANCE = 50;
    private static final double OBSTACLE_LENGTH = 20;
    private static final double OBSTACLE_WIDTH = 50;
    private static final double COLLISION_TOLERANCE = 5.0;

    private boolean isSecondPattern = false;

    public GameScene(Consumer<String> sceneSwitcher, Consumer<HighScore> updateHighScore, Supplier<HighScore> getHighScore,
                     Supplier<SoundManager> soundManager, Supplier<Integer> getSides, Supplier<GameMode> getGameMode,
                     Supplier<List<HighScore>> getAllHighScores) {
        this.sceneSwitcher = sceneSwitcher;
        this.dataManager = new DataManager();
        this.updateHighScore = highScore -> {
            DataManager.GameData gameData = dataManager.loadGameData();
            List<HighScore> highScores = gameData.getHighScores();
            List<DataManager.GameRecord> gameHistory = gameData.getGameHistory();

            // ذخیره همه رکوردها بدون شرط
            highScores.add(highScore);

            // اضافه کردن بازی به تاریخچه (فقط سختی و امتیاز)
            gameHistory.add(new DataManager.GameRecord(highScore.getDifficulty(), highScore.getScore()));

            // ذخیره همه داده‌ها
            dataManager.saveGameData(highScores, soundManager.get().isSoundEnabled(), gameHistory);
        };
        this.getHighScore = () -> {
            DataManager.GameData gameData = dataManager.loadGameData();
            return dataManager.loadGameData().getHighScores().stream()
                    .filter(hs -> hs.getDifficulty() == getGameMode.get())
                    .findFirst().orElse(null);
        };
        this.getAllHighScores = () -> dataManager.loadGameData().getHighScores();
        this.soundManager = soundManager;
        this.getSides = getSides;
        this.getGameMode = getGameMode;
        this.getAllHighScores = getAllHighScores;
        this.obstacles = new ArrayList<>();
        this.obstacleTemplates = new ArrayList<>();
        this.backgroundSections = new ArrayList<>();
        this.hexagonRotation = 0;
        this.playerSide = 0;
        this.random = new Random();
        this.patternIndex = 0;
        this.patternStep = 0;
        this.isPaused = false;

        colorThemes = new ArrayList<>();
        colorThemes.add(new Color[]{Color.rgb(255, 147, 41), Color.rgb(200, 100, 0), Color.rgb(255, 120, 0)});
        colorThemes.add(new Color[]{Color.rgb(100, 255, 100), Color.rgb(0, 150, 0), Color.rgb(0, 200, 0)});
        colorThemes.add(new Color[]{Color.rgb(100, 200, 255), Color.rgb(0, 100, 200), Color.rgb(0, 150, 255)});
        colorThemes.add(new Color[]{Color.rgb(255, 150, 200), Color.rgb(200, 50, 150), Color.rgb(255, 80, 180)});
        colorThemes.add(new Color[]{Color.rgb(200, 100, 255), Color.rgb(100, 0, 200), Color.rgb(150, 0, 255)});

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
        hexagon.setFill(colorDark);
        hexagon.setStroke(colorObstacle);
        hexagon.setStrokeWidth(2);
        hexagon.setTranslateX(300);
        hexagon.setTranslateY(200);

        player = new Polygon(0, -6, -3, 4, 3, 4);
        player.setFill(colorObstacle);
        updatePlayerPosition();

        timerText = new Text("TIME 00:00");
        timerText.setFont(Font.font("Verdana", 18));
        timerText.setFill(Color.WHITE);
        timerText.setStroke(Color.BLACK);
        timerText.setStrokeWidth(0.5);
        timerText.setTranslateX(450);
        timerText.setTranslateY(30);

        HighScore highScore = getHighScore.get();
        double highScoreValue = (highScore != null) ? highScore.getScore() : 0;
        int highScoreMinutes = (int) (highScoreValue / 60);
        int highScoreSeconds = (int) (highScoreValue % 60);
        highScoreText = new Text(String.format("BEST %02d:%02d", highScoreMinutes, highScoreSeconds));
        highScoreText.setFont(Font.font("Verdana", 18));
        highScoreText.setFill(Color.WHITE);
        highScoreText.setStroke(Color.BLACK);
        highScoreText.setStrokeWidth(0.5);
        highScoreText.setTranslateX(450);
        highScoreText.setTranslateY(60);

        newRecordText = new Text("NEW RECORD");
        newRecordText.setFont(Font.font("Verdana", 20));
        newRecordText.setFill(Color.WHITE);
        newRecordText.setTranslateX(20);
        newRecordText.setTranslateY(30);
        newRecordText.setVisible(false);

        root.getChildren().addAll(backgroundSections);
        root.getChildren().addAll(hexagon, player, timerText, highScoreText, newRecordText);

        GameMode mode = getGameMode.get();
        if (mode == null) {
            throw new IllegalStateException("GameMode cannot be null when creating GameScene");
        }
        resetSpeeds();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                playerSide = (playerSide - 1 + 6) % 6;
                updatePlayerPosition();
            } else if (event.getCode() == KeyCode.RIGHT) {
                playerSide = (playerSide + 1) % 6;
                updatePlayerPosition();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                double score = (System.nanoTime() - startTime) / 1_000_000_000.0;
                updateHighScore.accept(new HighScore(getGameMode.get(), score));
                gameLoop.stop();
                soundManager.get().stopBackgroundMusic();
                showGameOverScreen(score);
            } else if (event.getCode() == KeyCode.P) {
                if (isPaused) {
                    resumeGame();
                } else {
                    pauseGame();
                }
            }
        });

        root.requestFocus();
    }

    private void createBackground() {
        backgroundSections.clear();
        double maxRadius = 300;

        for (int i = 0; i < 6; i++) {
            double startAngle = Math.toRadians(60 * i);
            double endAngle = Math.toRadians(60 * (i + 1));
            Polygon section = new Polygon();
            section.getPoints().addAll(
                    300.0, 200.0,
                    300 + Math.cos(startAngle) * maxRadius, 200 + Math.sin(startAngle) * maxRadius,
                    300 + Math.cos(endAngle) * maxRadius, 200 + Math.sin(endAngle) * maxRadius
            );
            section.setFill(i % 2 == 0 ? colorLight : colorDark);
            backgroundSections.add(section);
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
            obstacle.setFill(colorObstacle);
            obstacle.setTranslateX(300);
            obstacle.setTranslateY(200);
            obstacle.setUserData(new double[]{baseAngle, distance});
            obstacleTemplates.add(obstacle);
        }
    }

    private void pauseGame() {
        isPaused = true;
        gameLoop.stop();
        soundManager.get().stopBackgroundMusic();

        pausePane = new Pane();
        pausePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        pausePane.setPrefSize(600, 400);

        Text pauseText = new Text("Paused");
        pauseText.setFont(Font.font("Verdana", 40));
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
                rotationSpeed = 1.5; // سرعت چرخش کمتر
                obstacleSpeed = 0.6; // سرعت موانع کمتر
                spawnInterval = 2_812_500_000L; // 2.8125 ثانیه فاصله اسپان
                soundManager.get().setBackgroundMusic("/easy_theme.mp3");
                break;
            case MEDIUM:
                rotationSpeed = 2.0;
                obstacleSpeed = 0.8;
                spawnInterval = 2_812_500_000L; // 2.8125 ثانیه فاصله اسپان
                soundManager.get().setBackgroundMusic("/medium_theme.mp3");
                break;
            case HARD:
                rotationSpeed = 2.5; // سرعت چرخش بیشتر
                obstacleSpeed = 1.0; // سرعت موانع بیشتر
                spawnInterval = 1_687_500_000L; // 1.6875 ثانیه فاصله اسپان
                soundManager.get().setBackgroundMusic("/hard_theme.mp3");
                break;
        }
    }

    private List<Integer> getNextObstaclePattern() {
        List<Integer> selectedSides = new ArrayList<>();

        List<List<Integer>> patterns = new ArrayList<>();
        patterns.add(new ArrayList<>(List.of(0, 1, 2)));         // الگوی رندم 1
        patterns.add(new ArrayList<>(List.of(0, 3, 1)));         // الگوی رندم 2
        patterns.add(new ArrayList<>(List.of(0, 2, 4)));         // الگوی رندم 3
        patterns.add(new ArrayList<>(List.of(0, 1, 2, 3, 4)));  // الگوی رندم 4
        patterns.add(new ArrayList<>(List.of(0, 2, 4, 1, 3)));  // الگوی رندم 5
        patterns.add(new ArrayList<>(List.of(0, 2, 4)));         // الگوی جدید: 3 مانع یه درمیون
        patterns.add(new ArrayList<>(List.of(1, 2, 3, 4, 5)));  // الگوی جدید: دو الگو با 5 مانع (الگوی اول)

        if (patternStep == 0) {
            // وزن‌ها: الگوهای رندم (5 تای اول) وزن 3، الگوهای جدید (2 تای آخر) وزن 1
            int totalWeight = (5 * 3) + (2 * 1); // 15 + 2 = 17
            int randomWeight = random.nextInt(totalWeight); // 0 تا 16

            if (randomWeight < 15) { // 0 تا 14: الگوهای رندم (وزن 15)
                patternIndex = randomWeight / 3; // 0, 1, 2, 3, 4
            } else { // 15 تا 16: الگوهای جدید (وزن 2)
                patternIndex = 5 + (randomWeight - 15); // 5 یا 6
            }
            isSecondPattern = false; // ریست کردن برای الگوهای دوتایی
        }

        List<Integer> currentPattern;
        // اگه الگوی انتخاب‌شده الگوی دوتایی (آخرین الگو) باشه
        if (patternIndex == patterns.size() - 1) {
            if (!isSecondPattern) {
                // الگوی اول: ضلع 0 خالی، موانع روی 1, 2, 3, 4, 5
                currentPattern = patterns.get(patternIndex);
                isSecondPattern = true; // دفعه بعد الگوی دوم رو اجرا می‌کنیم
            } else {
                // الگوی دوم: ضلع 3 خالی (روبروی ضلع 0)، موانع روی 0, 1, 2, 4, 5
                currentPattern = new ArrayList<>(List.of(0, 1, 2, 4, 5));
                isSecondPattern = false; // ریست برای دور بعدی
            }
        } else {
            // برای الگوهای معمولی
            currentPattern = patterns.get(patternIndex);
        }

        // اگه الگو از تعداد موانع تصادفی کمتر باشه، تعداد موانع رو با الگو تنظیم می‌کنیم
        int numObstacles = (patternIndex == patterns.size() - 1) ? 5 : Math.min(random.nextInt(5) + 1, currentPattern.size());
        for (int i = 0; i < numObstacles; i++) {
            int side = currentPattern.get(i % currentPattern.size());
            selectedSides.add(side);
        }
        patternStep = (patternStep + 1) % currentPattern.size();

        return selectedSides;
    }

    private void spawnObstacle() {
        List<Integer> selectedSides = getNextObstaclePattern();

        for (int side : selectedSides) {
            int templateIndex = side;

            Polygon template = obstacleTemplates.get(templateIndex);
            Polygon obstacle = new Polygon();
            obstacle.getPoints().addAll(template.getPoints());
            obstacle.setFill(colorObstacle);
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
        gameOverText.setFont(Font.font("Verdana", 40));
        gameOverText.setFill(Color.RED);
        gameOverText.setTranslateX(230);
        gameOverText.setTranslateY(50);

        int currentMinutes = (int) (currentScore / 60);
        int currentSeconds = (int) (currentScore % 60);
        Text currentScoreText = new Text(String.format("Your Score: %02d:%02d", currentMinutes, currentSeconds));
        currentScoreText.setFont(Font.font("Verdana", 20));
        currentScoreText.setFill(Color.WHITE);
        currentScoreText.setTranslateX(240);
        currentScoreText.setTranslateY(90);

        HighScore highScore = getHighScore.get();
        double highScoreValue = (highScore != null) ? highScore.getScore() : 0;
        int highScoreMinutes = (int) (highScoreValue / 60);
        int highScoreSeconds = (int) (highScoreValue % 60);
        Text highScoreText = new Text(String.format("High Score (%s): %02d:%02d", getGameMode.get().getName(), highScoreMinutes, highScoreSeconds));
        highScoreText.setFont(Font.font("Verdana", 20));
        highScoreText.setFill(Color.WHITE);
        highScoreText.setTranslateX(220);
        highScoreText.setTranslateY(120);

        LinearGradient buttonGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, colorLight),
                new Stop(1, colorDark)
        );
        DropShadow shadow = new DropShadow(10, Color.BLACK);

        VBox leftColumn = new VBox(10);
        leftColumn.setTranslateX(150);
        leftColumn.setTranslateY(150);

        VBox rightColumn = new VBox(10);
        rightColumn.setTranslateX(310);
        rightColumn.setTranslateY(150);

        Button restartButton = createStyledButton("Restart", buttonGradient, shadow);
        restartButton.setOnAction(e -> {
            root.getChildren().remove(gameOverPane);
            resetGame();
            startGame();
        });

        Button changeDifficultyButton = createStyledButton("Change Difficulty", buttonGradient, shadow);
        changeDifficultyButton.setOnAction(e -> {
            root.getChildren().remove(gameOverPane);
            sceneSwitcher.accept("preparation");
        });

        Button mainMenuButton = createStyledButton("Main Menu", buttonGradient, shadow);
        mainMenuButton.setOnAction(e -> {
            root.getChildren().remove(gameOverPane);
            sceneSwitcher.accept("main");
        });

        Button soundToggleButton = createStyledButton(soundManager.get().isSoundEnabled() ? "Sound: ON" : "Sound: OFF", buttonGradient, shadow);
        soundToggleButton.setOnAction(e -> {
            boolean isEnabled = !soundManager.get().isSoundEnabled();
            soundManager.get().setSoundEnabled(isEnabled);
            soundToggleButton.setText(isEnabled ? "Sound: ON" : "Sound: OFF");
            if (isEnabled) {
                soundManager.get().playSpeedIncreaseSound();
            }
        });

        Button highScoreButton = createStyledButton("High Scores", buttonGradient, shadow);
        highScoreButton.setOnAction(e -> showHighScoreBoard());

        Button settingsButton = createStyledButton("Settings", buttonGradient, shadow);
        settingsButton.setOnAction(e -> sceneSwitcher.accept("settings"));

        leftColumn.getChildren().addAll(restartButton, changeDifficultyButton, mainMenuButton, soundToggleButton);
        rightColumn.getChildren().addAll(highScoreButton, settingsButton);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), gameOverPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        root.getChildren().add(gameOverPane);
        gameOverPane.getChildren().addAll(gameOverText, currentScoreText, highScoreText, leftColumn, rightColumn);

        soundManager.get().stopBackgroundMusic();
        soundManager.get().playSpeedIncreaseSound();
    }

    private Button createStyledButton(String text, LinearGradient gradient, DropShadow shadow) {
        Button button = new Button(text);
        button.setFont(Font.font("Verdana", 16));
        button.setPrefSize(140, 30);
        button.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;");
        button.setEffect(shadow);
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: linear-gradient(#ffaa40, #e07b00); -fx-text-fill: white; -fx-background-radius: 10;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: linear-gradient(#ff9329, #c86400); -fx-text-fill: white; -fx-background-radius: 10;"));
        return button;
    }

    private void showHighScoreBoard() {
        if (highScorePane != null) {
            root.getChildren().remove(highScorePane);
        }

        highScorePane = new Pane();
        highScorePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        highScorePane.setPrefSize(600, 400);

        Text title = new Text("High Scores");
        title.setFont(Font.font("Verdana", 40));
        title.setFill(Color.YELLOW);
        title.setTranslateX(220);
        title.setTranslateY(50);

        Text easyScoreText = new Text("Easy: 00:00");
        easyScoreText.setFont(Font.font("Verdana", 20));
        easyScoreText.setFill(Color.WHITE);
        easyScoreText.setTranslateX(250);
        easyScoreText.setTranslateY(100);

        Text mediumScoreText = new Text("Medium: 00:00");
        mediumScoreText.setFont(Font.font("Verdana", 20));
        mediumScoreText.setFill(Color.WHITE);
        mediumScoreText.setTranslateX(250);
        mediumScoreText.setTranslateY(140);

        Text hardScoreText = new Text("Hard: 00:00");
        hardScoreText.setFont(Font.font("Verdana", 20));
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
                if (isPaused) return;

                hexagonRotation += rotationSpeed;
                hexagon.setRotate(hexagonRotation);

                double maxRadius = 300;
                for (int i = 0; i < backgroundSections.size(); i++) {
                    double startAngle = Math.toRadians(60 * i + hexagonRotation);
                    double endAngle = Math.toRadians(60 * (i + 1) + hexagonRotation);
                    Polygon section = backgroundSections.get(i);
                    section.getPoints().setAll(
                            300.0, 200.0,
                            300 + Math.cos(startAngle) * maxRadius, 200 + Math.sin(startAngle) * maxRadius,
                            300 + Math.cos(endAngle) * maxRadius, 200 + Math.sin(endAngle) * maxRadius
                    );
                }

                if (now - lastColorChange > COLOR_CHANGE_INTERVAL) {
                    Color[] newTheme = colorThemes.get(random.nextInt(colorThemes.size()));
                    colorLight = newTheme[0];
                    colorDark = newTheme[1];
                    colorObstacle = newTheme[2];
                    for (int i = 0; i < backgroundSections.size(); i++) {
                        backgroundSections.get(i).setFill(i % 2 == 0 ? colorLight : colorDark);
                    }
                    for (Polygon obstacle : obstacles) {
                        obstacle.setFill(colorObstacle);
                    }
                    hexagon.setStroke(colorObstacle);
                    hexagon.setFill(colorDark);
                    player.setFill(colorObstacle);
                    lastColorChange = now;
                }

                updatePlayerPosition();

                if (now - lastObstacleSpawn > spawnInterval) {
                    spawnObstacle();
                    lastObstacleSpawn = now;
                }

                if (now - lastSpeedIncrease > 10_000_000_000L) {
                    rotationSpeed *= 1.05;
                    obstacleSpeed *= 1.05;
                    // فاصله اسپان ثابت می‌مونه، پس هیچ تغییری اعمال نمی‌کنیم
                    lastSpeedIncrease = now;
                }

                double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
                int minutes = (int) (elapsedSeconds / 60);
                int seconds = (int) (elapsedSeconds % 60);
                timerText.setText(String.format("TIME %02d:%02d", minutes, seconds));

                HighScore highScore = getHighScore.get();
                double highScoreValue = (highScore != null) ? highScore.getScore() : 0;
                int highScoreMinutes = (int) (highScoreValue / 60);
                int highScoreSeconds = (int) (highScoreValue % 60);
                highScoreText.setText(String.format("BEST %02d:%02d", highScoreMinutes, highScoreSeconds));

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
                        soundManager.get().playCollisionSound();
                        double score = (System.nanoTime() - startTime) / 1_000_000_000.0;
                        updateHighScore.accept(new HighScore(getGameMode.get(), score));
                        gameLoop.stop();
                        new Thread(() -> {
                            try {
                                Thread.sleep(200);
                                soundManager.get().stopBackgroundMusic();
                                javafx.application.Platform.runLater(() -> showGameOverScreen(score));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
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