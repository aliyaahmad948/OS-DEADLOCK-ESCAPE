package ui;

import db.ScoreDatabase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import logic.ProgressManager;
import model.GameMode;

import java.util.List;

/**
 * LeaderboardScreen.java
 *
 * Displays persistent leaderboards from MySQL:
 * - Per-mode leaderboards (Mutual Exclusion, Hold & Wait, No Preemption,
 *   Circular Wait, Deadlock Escape)
 * - Overall highest score
 *
 * Uses the enhanced scores_mode table (Phase 16). Falls back gracefully
 * if the database is unavailable.
 */
public class LeaderboardScreen {

    private final Stage stage;
    private final ProgressManager progress;
    private final ScoreDatabase db;

    public LeaderboardScreen(Stage stage) {
        this.stage = stage;
        this.progress = new ProgressManager();
        this.db = new ScoreDatabase();
    }

    public LeaderboardScreen(Stage stage, ProgressManager progress) {
        this.stage = stage;
        this.progress = progress;
        this.db = new ScoreDatabase();
    }

    public Scene getScene() {
        Label titleLabel = new Label("Leaderboard");
        titleLabel.getStyleClass().add("title-text");

        // Database connection status — so MySQL problems are never silent
        Label dbStatus = new Label();
        dbStatus.setWrapText(true);
        dbStatus.setMaxWidth(900);
        dbStatus.setAlignment(Pos.CENTER);
        if (db.isAvailable()) {
            dbStatus.setText("\u2705 MySQL connected \u2014 scores are being saved.");
            dbStatus.getStyleClass().add("info-label");
        } else {
            dbStatus.setText("\u26A0 MySQL OFFLINE \u2014 scores are NOT being saved. Reason: "
                    + (db.getLastError() == null ? "could not connect to localhost:3306" : db.getLastError())
                    + ". Make sure the MySQL service is running (check Workbench / services.msc) then try again.");
            dbStatus.getStyleClass().add("error-label");
        }

        VBox levelsContainer = new VBox(20);
        levelsContainer.setAlignment(Pos.CENTER);

        // Per-mode leaderboards
        for (GameMode mode : GameMode.values()) {
            levelsContainer.getChildren().add(createModeLeaderboard(mode.getDisplayName(), mode.name()));
        }

        // Overall leaderboard (legacy scores table, descending score)
        levelsContainer.getChildren().add(createOverallLeaderboard());

        ScrollPane scrollPane = new ScrollPane(levelsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("transparent-scroll");
        scrollPane.setPrefHeight(620);

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("danger-button");
        backButton.setOnAction(e -> {
            MenuScreen menuScreen = new MenuScreen(stage, progress);
            stage.setScene(menuScreen.getScene());
        });

        VBox layout = new VBox(15, titleLabel, dbStatus, scrollPane, backButton);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        Scene scene = new Scene(layout, 1200, 800);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    private VBox createModeLeaderboard(String displayName, String modeName) {
        Label levelTitle = new Label(displayName);
        levelTitle.getStyleClass().add("level-label");

        List<String[]> scores = db.getModeLeaderboard(modeName);

        VBox scoreRows = new VBox(6);
        scoreRows.setAlignment(Pos.CENTER);

        if (scores.isEmpty()) {
            Label noScores = new Label("No scores recorded yet.");
            noScores.getStyleClass().add("info-label");
            scoreRows.getChildren().add(noScores);
        } else {
            int rank = 1;
            for (String[] row : scores) {
                String playerName = row[0];
                int score = Integer.parseInt(row[1]);
                String timestamp = row[2];

                Label rowLabel = new Label(rank + ".  " + playerName + "  -  Score: " + score + "  (" + timestamp + ")");
                rowLabel.getStyleClass().add("info-card");
                rowLabel.setWrapText(true);
                rowLabel.setMaxWidth(520);
                rank++;

                scoreRows.getChildren().add(rowLabel);
            }
        }

        VBox levelBox = new VBox(8, levelTitle, scoreRows);
        levelBox.getStyleClass().add("panel-box");
        levelBox.setAlignment(Pos.CENTER);
        levelBox.setPadding(new Insets(15));
        levelBox.setMaxWidth(600);

        return levelBox;
    }

    private VBox createOverallLeaderboard() {
        Label levelTitle = new Label("Overall Top Scores");
        levelTitle.getStyleClass().add("level-label");

        List<String> all = new java.util.ArrayList<>();
        // aggregate: pull players across all levels
        for (int i = 1; i <= 3; i++) {
            for (String[] row : db.getLeaderboard(i)) {
                all.add(row[0] + "\u0001" + row[1]);
            }
        }
        // Sort by score descending
        all.sort((a, b) -> Integer.parseInt(b.split("\u0001")[1])
                - Integer.parseInt(a.split("\u0001")[1]));

        // Dedupe case-insensitively: one best row per player
        java.util.LinkedHashMap<String, String> best = new java.util.LinkedHashMap<>();
        for (String entry : all) {
            String[] parts = entry.split("\u0001");
            String key = parts[0].trim().toLowerCase();
            if (!best.containsKey(key)) {
                best.put(key, entry);
            }
        }
        List<String[]> rows = new java.util.ArrayList<>();
        for (String entry : best.values()) {
            String[] parts = entry.split("\u0001");
            rows.add(new String[]{parts[0], parts[1]});
        }
        if (rows.size() > 15) rows = rows.subList(0, 15);

        VBox scoreRows = new VBox(6);
        scoreRows.setAlignment(Pos.CENTER);

        if (rows.isEmpty()) {
            Label noScores = new Label("No scores recorded yet.");
            noScores.getStyleClass().add("info-label");
            scoreRows.getChildren().add(noScores);
        } else {
            int rank = 1;
            for (String[] row : rows) {
                Label rowLabel = new Label(rank + ".  " + row[0] + "  -  Score: " + row[1]);
                rowLabel.getStyleClass().add("info-card");
                rowLabel.setWrapText(true);
                rowLabel.setMaxWidth(520);
                rank++;
                scoreRows.getChildren().add(rowLabel);
            }
        }

        VBox levelBox = new VBox(8, levelTitle, scoreRows);
        levelBox.getStyleClass().add("panel-box");
        levelBox.setAlignment(Pos.CENTER);
        levelBox.setPadding(new Insets(15));
        levelBox.setMaxWidth(600);

        return levelBox;
    }
}