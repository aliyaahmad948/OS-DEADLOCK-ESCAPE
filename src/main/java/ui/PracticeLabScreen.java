package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import logic.LevelFactory;
import logic.ProgressManager;
import model.GameMode;
import model.Level;
import model.Mission;

/**
 * PracticeLabScreen.java
 *
 * A free experimentation sandbox for students:
 * - No timer pressure (very long time limit)
 * - No leaderboard score
 * - Free allocate/release/finish
 * - Create and detect deadlocks to observe safe/unsafe states
 *
 * Reuses the full GameScreen gameplay inside a chosen scenario.
 * No score is saved for practice sessions.
 */
public class PracticeLabScreen {

    private final Stage stage;
    private final ProgressManager progress;

    public PracticeLabScreen(Stage stage, ProgressManager progress) {
        this.stage = stage;
        this.progress = progress;
    }

    public Scene getScene() {
        Label titleLabel = new Label("Practice Lab");
        titleLabel.getStyleClass().add("title-text");

        Label subtitleLabel = new Label(
                "Free experimentation — no timer pressure, no leaderboard score.\n"
                        + "Experiment with resources, create deadlocks, detect cycles, and observe safe/unsafe states."
        );
        subtitleLabel.getStyleClass().add("subtitle-text");
        subtitleLabel.setWrapText(true);

        // Scenario selector: reuse existing level scenarios for experimentation.
        // Buttons use wrapped multi-line text + tooltips so nothing is truncated.
        Button circularScenario = createScenarioButton(
                "Circular Wait Scenario",
                "Experiment with Deadlock — study a busy wait-for graph",
                "Circular Wait scenario. Try to create and detect a deadlock cycle.",
                GameMode.CIRCULAR_WAIT, 2);

        Button combinedScenario = createScenarioButton(
                "Combined Scenario",
                "Test Everything — all four conditions mixed together",
                "Combined scenario mixing all four Coffman conditions.",
                GameMode.DEADLOCK_ESCAPE, 3);

        Button chainScenario = createScenarioButton(
                "Simple Chain",
                "Safe Sequence Training — a clean, beginner-friendly chain",
                "Simple chain scenario. Practice finding the safe finishing order.",
                GameMode.MUTUAL_EXCLUSION, 1);

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setPrefHeight(210);
        infoArea.setPrefWidth(560);
        infoArea.getStyleClass().add("result-text-area");
        infoArea.setText(
                "HOW TO USE THE LAB:\n" +
                        "1. Click a PROCESS node (left), then a RESOURCE node (right) on the graph to select them.\n" +
                        "2. Allocate the resource to the process and observe Mutual Exclusion.\n" +
                        "3. Request held resources to create Hold and Wait situations.\n" +
                        "4. Try to finish processes — note that resources cannot be stolen (No Preemption).\n" +
                        "5. Create circular waits and press 'Detect Deadlock' to see the cycle highlighted.\n" +
                        "6. Find the safe sequence to finish all processes.\n\n" +
                        "No score is saved in Practice Lab mode."
        );

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("danger-button");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> {
            MenuScreen menuScreen = new MenuScreen(stage, progress);
            stage.setScene(menuScreen.getScene());
        });

        VBox layout = new VBox(15, titleLabel, subtitleLabel,
                circularScenario, combinedScenario, chainScenario, infoArea, backButton);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Scene scene = new Scene(layout, 1200, 800);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    private Button createScenarioButton(String title, String subtitle, String tooltipText,
                                        GameMode mode, int levelNumber) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("practice-button-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("practice-button-desc");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(400);

        VBox textBox = new VBox(2, titleLabel, subtitleLabel);
        textBox.setAlignment(Pos.CENTER);

        Button button = new Button();
        button.setGraphic(textBox);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.getStyleClass().add("success-button");
        button.setPrefWidth(420);
        button.setPrefHeight(54);
        Tooltip.install(button, new Tooltip(tooltipText));
        button.setOnAction(e -> launchPractice(mode, levelNumber));
        return button;
    }

    private void launchPractice(GameMode mode, int levelNumber) {
        Level level = LevelFactory.createLevel(mode, levelNumber);

        // Give the lab a very long timer (no real pressure)
        level.setTimeLimitSeconds(900);

        Mission labMission = new Mission("Practice Lab: experiment freely — no pressure, no score. "
                + "Try to find the safe sequence and observe deadlock behavior.");
        level.setMission(labMission);
        level.setHintCount(5);

        // Launch the standard GameScreen. Practice results won't be saved to DB
        // because GameScreen only builds progress/DB entries when using the
        // standard flow; exiting mid-scenario returns to this lab list.
        GameScreen gameScreen = new GameScreen(stage, progress, level, mode, () -> {
            PracticeLabScreen lab = new PracticeLabScreen(stage, progress);
            stage.setScene(lab.getScene());
        });
        stage.setScene(gameScreen.getScene());
    }
}