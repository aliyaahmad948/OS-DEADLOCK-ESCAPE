package ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import logic.*;

import model.GameMode;
import model.Level;
import model.LevelResult;
import model.Mission;
import model.Process;
import model.Resource;
import ui.graph.GraphCanvas;
import ui.graph.NodeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * GameScreen.java
 *
 * Premium "node-graph" gameplay screen.
 * Layout:
 * Top    -> glowing 'DEADLOCK ESCAPE GAME' title + level name + concept badge + mission banner
 * Left   -> Process list panel
 * Center -> GraphCanvas (animated node-graph visualization)
 * Right  -> Resource list panel
 * Bottom -> Allocate / Release / Finish / Detect Deadlock / Hint / Restart buttons
 *        + Status panel, OS Guide panel, Score, Timer, Stability, Mistakes
 *
 * Educational features (added in the enhancement):
 * - Mission banner showing the level's objective
 * - Concept badge showing which OS concept is being taught
 * - OS Guide panel with contextual reasoning after every action
 * - Hint button (reveals contextual hints, minor score penalty)
 * - System Stability meter (drops on wrong decisions, watched on timeout)
 * - Mistakes counter, deadlocks detected/prevented, recovery actions
 * - Stars + efficiency computed on level completion
 *
 * IMPORTANT: The underlying game logic (GameManager) is UNCHANGED. This class
 * only reads GameManager state and adds the educational/presentation layers.
 */
public class GameScreen {

    private Stage stage;
    private GameManager gameManager;
    private Level level;
    private GameMode mode;
    private ProgressManager progress;
    private HintManager hintManager;

    // Where to go when the player exits mid-game. Null = standard navigation
    // back to the mode's level-select screen.
    private final Runnable exitAction;

    private VBox processListBox;
    private VBox resourceListBox;
    private GraphCanvas graphCanvas;

    private Label scoreLabel;
    private Label timerLabel;
    private Label statusLabel;
    private Label moveCounterLabel;
    private Label allocationCounterLabel;
    private Label stabilityLabel;
    private Label mistakesLabel;
    private Label hintLabel;
    private Label osGuideLabel;
    private Label missionLabel;

    private Label selectionLabel;

    // click-to-select state (Priority 4)
    private String selectedProcess = null;
    private String selectedResource = null;

    private StackPane resultCardLayer;
    private VBox sceneContentRoot;
    private HBox tutorialBar;

    private Timeline gameTimer;

    // UI-only counters
    private int moveCount = 0;
    private int allocationCount = 0;
    private int mistakes = 0;
    private int deadlocksDetected = 0;
    private int deadlocksPrevented = 0;
    private int recoveryActions = 0;
    private int hintsUsed = 0;
    private double stability = 100.0;
    private int elapsedSeconds = 0;
    private int goldBonus = 0;
    private boolean[] usedSafeSequence = {false};
    private final List<String> finishedOrder = new ArrayList<>();

    public GameScreen(Stage stage, Level level) {
        this(stage, new ProgressManager(), level, level.getGameMode());
    }

    public GameScreen(Stage stage, ProgressManager progress, Level level, GameMode mode) {
        this(stage, progress, level, mode, null);
    }

    public GameScreen(Stage stage, ProgressManager progress, Level level, GameMode mode, Runnable exitAction) {
        this.stage = stage;
        this.progress = progress;
        this.level = level;
        this.mode = mode;
        this.exitAction = exitAction;
        this.gameManager = new GameManager();
        this.gameManager.loadLevel(level);
        this.hintManager = new HintManager(level);
    }

    public Scene getScene() {

        // ---------- Top bar: title + level + concept + mission ----------
        Label mainTitle = new Label("DEADLOCK ESCAPE GAME");
        mainTitle.getStyleClass().add("title-text");
        mainTitle.setStyle(mainTitle.getStyle() + "-fx-font-size: 22px;");

        Label conceptBadge = new Label("\uD83C\uDFAF " + mode.getDisplayName());
        conceptBadge.getStyleClass().add("concept-badge");

        Label levelLabel = new Label(level.getLevelName() + "  |  " + level.getDifficulty());
        levelLabel.getStyleClass().add("level-label");

        // Exit / Quit-to-Menu — sits at the top-right, away from the action buttons.
        Button exitButton = new Button("\u2716 Exit");
        exitButton.getStyleClass().add("danger-button");
        exitButton.setPrefWidth(100);
        exitButton.setOnAction(e -> confirmExit());

        Region titleSpacerLeft = new Region();
        titleSpacerLeft.setPrefWidth(115);
        Region titleSpacerRight = new Region();
        titleSpacerRight.setPrefWidth(40);

        HBox titleRow = new HBox(12, titleSpacerLeft, mainTitle, conceptBadge, titleSpacerRight, exitButton);
        titleRow.setAlignment(Pos.CENTER);

        // Mission banner
        Mission mission = level.getMission();
        missionLabel = new Label(mission != null
                ? "\uD83C\uDFC1 MISSION: " + mission.getDescription()
                : "Complete all processes safely.");
        missionLabel.getStyleClass().add("mission-banner");
        missionLabel.setWrapText(true);
        missionLabel.setMaxWidth(1050);
        if (mission != null) {
            Tooltip.install(missionLabel, new Tooltip(mission.getDescription()));
        }

        VBox topBar = new VBox(8, titleRow, levelLabel, missionLabel);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));

        // ---------- Left panel: Processes ----------
        processListBox = new VBox(8);
        ScrollPane processScroll = new ScrollPane(processListBox);
        processScroll.setFitToWidth(true);
        processScroll.setPrefHeight(412);
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
        resourceScroll.setPrefHeight(412);
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
        graphScroll.setPrefHeight(412);
        graphScroll.getStyleClass().add("transparent-scroll");

        VBox centerPanel = new VBox(graphScroll);
        centerPanel.getStyleClass().add("panel-box");
        centerPanel.setPrefWidth(420);

        HBox.setHgrow(centerPanel, Priority.ALWAYS);

        HBox middleRow = new HBox(15, processPanel, centerPanel, resourcePanel);
        middleRow.setAlignment(Pos.CENTER);
        middleRow.setPadding(new Insets(10));

        // ---------- Controls ----------
        // (ComboBoxes removed — selection now happens by clicking graph nodes.)
        selectionLabel = new Label();
        selectionLabel.getStyleClass().add("selection-label");
        refreshSelectionLabel();

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

        Button hintButton = new Button("Hint (" + hintManager.getHintsRemaining() + ")");
        hintButton.getStyleClass().add("game-button");
        hintButton.setOnAction(e -> handleHint());

        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("danger-button");
        restartButton.setOnAction(e -> restartLevel());

        HBox controlsRow = new HBox(10,
                selectionLabel,
                allocateButton, releaseButton, finishButton, detectButton, hintButton, restartButton);
        controlsRow.setAlignment(Pos.CENTER);
        controlsRow.setPadding(new Insets(8));

        // Clicking graph nodes sets the selection (Priority 4)
        graphCanvas.setNodeClickHandler(this::handleNodeClick);

        // ---------- Status + OS Guide ----------
        statusLabel = new Label("Select a process and resource, then Allocate.");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(1100);

        osGuideLabel = new Label("\uD83D\uDCD6 OS GUIDE: Study the graph and find a safe way to finish all processes.");
        osGuideLabel.getStyleClass().add("os-guide-label");
        osGuideLabel.setWrapText(true);
        osGuideLabel.setMaxWidth(1100);

        VBox guideBox = new VBox(6, statusLabel, osGuideLabel);
        guideBox.setAlignment(Pos.CENTER_LEFT);
        guideBox.setPadding(new Insets(4, 12, 4, 12));

        // ---------- Stats row ----------
        scoreLabel = new Label();
        scoreLabel.getStyleClass().add("score-label");

        timerLabel = new Label();
        timerLabel.getStyleClass().add("timer-label");

        moveCounterLabel = new Label();
        moveCounterLabel.getStyleClass().add("counter-label");

        allocationCounterLabel = new Label();
        allocationCounterLabel.getStyleClass().add("counter-label");

        stabilityLabel = new Label();
        stabilityLabel.getStyleClass().add("counter-label");

        mistakesLabel = new Label();
        mistakesLabel.getStyleClass().add("counter-label");

        HBox statsRow = new HBox(25, scoreLabel, timerLabel, moveCounterLabel,
                allocationCounterLabel, stabilityLabel, mistakesLabel);
        statsRow.setAlignment(Pos.CENTER);

        VBox bottomInfo = new VBox(8, guideBox, statsRow);
        bottomInfo.setAlignment(Pos.CENTER);

        // ---------- First-time onboarding tip (once ever, dismissible) ----------
        if (progress.shouldShowGameTutorial()) {
            Label tipText = new Label("\uD83D\uDCA1 FIRST TIME: click a PROCESS node (left) then a RESOURCE "
                    + "node (right) on the graph to select them, then press Allocate.");
            tipText.getStyleClass().add("tutorial-tip");
            tipText.setWrapText(true);

            Button gotItButton = new Button("Got it!");
            gotItButton.getStyleClass().add("game-button");
            gotItButton.setOnAction(e -> dismissTutorial());

            tutorialBar = new HBox(12, tipText, gotItButton);
            tutorialBar.setAlignment(Pos.CENTER);
            tutorialBar.getStyleClass().add("tutorial-bar");
        }

        VBox bottomBar;
        if (tutorialBar != null) {
            bottomBar = new VBox(8, tutorialBar, controlsRow, bottomInfo);
        } else {
            bottomBar = new VBox(8, controlsRow, bottomInfo);
        }
        bottomBar.setAlignment(Pos.CENTER);

        // ---------- Main content ----------
        sceneContentRoot = new VBox(8, topBar, middleRow, bottomBar);
        sceneContentRoot.getStyleClass().add("root-background");
        sceneContentRoot.setAlignment(Pos.TOP_CENTER);
        sceneContentRoot.setPadding(new Insets(12));

        // ---------- Floating result card layer ----------
        resultCardLayer = new StackPane();
        resultCardLayer.setPickOnBounds(false);
        resultCardLayer.setMouseTransparent(true);
        resultCardLayer.setAlignment(Pos.TOP_CENTER);
        resultCardLayer.setPadding(new Insets(25, 0, 0, 0));

        StackPane rootStack = new StackPane(sceneContentRoot, resultCardLayer);

        refreshUI();
        startTimer();

        Scene scene = new Scene(rootStack, 1200, 800);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    // ---------- Button Handlers ----------

    // ---------- Click-to-Select (Priority 4) ----------

    private void handleNodeClick(String id, NodeView.NodeType type) {
        if (type == NodeView.NodeType.PROCESS) {
            if (id.equals(selectedProcess)) {
                selectedProcess = null;      // click selected node again = deselect
            } else {
                selectedProcess = id;        // single selection per type (switch)
            }
        } else {
            if (id.equals(selectedResource)) {
                selectedResource = null;
            } else {
                selectedResource = id;
            }
        }
        refreshSelectionVisuals();
    }

    private void refreshSelectionVisuals() {
        for (Process p : gameManager.getProcesses()) {
            graphCanvas.setNodeSelected(p.getProcessName(), p.getProcessName().equals(selectedProcess));
        }
        for (Resource r : gameManager.getResources()) {
            graphCanvas.setNodeSelected(r.getResourceName(), r.getResourceName().equals(selectedResource));
        }
        refreshSelectionLabel();
    }

    private void refreshSelectionLabel() {
        if (selectionLabel != null) {
            selectionLabel.setText("Selected Process: "
                    + (selectedProcess != null ? selectedProcess : "\u2014")
                    + "  |  Selected Resource: "
                    + (selectedResource != null ? selectedResource : "\u2014"));
        }
    }

    private void clearSelectionState() {
        selectedProcess = null;
        selectedResource = null;
        graphCanvas.clearSelection();
        refreshSelectionLabel();
    }

    // ---------- Exit mid-game (never counts as a loss) ----------

    private void confirmExit() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Exit Level");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(ThemeManager.stylesheetUrl());
        pane.getStyleClass().add("root-background");

        Label heading = new Label("\uD83D\uDEAA Are you sure you want to exit?");
        heading.getStyleClass().add("instr-heading");

        Label body = new Label("Progress on this level will be lost. "
                + "Exiting now will NOT count as a loss and no score will be saved.");
        body.getStyleClass().add("instr-card-text");
        body.setWrapText(true);
        body.setMaxWidth(420);

        VBox content = new VBox(12, heading, body);
        content.setPadding(new Insets(16, 24, 12, 24));
        pane.setContent(content);

        ButtonType confirmType = new ButtonType("Exit Level", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(confirmType, cancelType);

        Button confirmButton = (Button) pane.lookupButton(confirmType);
        confirmButton.getStyleClass().add("confirm-danger");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == confirmType) {
            exitMidGame();
        }
    }

    private void exitMidGame() {
        stopTimer();
        if (exitAction != null) {
            exitAction.run();
        } else {
            ConceptModeLevelScreen levelScreen = new ConceptModeLevelScreen(stage, progress, mode);
            stage.setScene(levelScreen.getScene());
        }
    }

    private void handleAllocate() {
        if (selectedProcess == null || selectedResource == null) {
            statusLabel.setText("Click a PROCESS node and a RESOURCE node on the graph to select them first.");
            osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: click a process node (left column) and a "
                    + "resource node (right column), then press Allocate.");
            return;
        }
        String processName = selectedProcess;
        String resourceName = selectedResource;

        // BANKER'S ALGORITHM INTERCEPT (Deadlock Escape Level 4 "The Banker"
        // and Level 5 "OS Chaos"): the player proposes a request; the system
        // denies it if granting would leave the system in an UNSAFE state.
        // Safe, granted requests earn a gold prevention bonus.
        boolean bankerMode = (mode == GameMode.DEADLOCK_ESCAPE
                && (level.getLevelNumber() == 4 || level.getLevelNumber() == 5));
        if (bankerMode) {
            Process proc = findProcess(processName);
            Resource res = findResource(resourceName);
            if (proc != null && res != null && !res.isAvailable()) {
                boolean safe = gameManager.isRequestSafe(processName, resourceName);
                if (!safe) {
                    moveCount++;
                    recordMistake();
                    stability = Math.max(0, stability - 10);
                    statusLabel.setText("REQUEST DENIED. Granting " + resourceName
                            + " to " + processName + " would leave the system in an UNSAFE STATE.");
                    osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: UNSAFE REQUEST — no safe sequence exists if this "
                            + "request is granted. The Banker's Algorithm rejects it. Find the current safe sequence "
                            + "to release held resources first.");
                    refreshStats();
                    return;
                }
                // safe request — proceed, show the safe-sequence explanation, reward prevention
                moveCount++;
                goldBonus += 25;
                String message = gameManager.allocateResource(processName, resourceName);
                statusLabel.setText(message + "  |  \uD83E\uDD47 GOLD +25 banker-safe move");
                osGuideLabel.setText("\u2714 SAFE REQUEST GRANTED. System remains in a SAFE STATE. "
                        + "Safe sequence: " + safeSequenceText() + ".  Prevention is rewarded!");
                allocationCount++;
                clearSelectionState();
                refreshUI();
                checkGameEnd();
                return;
            }
        }

        moveCount++;
        String message = gameManager.allocateResource(processName, resourceName);
        statusLabel.setText(message);

        Process p = findProcess(processName);
        Resource r = findResource(resourceName);

        if (message.contains("cannot") || message.contains("Invalid")) {
            recordMistake();
        } else if (message.contains("waiting for")) {
            osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: " + OSGuide.waitingExplanation(p, r, mode));
        } else if (message.contains("granted")) {
            osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: " + OSGuide.allocationExplanation(p, r, mode));
            allocationCount++;
        }

        if (gameManager.getState() == GameManager.GameState.LOST_DEADLOCK) {
            deadlocksDetected++;
        }

        boolean applied = message.contains("granted") || message.contains("waiting for");
        if (applied) {
            clearSelectionState();
        }

        refreshUI();
        checkGameEnd();
    }

    private String safeSequenceText() {
        List<String> seq = gameManager.findSafeSequence();
        if (seq.isEmpty()) {
            return "none available";
        }
        return String.join(" \u2192 ", seq);
    }

    private void handleRelease() {
        if (selectedProcess == null || selectedResource == null) {
            statusLabel.setText("Click a PROCESS node and a RESOURCE node on the graph to select them first.");
            osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: click a process node (left column) and a "
                    + "resource node (right column), then press Release.");
            return;
        }
        String processName = selectedProcess;
        String resourceName = selectedResource;

        moveCount++;
        String message = gameManager.releaseResource(processName, resourceName);
        statusLabel.setText(message);

        Process p = findProcess(processName);
        Resource r = findResource(resourceName);

        if (message.contains("does not hold") || message.contains("cannot release")
                || message.contains("Invalid") || message.contains("already finished")) {
            recordMistake();
        } else {
            osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: " + OSGuide.releaseExplanation(p, r, mode));
            if (message.contains("finished!")) {
                recoveryActions++;
                usedSafeSequence[0] = true;
            }
            clearSelectionState();
        }

        refreshUI();
        checkGameEnd();
    }

    private void handleFinish() {
        if (selectedProcess == null) {
            statusLabel.setText("Click a PROCESS node on the graph to select it first.");
            osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: click the process node you want to finish, then press Finish.");
            return;
        }
        String processName = selectedProcess;

        moveCount++;
        String message = gameManager.completeProcess(processName);
        statusLabel.setText(message);

        Process p = findProcess(processName);

        if (message.contains("cannot finish") || message.contains("cannot release")
                || message.contains("Invalid") || message.contains("already finished")) {
            recordMistake();
        } else {
            osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: " + OSGuide.finishExplanation(p, p.getHeldResources()));
            recoveryActions++;
            usedSafeSequence[0] = true;
            clearSelectionState();
        }

        refreshUI();
        checkGameEnd();
    }

    private void handleDetectDeadlock() {
        gameManager.checkForDeadlock();
        refreshUI();

        if (gameManager.getState() == GameManager.GameState.LOST_DEADLOCK) {
            List<String> cycleNodes = gameManager.getDetector().getLastCycleNodes();
            graphCanvas.highlightDeadlock(cycleNodes);
            showWarningCard();
            dimInterface(true);
            deadlocksDetected++;

            // Deadlock explanation + recovery teaching
            String recoveryTip = RecoveryAdvisor.recommendRecovery(gameManager, cycleNodes);
            osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: " + OSGuide.deadlockExplanation(
                    gameManager.getLastMessage(), cycleNodes) + "\n\n\uD83D\uDEE1 " + recoveryTip);

            // Show the four conditions analysis
            missionLabel.setText(missionLabel.getText() + "  |  \u26D4 FOUR CONDITIONS: "
                    + "\u2714 ME  \u2714 H&W  \u2714 NP  \u2714 CW  ->  DEADLOCK CONFIRMED");
        } else {
            graphCanvas.showSafeState();
            showSafeCard();
            deadlocksPrevented++;
            osGuideLabel.setText("\u2714 No deadlock found. The system is in a safe state.");
        }

        // Re-arm stability slightly on successful prevent/analysis
        checkGameEnd();
    }

    private void handleHint() {
        String hint = hintManager.revealHint();
        if (hint == null) {
            statusLabel.setText("No more hints available for this level.");
            return;
        }
        hintsUsed++;
        osGuideLabel.setText("\uD83D\uDD11 HINT: " + hint);
        statusLabel.setText("Hint revealed. Score reduced slightly (-10).");
    }

    private void recordMistake() {
        mistakes++;
        stability = Math.max(0, stability - 5);
        refreshStats();
    }

    private void dismissTutorial() {
        progress.markGameTutorialSeen();
        if (tutorialBar != null) {
            tutorialBar.setVisible(false);
        }
    }

    private void restartLevel() {
        stopTimer();
        gameManager = new GameManager();
        gameManager.loadLevel(level);
        moveCount = 0;
        allocationCount = 0;
        mistakes = 0;
        deadlocksDetected = 0;
        deadlocksPrevented = 0;
        recoveryActions = 0;
        hintsUsed = 0;
        stability = 100.0;
        elapsedSeconds = 0;
        goldBonus = 0;
        finishedOrder.clear();
        statusLabel.setText("Level restarted.");
        osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: Study the graph and find a safe way to finish all processes.");
        missionLabel.setText(level.getMission() != null
                ? "\uD83C\uDFC1 MISSION: " + level.getMission().getDescription()
                : "Complete all processes safely.");
        hintManager = new HintManager(level);
        graphCanvas.clearAll();
        graphCanvas.resetDimming();
        clearSelectionState();
        dimInterface(false);
        refreshUI();
        startTimer();
    }

    // ---------- UI Refresh ----------

    private void refreshUI() {
        try {
            syncFinishedOrder();
            processListBox.getChildren().clear();
            for (Process p : gameManager.getProcesses()) {
                String info = p.getProcessName()
                        + " | Held: " + p.getHeldResources()
                        + " | Waiting: " + (p.isWaiting() ? p.getWaitingFor() : "-")
                        + " | " + (p.isFinished() ? "FINISHED" : "Active");
                Label row = new Label(info);
                row.getStyleClass().add(p.isFinished() ? "info-card-finished"
                        : p.isWaiting() ? "info-card-waiting" : "info-card");
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
            refreshStats();

        } catch (Exception ex) {
            System.out.println("=== ERROR in refreshUI ===");
            ex.printStackTrace();
        }
    }

    private void refreshStats() {
        scoreLabel.setText("Score: " + (gameManager.getScore() + goldBonus) + (goldBonus > 0 ? "  \uD83E\uDD47 GOLD +" + goldBonus : ""));
        timerLabel.setText("Time: " + gameManager.getTimeRemainingSeconds() + "s");
        moveCounterLabel.setText("Moves: " + moveCount);
        allocationCounterLabel.setText("Allocations: " + allocationCount);
        stabilityLabel.setText("Stability: " + Math.round(stability) + "%");
        mistakesLabel.setText("Mistakes: " + mistakes);
    }

    /**
     * Records processes that just finished in completion order and plays a
     * green pulse on their graph node. Called on every UI refresh.
     */
    private void syncFinishedOrder() {
        for (Process p : gameManager.getProcesses()) {
            if (p.isFinished() && !finishedOrder.contains(p.getProcessName())) {
                finishedOrder.add(p.getProcessName());
                graphCanvas.pulseNode(p.getProcessName());
            }
        }
    }

    private void checkGameEnd() {
        GameManager.GameState state = gameManager.getState();

        if (state == GameManager.GameState.WON
                || state == GameManager.GameState.LOST_DEADLOCK
                || state == GameManager.GameState.LOST_TIMEOUT) {

            stopTimer();

            // Build level result
            LevelResult result = buildResult(state);

            PauseTransition pause = new PauseTransition(Duration.seconds(1.4));
            pause.setOnFinished(e -> {
                ResultScreen resultScreen = new ResultScreen(stage, progress, level, gameManager, result, mode);
                stage.setScene(resultScreen.getScene());
            });

            if (state == GameManager.GameState.LOST_DEADLOCK) {
                List<String> cycleNodes = gameManager.getDetector().getLastCycleNodes();
                graphCanvas.highlightDeadlock(cycleNodes);
                showWarningCard();
                dimInterface(true);
                String recoveryTip = RecoveryAdvisor.recommendRecovery(gameManager, cycleNodes);
                osGuideLabel.setText("\uD83D\uDCD6 OS GUIDE: " + OSGuide.deadlockExplanation(
                        gameManager.getLastMessage(), cycleNodes) + "\n\n\uD83D\uDEE1 " + recoveryTip);
            } else if (state == GameManager.GameState.WON) {
                graphCanvas.showSafeState();
                showSafeCard();
                graphCanvas.animateSafeSequence(finishedOrder);
            }

            pause.play();
        }
    }

    private LevelResult buildResult(GameManager.GameState state) {
        LevelResult result = new LevelResult();
        result.setWon(state == GameManager.GameState.WON);
        result.setScore(gameManager.getScore() + goldBonus);
        result.setTimeUsedSeconds(elapsedSeconds);
        result.setMistakes(mistakes);
        result.setProcessesCompleted(countFinished());
        result.setTotalProcesses(gameManager.getProcesses().size());
        result.setDeadlocksDetected(deadlocksDetected);
        result.setDeadlocksPrevented(deadlocksPrevented);
        result.setRecoveryActions(recoveryActions);
        result.setHintsUsed(hintsUsed);
        result.setConceptLearned(mode.getDisplayName());
        result.setLevelName(level.getLevelName());
        result.setLevelNumber(level.getLevelNumber());
        result.setUsedSafeSequence(usedSafeSequence[0]);

        int stars = StarsCalculator.calculateStars(elapsedSeconds, level.getTimeLimitSeconds(),
                mistakes, hintsUsed);
        result.setStars(stars);

        // If lost, minimum 0 stars on result
        if (!result.isWon()) {
            result.setStars(0);
        }

        // Could not reach exactly the stars field if lost
        result.setEfficiencyPercent(StarsCalculator.calculateEfficiency(elapsedSeconds,
                level.getTimeLimitSeconds(), mistakes, hintsUsed));

        if (result.isWon()) {
            progress.completeLevel(mode, level.getLevelNumber(), stars);
        }

        return result;
    }

    private int countFinished() {
        int count = 0;
        for (Process p : gameManager.getProcesses()) {
            if (p.isFinished()) count++;
        }
        return count;
    }

    // ---------- Floating Cards ----------

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
        fadeOut.setOnFinished(e -> resultCardLayer.getChildren().clear());

        SequentialTransition sequence = new SequentialTransition(fadeIn, stay, fadeOut);
        sequence.play();
    }

    private void dimInterface(boolean dim) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), sceneContentRoot);
        fade.setToValue(dim ? 0.55 : 1.0);
        fade.play();
    }

    // ---------- Timer ----------

    private void startTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            gameManager.tick();
            elapsedSeconds++;
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

    private Process findProcess(String name) {
        for (Process p : gameManager.getProcesses()) {
            if (p.getProcessName().equals(name)) return p;
        }
        return null;
    }

    private Resource findResource(String name) {
        for (Resource r : gameManager.getResources()) {
            if (r.getResourceName().equals(name)) return r;
        }
        return null;
    }
}