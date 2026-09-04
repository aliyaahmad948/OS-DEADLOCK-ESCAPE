package ui;

import db.ScoreDatabase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import logic.GameManager;
import logic.AchievementTracker;
import logic.LevelFactory;
import logic.ProgressManager;
import model.GameMode;
import model.Level;
import model.LevelResult;
import model.PlayerSession;

import java.util.List;

/**
 * ResultScreen.java
 *
 * Shown automatically when a level ends, either in WIN or LOSE state.
 * Displays:
 * - Level completed/failed
 * - Stars earned (on win)
 * - Score, time, mistakes, efficiency
 * - Educational stats (deadlocks detected, prevented, recovery actions, hints)
 * - Concept learned + short OS explanation
 *
 * BUTTONS:
 * - Next Level (when a next level is unlocked)
 * - Retry Level
 * - Back to Mode
 * - Main Menu
 */
public class ResultScreen {

    private final Stage stage;
    private final ProgressManager progress;
    private final Level level;
    private final GameManager gameManager;
    private final LevelResult result;
    private final GameMode mode;
    private final boolean dbSaveFailed;
    private final String dbError;

    // Backward-compatible constructor (legacy flow: 3 classic levels)
    public ResultScreen(Stage stage, Level level, GameManager gameManager) {
        this(stage, new ProgressManager(), level, gameManager, buildLegacyResult(gameManager, level), level.getGameMode());
    }

    public ResultScreen(Stage stage, ProgressManager progress, Level level,
                        GameManager gameManager, LevelResult result, GameMode mode) {
        this.stage = stage;
        this.progress = progress;
        this.level = level;
        this.gameManager = gameManager;
        this.result = result;
        this.mode = mode;

        // Persist score to the legacy scores table (backward compatible)
        boolean dbOk = true;
        String dbError = null;
        try {
            ScoreDatabase db = new ScoreDatabase();
            String playerName = PlayerSession.getInstance().getPlayerName();
            db.saveScore(playerName, level.getLevelNumber(), gameManager.getScore());

            // Phase 16: persist mode-based score + level progress
            db.saveModeScore(playerName, mode.name(), gameManager.getScore());
            db.saveLevelProgress(playerName, mode.name(), level.getLevelNumber(),
                    result.getStars(), gameManager.getScore());
            dbOk = db.isAvailable();
            dbError = db.getLastError();
        } catch (Exception e) {
            dbOk = false;
            dbError = e.getMessage();
            System.out.println("Score DB save failed: " + e.getMessage());
        }
        this.dbSaveFailed = !dbOk;
        this.dbError = dbError;
    }

    private static LevelResult buildLegacyResult(GameManager gameManager, Level level) {
        LevelResult r = new LevelResult();
        r.setWon(gameManager.getState() == GameManager.GameState.WON);
        r.setScore(gameManager.getScore());
        r.setLevelName(level.getLevelName());
        r.setLevelNumber(level.getLevelNumber());
        r.setStars(r.isWon() ? 1 : 0);
        r.setTotalProcesses(gameManager.getProcesses().size());
        return r;
    }

    public Scene getScene() {
        boolean won = result.isWon();

        Label resultTitle = new Label();
        if (won) {
            resultTitle.setText("LEVEL COMPLETE!");
            resultTitle.getStyleClass().add("win-title");
        } else if (gameManager.getState() == GameManager.GameState.LOST_DEADLOCK) {
            resultTitle.setText("DEADLOCK DETECTED");
            resultTitle.getStyleClass().add("lose-title");
        } else {
            resultTitle.setText("SYSTEM FAILURE");
            resultTitle.getStyleClass().add("lose-title");
        }

        // Stars
        Label starsLabel = new Label(starsToText(result.getStars()));
        starsLabel.getStyleClass().add("stars-big");

        Label playerLabel = new Label("Player: " + PlayerSession.getInstance().getPlayerName());
        playerLabel.getStyleClass().add("info-label");

        Label scoreLabel = new Label("Score: " + result.getScore());
        scoreLabel.getStyleClass().add("score-label");

        // Stats breakdown
        Label statsLabel = new Label(
                formatTime(result.getTimeUsedSeconds()) + "  |  Efficiency: "
                        + Math.round(result.getEfficiencyPercent()) + "%  |  Mistakes: "
                        + result.getMistakes() + "  |  Processes: "
                        + result.getProcessesCompleted() + "/" + result.getTotalProcesses());
        statsLabel.getStyleClass().add("info-label");

        Label eduStatsLabel = new Label(
                "Deadlocks Detected: " + result.getDeadlocksDetected()
                        + "  |  Deadlocks Prevented: " + result.getDeadlocksPrevented()
                        + "  |  Recovery Actions: " + result.getRecoveryActions()
                        + "  |  Hints Used: " + result.getHintsUsed());
        eduStatsLabel.getStyleClass().add("info-label");

        // Concept learned + why it matters
        Label conceptLabel = new Label("CONCEPT MASTERED: " + result.getConceptLearned());
        conceptLabel.getStyleClass().add("concept-badge");

        // Achievements: record lifetime stats + check for unlocks
        String newAchievement = AchievementTracker.recordLevelOutcome(
                result.isWon(),
                result.getDeadlocksDetected(),
                result.getDeadlocksPrevented(),
                result.getRecoveryActions(),
                result.isUsedSafeSequence(),
                result.getTimeUsedSeconds(),
                level.getTimeLimitSeconds(),
                result.getStars());

        Label achievementLabel = new Label();
        if (newAchievement != null) {
            achievementLabel.setText("\uD83C\uDFC6 ACHIEVEMENT UNLOCKED: " + newAchievement.toUpperCase());
            achievementLabel.getStyleClass().add("achievement-unlocked");
        } else {
            achievementLabel.setText("Achievements: " + AchievementTracker.getUnlockedAchievements().size()
                    + " / " + AchievementTracker.allDefinitions().size() + " unlocked");
            achievementLabel.getStyleClass().add("info-label");
        }

        // Explanation area: deadlock losses show a readable cycle diagram,
        // other endings show the plain explanation text.
        Node explanationNode;
        if (!won && gameManager.getState() == GameManager.GameState.LOST_DEADLOCK) {
            explanationNode = buildCyclePanel();
        } else {
            TextArea explanationArea = new TextArea();
            explanationArea.setEditable(false);
            explanationArea.setWrapText(true);
            explanationArea.setPrefHeight(110);
            explanationArea.setPrefWidth(600);
            explanationArea.getStyleClass().add("result-text-area");
            explanationArea.setText(buildExplanation(won));
            explanationNode = explanationArea;
        }

        // ---------- Buttons ----------
        Button nextLevelButton = null;
        int nextLevelNum = level.getLevelNumber() + 1;
        if (won && nextLevelNum <= mode.getLevelCount()) {
            LevelResult actual = (result.isWon()) ? result : null;
            if (actual != null || progress.isLevelUnlocked(mode, nextLevelNum)) {
                nextLevelButton = new Button("Next Level");
                nextLevelButton.getStyleClass().add("success-button");
                Level nextLevel = LevelFactory.createLevel(mode, nextLevelNum);
                nextLevelButton.setOnAction(e -> {
                    GameScreen gameScreen = new GameScreen(stage, progress, nextLevel, mode);
                    stage.setScene(gameScreen.getScene());
                });
            }
        }

        Button retryButton = new Button("Retry Level");
        retryButton.getStyleClass().add("game-button");
        retryButton.setOnAction(e -> {
            GameScreen gameScreen = new GameScreen(stage, progress, level, mode);
            stage.setScene(gameScreen.getScene());
        });

        Button modeButton = new Button("Back to Mode");
        modeButton.getStyleClass().add("game-button");
        modeButton.setOnAction(e -> {
            ConceptModeLevelScreen levelScreen = new ConceptModeLevelScreen(stage, progress, mode);
            stage.setScene(levelScreen.getScene());
        });

        Button menuButton = new Button("Main Menu");
        menuButton.getStyleClass().add("danger-button");
        menuButton.setOnAction(e -> {
            MenuScreen menuScreen = new MenuScreen(stage, progress);
            stage.setScene(menuScreen.getScene());
        });

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        if (nextLevelButton != null) {
            buttonBox.getChildren().add(nextLevelButton);
        }
        buttonBox.getChildren().addAll(retryButton, modeButton, menuButton);

        // ---------- Layout ----------
        VBox layout = new VBox(12, resultTitle, starsLabel, playerLabel, scoreLabel,
                statsLabel, eduStatsLabel, achievementLabel, conceptLabel, explanationNode, buttonBox);
        if (dbSaveFailed) {
            Label dbWarning = new Label("\u26A0 Score was NOT saved \u2014 leaderboard data is missing. "
                    + "MySQL connection failed ("
                    + (dbError == null ? "could not connect to localhost:3306" : dbError)
                    + "). Start the MySQL service and replay the level to record your score.");
            dbWarning.getStyleClass().add("error-label");
            dbWarning.setWrapText(true);
            dbWarning.setMaxWidth(480);
            layout.getChildren().add(9, dbWarning);
        }
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(layout, 1200, 800);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    private String buildExplanation(boolean won) {
        if (won) {
            switch (mode) {
                case MUTUAL_EXCLUSION:
                    return "WHY IT MATTERS:\nA resource can be held by only one process at a time. "
                            + "This exclusivity is a necessary condition for deadlock.";
                case HOLD_AND_WAIT:
                    return "WHY IT MATTERS:\nA process may hold resources while waiting for others, "
                            + "which can contribute to deadlock formation.";
                case NO_PREEMPTION:
                    return "WHY IT MATTERS:\nResources cannot be forcibly taken away while in use. "
                            + "Only the holding process can release them.";
                case CIRCULAR_WAIT:
                    return "WHY IT MATTERS:\nA circular chain of waits between processes "
                            + "is the final condition that creates deadlock.";
                case DEADLOCK_ESCAPE:
                default:
                    return "WHY IT MATTERS:\nYou used all four concepts — Mutual Exclusion, Hold and Wait, "
                            + "No Preemption, and Circular Wait — to manage resources like a real OS.";
            }
        }
        if (gameManager.getState() == GameManager.GameState.LOST_DEADLOCK) {
            return "Reason:\n" + gameManager.getLastMessage()
                    + "\n\nTip: Release resources in the correct order to break the circular wait.";
        }
        return "You ran out of time. Restart and try to find the safe sequence faster.";
    }

    /**
     * Builds a readable "wait-for cycle" panel instead of the raw console log.
     * Shows the cycle as a compact arrow chain (P2 → R3 → P3 → R2 → P2) plus
     * one short meaning line per edge. Underlying detection data is unchanged.
     */
    private VBox buildCyclePanel() {
        List<String> cycle = gameManager.getDetector().getLastCycleNodes();
        if (cycle == null || cycle.isEmpty()) {
            TextArea fallback = new TextArea();
            fallback.setEditable(false);
            fallback.setWrapText(true);
            fallback.setPrefHeight(110);
            fallback.setPrefWidth(600);
            fallback.getStyleClass().add("result-text-area");
            fallback.setText(gameManager.getLastMessage());
            VBox box = new VBox(fallback);
            box.setAlignment(Pos.CENTER);
            return box;
        }

        String headingText = "\uD83D\uDD04 DEADLOCK \u2014 Wait-For Cycle Detected";
        Label heading = new Label(headingText);
        heading.getStyleClass().add("cycle-heading");

        // Show the cycle as a readable arrow chain (drop the repeated closing node)
        List<String> display = new java.util.ArrayList<>(cycle);
        if (display.size() > 1 && display.get(0).equals(display.get(display.size() - 1))) {
            display.remove(display.size() - 1);
        }
        Label sequence = new Label(String.join("  \u2192  ", display));
        sequence.setWrapText(true);
        sequence.getStyleClass().add("cycle-sequence");

        VBox meaningBox = new VBox(4);
        meaningBox.setAlignment(Pos.CENTER_LEFT);
        Label meaningHeader = new Label("What is happening:");
        meaningHeader.getStyleClass().add("instr-card-title");
        meaningBox.getChildren().add(meaningHeader);
        for (int i = 0; i < cycle.size() - 1; i++) {
            String from = cycle.get(i);
            String to = cycle.get(i + 1);
            String line = from.startsWith("P")
                    ? from + "  waits for  " + to
                    : to + "  holds  " + from;
            Label row = new Label("\u25B8  " + line);
            row.getStyleClass().add("cycle-meaning");
            meaningBox.getChildren().add(row);
        }

        Label tip = new Label("Tip: Release resources in the correct order to break the circular wait.");
        tip.setWrapText(true);
        tip.getStyleClass().add("cycle-tip");

        VBox panel = new VBox(10, heading, sequence, meaningBox, tip);
        panel.getStyleClass().add("panel-box");
        panel.setMaxWidth(620);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(14));
        return panel;
    }

    private String starsToText(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < stars ? "\u2605" : "\u2606");
        }
        return sb.toString();
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("Time: %02d:%02d", mins, secs);
    }
}