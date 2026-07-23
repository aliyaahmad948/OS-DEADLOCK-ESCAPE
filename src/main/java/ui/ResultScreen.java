package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import db.ScoreDatabase;
import logic.GameManager;
import model.Level;
import model.PlayerSession;

/**
 * ResultScreen.java
 *
 * Shown automatically when a level ends, either in WIN or LOSE state.
 * - WIN: "Mission Complete – Safe Sequence Found"
 * - LOSE (deadlock): "Deadlock Detected" + explanation trace
 * - LOSE (timeout): "Time's Up" message
 *
 * Styled using style.css classes to match the Navy+Neon theme.
 *
 * OS Concept Mapping:
 * This screen reports the final outcome of the resource allocation
 * simulation the player just ran.
 */
public class ResultScreen {

    private Stage stage;
    private Level level;
    private GameManager gameManager;

    public ResultScreen(Stage stage, Level level, GameManager gameManager) {
        this.stage = stage;
        this.level = level;
        this.gameManager = gameManager;

        ScoreDatabase db = new ScoreDatabase();
        String playerName = PlayerSession.getInstance().getPlayerName();
        db.saveScore(playerName, level.getLevelNumber(), gameManager.getScore());
    }

    /**
     * Builds and returns the Scene for the result screen.
     * Content changes depending on whether the player won or lost.
     */
    public Scene getScene() {

        GameManager.GameState state = gameManager.getState();
        String playerName = PlayerSession.getInstance().getPlayerName();

        Label resultTitle = new Label();

        Label playerLabel = new Label("Player: " + playerName);
        playerLabel.getStyleClass().add("info-label");

        Label scoreLabel = new Label("Final Score: " + gameManager.getScore());
        scoreLabel.getStyleClass().add("score-label");

        TextArea explanationArea = new TextArea();
        explanationArea.setEditable(false);
        explanationArea.setWrapText(true);
        explanationArea.setPrefHeight(180);
        explanationArea.setPrefWidth(500);
        explanationArea.getStyleClass().add("result-text-area");

        // Set title text and style class based on outcome
        if (state == GameManager.GameState.WON) {
            resultTitle.setText("Mission Complete - Safe Sequence Found!");
            resultTitle.getStyleClass().add("win-title");
            explanationArea.setText("All processes escaped safely without any deadlock.\n\n"
                    + "Final message: " + gameManager.getLastMessage());

        } else if (state == GameManager.GameState.LOST_DEADLOCK) {
            resultTitle.setText("Deadlock Detected");
            resultTitle.getStyleClass().add("lose-title");
            explanationArea.setText(gameManager.getLastMessage());

        } else if (state == GameManager.GameState.LOST_TIMEOUT) {
            resultTitle.setText("Time's Up!");
            resultTitle.getStyleClass().add("lose-title");
            explanationArea.setText("You ran out of time before finding a safe sequence.\n\n"
                    + "Final message: " + gameManager.getLastMessage());

        } else {
            // Fallback (should not normally happen)
            resultTitle.setText("Game Over");
            resultTitle.getStyleClass().add("lose-title");
            explanationArea.setText(gameManager.getLastMessage());
        }

        // ---------- Buttons ----------
        Button retryButton = new Button("Retry Level");
        retryButton.getStyleClass().add("game-button");
        retryButton.setOnAction(e -> {
            GameScreen gameScreen = new GameScreen(stage, level);
            stage.setScene(gameScreen.getScene());
        });

        Button levelSelectButton = new Button("Choose Another Level");
        levelSelectButton.getStyleClass().add("game-button");
        levelSelectButton.setOnAction(e -> {
            LevelSelectionScreen levelSelectionScreen = new LevelSelectionScreen(stage);
            stage.setScene(levelSelectionScreen.getScene());
        });

        Button menuButton = new Button("Main Menu");
        menuButton.getStyleClass().add("danger-button");
        menuButton.setOnAction(e -> {
            MenuScreen menuScreen = new MenuScreen(stage);
            stage.setScene(menuScreen.getScene());
        });

        VBox buttonBox = new VBox(12, retryButton, levelSelectButton, menuButton);
        buttonBox.setAlignment(Pos.CENTER);

        // ---------- Layout ----------
        VBox layout = new VBox(20, resultTitle, playerLabel, scoreLabel, explanationArea, buttonBox);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }
}