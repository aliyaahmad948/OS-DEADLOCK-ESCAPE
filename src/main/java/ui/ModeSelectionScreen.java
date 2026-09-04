package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import logic.ProgressManager;
import model.GameMode;

/**
 * ModeSelectionScreen.java
 *
 * Screen where the player chooses which OS concept mode to practice.
 * Shows all 5 modes with description, concept taught, and level count.
 * Uses the Navy + Neon cyber theme.
 */
public class ModeSelectionScreen {

    private final Stage stage;
    private final ProgressManager progress;

    public ModeSelectionScreen(Stage stage, ProgressManager progress) {
        this.stage = stage;
        this.progress = progress;
    }

    public Scene getScene() {
        Label titleLabel = new Label("Select a Learning Mode");
        titleLabel.getStyleClass().add("title-text");

        Label subtitleLabel = new Label("Choose which Operating System concept to practice");
        subtitleLabel.getStyleClass().add("subtitle-text");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        int col = 0;
        int row = 0;
        int cardsPerRow = 3;
        for (GameMode mode : GameMode.values()) {
            grid.add(createModeCard(mode), col, row);
            col++;
            if (col >= cardsPerRow) {
                col = 0;
                row++;
            }
        }

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("danger-button");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> {
            MenuScreen menuScreen = new MenuScreen(stage, progress);
            stage.setScene(menuScreen.getScene());
        });

        // XP / progress summary
        Label progressLabel = new Label("XP: " + progress.getTotalXp()
                + "   |   Levels Completed: " + progress.levelsCompleted() + " / 25");
        progressLabel.getStyleClass().add("score-label");

        VBox layout = new VBox(20, titleLabel, subtitleLabel, grid, progressLabel, backButton);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Scene scene = new Scene(layout, 1200, 800);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    /**
     * Builds a clickable card for a single game mode.
     */
    private StackPane createModeCard(GameMode mode) {
        Label modeName = new Label(mode.getDisplayName());
        modeName.getStyleClass().add("mode-card-title");

        Label conceptLabel = new Label(mode.getShortDescription());
        conceptLabel.getStyleClass().add("mode-card-desc");
        conceptLabel.setWrapText(true);

        Label levelsLabel = new Label(mode.getLevelCount() + " Levels");
        levelsLabel.getStyleClass().add("mode-card-levels");

        Label descLabel = new Label(mode.getFullDescription());
        descLabel.getStyleClass().add("mode-card-full");
        descLabel.setWrapText(true);

        boolean mastered = progress.conceptMastered(mode);
        Label masteryLabel = new Label(mastered ? "\u2714 MASTERED" : "");
        masteryLabel.getStyleClass().add(mastered ? "mastery-badge" : "mode-card-levels");

        VBox cardContent = new VBox(8, modeName, conceptLabel, levelsLabel, masteryLabel, descLabel);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(18));
        cardContent.getStyleClass().add("mode-card");

        StackPane card = new StackPane(cardContent);
        card.getStyleClass().add(mastered ? "mode-card-mastered" : "mode-card");

        card.setOnMouseEntered(e -> {
            card.setScaleX(1.04);
            card.setScaleY(1.04);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        card.setOnMouseClicked(e -> {
            ConceptModeLevelScreen levelScreen = new ConceptModeLevelScreen(stage, progress, mode);
            stage.setScene(levelScreen.getScene());
        });

        card.setPrefWidth(260);
        card.setPrefHeight(200);
        return card;
    }
}