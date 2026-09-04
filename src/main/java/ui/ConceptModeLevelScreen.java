package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import logic.LevelFactory;
import logic.ProgressManager;
import model.GameMode;
import model.Level;

import java.util.List;

/**
 * ConceptModeLevelScreen.java
 *
 * Shows the 5 levels for a selected game mode.
 * Each level card displays: level number, name, process count, difficulty,
 * mission, and locked/unlocked status.
 */
public class ConceptModeLevelScreen {

    private final Stage stage;
    private final ProgressManager progress;
    private final GameMode mode;

    public ConceptModeLevelScreen(Stage stage, ProgressManager progress, GameMode mode) {
        this.stage = stage;
        this.progress = progress;
        this.mode = mode;
    }

    public Scene getScene() {
        Label titleLabel = new Label(mode.getDisplayName());
        titleLabel.getStyleClass().add("title-text");

        Label subtitleLabel = new Label(mode.getFullDescription());
        subtitleLabel.getStyleClass().add("subtitle-text");
        subtitleLabel.setWrapText(true);

        List<Level> levels = LevelFactory.getLevelsForMode(mode);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        int col = 0;
        int row = 0;
        int cardsPerRow = 3;
        for (Level level : levels) {
            grid.add(createLevelCard(level), col, row);
            col++;
            if (col >= cardsPerRow) {
                col = 0;
                row++;
            }
        }

        Button backButton = new Button("Back to Modes");
        backButton.getStyleClass().add("danger-button");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> {
            ModeSelectionScreen modeScreen = new ModeSelectionScreen(stage, progress);
            stage.setScene(modeScreen.getScene());
        });

        Label modeProgress = new Label("Mode Progress: " + countCompleted(levels) + " / 5 levels");
        modeProgress.getStyleClass().add("score-label");

        VBox layout = new VBox(20, titleLabel, subtitleLabel, grid, modeProgress, backButton);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Scene scene = new Scene(layout, 1200, 800);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    private int countCompleted(List<Level> levels) {
        int count = 0;
        for (Level level : levels) {
            if (progress.isLevelCompleted(mode, level.getLevelNumber())) count++;
        }
        return count;
    }

    private VBox createLevelCard(Level level) {
        int levelNum = level.getLevelNumber();
        boolean unlocked = progress.isLevelUnlocked(mode, levelNum);
        int stars = progress.getStarsFor(mode, levelNum);

        Label levelTitle = new Label("LEVEL " + levelNum);
        levelTitle.getStyleClass().add("level-card-title");

        Label nameLabel = new Label(level.getLevelName());
        nameLabel.getStyleClass().add("mode-card-desc");

        Label details = new Label(
                level.getProcessNames().size() + " Processes\n"
                        + "Difficulty: " + level.getDifficulty()
        );
        details.getStyleClass().add("info-label");
        details.setWrapText(true);

        Label starsLabel = new Label(starsToText(stars));
        starsLabel.getStyleClass().add("stars-label");

        VBox card = new VBox(8, levelTitle, nameLabel, details, starsLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("mode-card");
        card.setPrefWidth(260);
        card.setPrefHeight(150);

        Button playButton = new Button(unlocked ? "Play" : "\uD83D\uDD12 Locked");
        playButton.getStyleClass().add(unlocked ? "game-button" : "danger-button");
        playButton.setDisable(!unlocked);
        playButton.setOnAction(e -> {
            GameScreen gameScreen = new GameScreen(stage, progress, level, mode);
            stage.setScene(gameScreen.getScene());
        });

        card.getChildren().add(playButton);
        return card;
    }

    private String starsToText(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < stars ? "\u2605" : "\u2606");
        }
        return sb.toString();
    }
}