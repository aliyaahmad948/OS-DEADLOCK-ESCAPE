package ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import logic.GameManager;
import model.Level;
import model.Process;
import model.Resource;
import ui.graph.GraphCanvas;

import java.util.List;

/**
 * GameScreen.java
 * <p>
 * Premium "node-graph" gameplay screen.
 * Layout:
 * Top    -> glowing "DEADLOCK SIMULATOR" title + level name
 * Left   -> Process list panel
 * Center -> GraphCanvas (animated node-graph visualization)
 * Right  -> Resource list panel
 * Bottom -> Allocate / Finish (Release) / Detect Deadlock / Restart buttons
 * + Status panel, Move Counter, Allocation Counter, Timer, Score
 * <p>
 * IMPORTANT: This class does NOT change any game logic. It only reads
 * GameManager's state (via its existing public getters) and visualizes it.
 * All existing behavior (allocation rules, win/lose conditions, scoring,
 * timer) is untouched — this is purely a presentation-layer rewrite.
 */
public class GameScreen {

    private Stage stage;
    private GameManager gameManager;
    private Level level;

    private VBox processListBox;
    private VBox resourceListBox;
    private GraphCanvas graphCanvas;

    private Label scoreLabel;
    private Label timerLabel;
    private Label statusLabel;
    private Label moveCounterLabel;
    private Label allocationCounterLabel;

    private ComboBox<String> processDropdown;
    private ComboBox<String> resourceDropdown;

    private StackPane resultCardLayer; // holds the floating warning/safe card
    private VBox sceneContentRoot;     // everything except the result card (used for dimming)

    private Timeline gameTimer;

    // UI-only counters (do not affect game logic/scoring)
    private int moveCount = 0;
    private int allocationCount = 0;

    public GameScreen(Stage stage, Level level) {
        this.stage = stage;
        this.level = level;
        this.gameManager = new GameManager();
        this.gameManager.loadLevel(level);
    }

    public Scene getScene() {

        // ---------- Glowing top title ----------
        Label mainTitle = new Label("DEADLOCK ESCAPE GAME");
        mainTitle.getStyleClass().add("title-text");
        mainTitle.setStyle(mainTitle.getStyle() + "-fx-font-size: 26px;");

        Label levelLabel = new Label(level.getLevelName());
        levelLabel.getStyleClass().add("level-label");

        VBox topBar = new VBox(4, mainTitle, levelLabel);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));

        // ---------- Left panel: Processes ----------
        processListBox = new VBox(8);
        ScrollPane processScroll = new ScrollPane(processListBox);
        processScroll.setFitToWidth(true);
        processScroll.setPrefHeight(420);
        processScroll.getStyleClass().add("transparent-scroll");

        Label processHeader = new Label("Processes");
        processHeader.getStyleClass().add("info-label");

        VBox processPanel = new VBox(8, processHeader, processScroll);
        processPanel.getStyleClass().add("panel-box");
        processPanel.setPrefWidth(230);

        // ---------- Right panel: Resources ----------
        resourceListBox = new VBox(8);
        ScrollPane resourceScroll = new ScrollPane(resourceListBox);
        resourceScroll.setFitToWidth(true);
        resourceScroll.setPrefHeight(420);
        resourceScroll.getStyleClass().add("transparent-scroll");

        Label resourceHeader = new Label("Resources");
        resourceHeader.getStyleClass().add("info-label");

        VBox resourcePanel = new VBox(8, resourceHeader, resourceScroll);
        resourcePanel.getStyleClass().add("panel-box");
        resourcePanel.setPrefWidth(230);

        // ---------- Center: Graph Visualization ----------
        graphCanvas = new GraphCanvas();
        ScrollPane graphScroll = new ScrollPane(graphCanvas);
        graphScroll.setFitToWidth(true);
        graphScroll.setPrefHeight(420);
        graphScroll.getStyleClass().add("transparent-scroll");

        VBox centerPanel = new VBox(graphScroll);
        centerPanel.getStyleClass().add("panel-box");
        centerPanel.setPrefWidth(420);

        // Let the center panel grow to fill available horizontal space
        HBox.setHgrow(centerPanel, Priority.ALWAYS);

        HBox middleRow = new HBox(15, processPanel, centerPanel, resourcePanel);
        middleRow.setAlignment(Pos.CENTER);
        middleRow.setPadding(new Insets(10));

        // ---------- Controls: process/resource selection + action buttons ----------
        processDropdown = new ComboBox<>();
        resourceDropdown = new ComboBox<>();
        processDropdown.getStyleClass().add("combo-box");
        resourceDropdown.getStyleClass().add("combo-box");

        for (Process p : gameManager.getProcesses()) {
            processDropdown.getItems().add(p.getProcessName());
        }
        for (Resource r : gameManager.getResources()) {
            resourceDropdown.getItems().add(r.getResourceName());
        }

        Button allocateButton = new Button("Allocate");
        allocateButton.getStyleClass().add("success-button");
        allocateButton.setOnAction(e -> handleAllocate());

        Button releaseButton = new Button("Release");
        releaseButton.getStyleClass().add("success-button");
        releaseButton.setOnAction(e -> handleRelease());

        Button finishButton = new Button("Finish");
        finishButton.getStyleClass().add("game-button");
        finishButton.setOnAction(e -> handleFinish());

        Button detectButton = new Button("Detect Deadlock");
        detectButton.getStyleClass().add("game-button");
        detectButton.setOnAction(e -> handleDetectDeadlock());

        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("danger-button");
        restartButton.setOnAction(e -> restartLevel());

        Label processFieldLabel = new Label("Process:");
        processFieldLabel.getStyleClass().add("info-label");
        Label resourceFieldLabel = new Label("Resource:");
        resourceFieldLabel.getStyleClass().add("info-label");

        HBox controlsRow = new HBox(10,
                processFieldLabel, processDropdown,
                resourceFieldLabel, resourceDropdown,
                allocateButton, releaseButton, finishButton, detectButton, restartButton);
        controlsRow.setAlignment(Pos.CENTER);
        controlsRow.setPadding(new Insets(10));

        // ---------- Status + counters row ----------
        statusLabel = new Label("Select a process and resource, then Allocate.");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(500);

        scoreLabel = new Label();
        scoreLabel.getStyleClass().add("score-label");

        timerLabel = new Label();
        timerLabel.getStyleClass().add("timer-label");

        moveCounterLabel = new Label();
        moveCounterLabel.getStyleClass().add("counter-label");

        allocationCounterLabel = new Label();
        allocationCounterLabel.getStyleClass().add("counter-label");

        HBox statsRow = new HBox(25, scoreLabel, timerLabel, moveCounterLabel, allocationCounterLabel);
        statsRow.setAlignment(Pos.CENTER);

        VBox bottomInfo = new VBox(8, statusLabel, statsRow);
        bottomInfo.setAlignment(Pos.CENTER);
        bottomInfo.setPadding(new Insets(10));

        VBox bottomBar = new VBox(8, controlsRow, bottomInfo);
        bottomBar.setAlignment(Pos.CENTER);

        // ---------- Main content column ----------
        sceneContentRoot = new VBox(10, topBar, middleRow, bottomBar);
        sceneContentRoot.getStyleClass().add("root-background");
        sceneContentRoot.setAlignment(Pos.TOP_CENTER);
        sceneContentRoot.setPadding(new Insets(15));

        // ---------- Floating result card layer (warning/safe), overlaid on top ----------
        resultCardLayer = new StackPane();
        resultCardLayer.setPickOnBounds(false);
        resultCardLayer.setMouseTransparent(true); // never intercepts clicks, ever
        resultCardLayer.setAlignment(Pos.TOP_CENTER);
        resultCardLayer.setPadding(new Insets(30, 0, 0, 0));

        StackPane rootStack = new StackPane(sceneContentRoot, resultCardLayer);

        refreshUI();
        startTimer();

        Scene scene = new Scene(rootStack, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }

    // ---------- Button Handlers ----------

    private void handleAllocate() {
        String processName = processDropdown.getValue();
        String resourceName = resourceDropdown.getValue();

        if (processName == null || resourceName == null) {
            statusLabel.setText("Please select both a process and a resource.");
            return;
        }

        moveCount++;
        String message = gameManager.allocateResource(processName, resourceName);
        statusLabel.setText(message);

        allocationCount++;

        refreshUI();
        checkGameEnd();
    }

    private void handleRelease() {
        String processName = processDropdown.getValue();
        String resourceName = resourceDropdown.getValue();

        if (processName == null || resourceName == null) {
            statusLabel.setText("Please select both a process and a resource to release.");
            return;
        }

        moveCount++;
        String message = gameManager.releaseResource(processName, resourceName);
        statusLabel.setText(message);

        refreshUI();
        checkGameEnd();
    }

    private void handleFinish() {
        String processName = processDropdown.getValue();

        if (processName == null) {
            statusLabel.setText("Please select a process to release/finish.");
            return;
        }

        moveCount++;
        String message = gameManager.completeProcess(processName);
        statusLabel.setText(message);

        refreshUI();
        checkGameEnd();
    }

    /**
     * Manually triggers deadlock detection (in addition to the automatic
     * check that already happens inside GameManager.allocateResource()).
     * Shows either the red deadlock visualization or the green safe-state
     * visualization depending on the result.
     */
    private void handleDetectDeadlock() {
        gameManager.checkForDeadlock();
        refreshUI();

        if (gameManager.getState() == GameManager.GameState.LOST_DEADLOCK) {
            List<String> cycleNodes = gameManager.getDetector().getLastCycleNodes();
            graphCanvas.highlightDeadlock(cycleNodes);
            showWarningCard();
            dimInterface(true);
        } else {
            graphCanvas.showSafeState();
            showSafeCard();
        }

        checkGameEnd();
    }

    private void restartLevel() {
        stopTimer();
        gameManager = new GameManager();
        gameManager.loadLevel(level);
        moveCount = 0;
        allocationCount = 0;
        statusLabel.setText("Level restarted.");
        graphCanvas.clearAll();
        graphCanvas.resetDimming();
        dimInterface(false);
        refreshUI();
        startTimer();
    }

    // ---------- UI Refresh Logic ----------

    private void refreshUI() {
        try {
            processListBox.getChildren().clear();
            for (Process p : gameManager.getProcesses()) {
                String info = p.getProcessName()
                        + " | Held: " + p.getHeldResources()
                        + " | Waiting: " + (p.isWaiting() ? p.getWaitingFor() : "-")
                        + " | " + (p.isFinished() ? "FINISHED" : "Active");
                Label row = new Label(info);

                if (p.isFinished()) {
                    row.getStyleClass().add("info-card-finished");
                } else if (p.isWaiting()) {
                    row.getStyleClass().add("info-card-waiting");
                } else {
                    row.getStyleClass().add("info-card");
                }
                row.setWrapText(true);
                row.setMaxWidth(Double.MAX_VALUE);
                processListBox.getChildren().add(row);
            }

            resourceListBox.getChildren().clear();
            for (Resource r : gameManager.getResources()) {
                String info = r.getResourceName()
                        + " | " + (r.isAvailable() ? "Available" : "Held by " + r.getAllocatedTo());
                Label row = new Label(info);
                row.getStyleClass().add(r.isAvailable() ? "info-card-finished" : "info-card");
                row.setWrapText(true);
                row.setMaxWidth(Double.MAX_VALUE);
                resourceListBox.getChildren().add(row);
            }

            graphCanvas.refresh(gameManager);

            scoreLabel.setText("Score: " + gameManager.getScore());
            timerLabel.setText("Time: " + gameManager.getTimeRemainingSeconds() + "s");
            moveCounterLabel.setText("Moves: " + moveCount);
            allocationCounterLabel.setText("Allocations: " + allocationCount);

        } catch (Exception ex) {
            // TEMPORARY diagnostic: if refreshUI throws, we print the full
            // error to console instead of silently breaking the next click.
            System.out.println("=== ERROR in refreshUI ===");
            ex.printStackTrace();
        }
    }

    private void checkGameEnd() {
        GameManager.GameState state = gameManager.getState();

        if (state == GameManager.GameState.WON
                || state == GameManager.GameState.LOST_DEADLOCK
                || state == GameManager.GameState.LOST_TIMEOUT) {

            stopTimer();

            // Give the player a brief moment to see the final visualization
            // before transitioning to the ResultScreen.
            PauseTransition pause = new PauseTransition(Duration.seconds(1.4));
            pause.setOnFinished(e -> {
                ResultScreen resultScreen = new ResultScreen(stage, level, gameManager);
                stage.setScene(resultScreen.getScene());
            });

            if (state == GameManager.GameState.LOST_DEADLOCK) {
                List<String> cycleNodes = gameManager.getDetector().getLastCycleNodes();
                graphCanvas.highlightDeadlock(cycleNodes);
                showWarningCard();
                dimInterface(true);
            } else if (state == GameManager.GameState.WON) {
                graphCanvas.showSafeState();
                showSafeCard();
            }

            pause.play();
        }
    }

    // ---------- Floating Cards (Warning / Safe) ----------

    /**
     * Shows a floating "DEADLOCK DETECTED" card that fades in, shakes for
     * about a second, then stays visible. Purely visual — mouse-transparent
     * so it never blocks clicks to the controls underneath.
     */
    private void showWarningCard() {
        resultCardLayer.getChildren().clear();

        Label warningText = new Label("\u26A0 DEADLOCK DETECTED");
        warningText.getStyleClass().add("warning-card-text");

        StackPane card = new StackPane(warningText);
        card.getStyleClass().add("warning-card");
        card.setMaxWidth(360);

        resultCardLayer.getChildren().add(card);

        card.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), card);
        fade.setToValue(1);

        TranslateTransition shake = new TranslateTransition(Duration.millis(60), card);
        shake.setFromX(-8);
        shake.setToX(8);
        shake.setCycleCount(8);
        shake.setAutoReverse(true);

        SequentialTransition sequence = new SequentialTransition(fade, shake);
        sequence.play();
    }

    /**
     * Shows a floating "SAFE STATE" card that fades in, stays for a moment,
     * then fades back out. Purely visual — mouse-transparent so it never
     * blocks clicks to the controls underneath.
     */
    private void showSafeCard() {
        resultCardLayer.getChildren().clear();

        Label safeText = new Label("\u2714 SAFE STATE");
        safeText.getStyleClass().add("safe-card-text");

        StackPane card = new StackPane(safeText);
        card.getStyleClass().add("safe-card");
        card.setMaxWidth(360);

        resultCardLayer.getChildren().add(card);

        card.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.seconds(1.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), card);
        fadeOut.setToValue(0);

        // Remove the card from the scene graph once fully faded out.
        fadeOut.setOnFinished(e -> resultCardLayer.getChildren().clear());

        SequentialTransition sequence = new SequentialTransition(fadeIn, stay, fadeOut);
        sequence.play();
    }

    /**
     * Dims the side panels and controls (everything except the graph
     * canvas, which handles its own node dimming) to draw focus toward
     * the deadlock visualization. Reversed on restart.
     */
    private void dimInterface(boolean dim) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), sceneContentRoot);
        fade.setToValue(dim ? 0.55 : 1.0);
        fade.play();
    }

    // ---------- Timer Management ----------

    private void startTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            gameManager.tick();
            // Only update the timer label every second — NOT the full
            // graph/canvas rebuild. Rebuilding the whole visualization
            // every second (even with no player action) was heavy for
            // larger levels and interfered with dropdown/button clicks.
            timerLabel.setText("Time: " + gameManager.getTimeRemainingSeconds() + "s");
            checkGameEnd();
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
}