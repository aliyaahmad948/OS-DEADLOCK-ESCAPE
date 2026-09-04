package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import logic.ProgressManager;
import model.PlayerSession;

/**
 * MenuScreen.java
 *
 * The main menu screen of the game. Shown first when the app opens.
 * Provides three options: Start Game, Leaderboard, Instructions, Exit.
 * Styled using style.css classes, including the Instructions popup.
 */
public class MenuScreen {

    private final Stage stage;
    private final ProgressManager progress;

    public MenuScreen(Stage stage) {
        this.stage = stage;
        this.progress = ProgressManager.getInstance();
    }

    public MenuScreen(Stage stage, ProgressManager progress) {
        this.stage = stage;
        this.progress = progress;
    }

    public Scene getScene() {

        Label titleLabel = new Label("Deadlock Escape Game");
        titleLabel.getStyleClass().add("title-text");

        String playerName = PlayerSession.getInstance().getPlayerName();
        Label greetingLabel = new Label("Welcome, " + playerName + "!");
        greetingLabel.getStyleClass().add("subtitle-text");

        Label subtitleLabel = new Label("Can your processes escape the trap?");
        subtitleLabel.getStyleClass().add("subtitle-text");

        Button startButton = new Button("Start Game");
        Button leaderboardButton = new Button("Leaderboard");
        Button profileButton = new Button("Profile");
        Button instructionsButton = new Button("Instructions");
        Button practiceButton = new Button("Practice Lab");
        Button exitButton = new Button("Exit");
        Button themeButton = new Button(progress.isDarkMode() ? "\uD83C\uDF19 Dark Theme" : "\u2600\uFE0F Light Theme");

        startButton.getStyleClass().add("game-button");
        leaderboardButton.getStyleClass().add("game-button");
        profileButton.getStyleClass().add("game-button");
        instructionsButton.getStyleClass().add("game-button");
        practiceButton.getStyleClass().add("success-button");
        exitButton.getStyleClass().add("danger-button");
        themeButton.getStyleClass().add("game-button");

        startButton.setPrefWidth(200);
        leaderboardButton.setPrefWidth(200);
        profileButton.setPrefWidth(200);
        instructionsButton.setPrefWidth(200);
        practiceButton.setPrefWidth(200);
        exitButton.setPrefWidth(200);
        themeButton.setPrefWidth(200);

        startButton.setOnAction(e -> {
            ModeSelectionScreen modeSelectionScreen = new ModeSelectionScreen(stage, progress);
            stage.setScene(modeSelectionScreen.getScene());
        });

        leaderboardButton.setOnAction(e -> {
            LeaderboardScreen leaderboardScreen = new LeaderboardScreen(stage, progress);
            stage.setScene(leaderboardScreen.getScene());
        });

        practiceButton.setOnAction(e -> {
            PracticeLabScreen practiceLab = new PracticeLabScreen(stage, progress);
            stage.setScene(practiceLab.getScene());
        });

        profileButton.setOnAction(e -> {
            ProfileScreen profileScreen = new ProfileScreen(stage, progress);
            stage.setScene(profileScreen.getScene());
        });

        instructionsButton.setOnAction(e -> showInstructions());

        exitButton.setOnAction(e -> stage.close());

        themeButton.setOnAction(e -> {
            ThemeManager.toggleTheme(stage.getScene());
            themeButton.setText(progress.isDarkMode() ? "\uD83C\uDF19 Dark Theme" : "\u2600\uFE0F Light Theme");
        });

        VBox layout = new VBox(15);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(titleLabel, greetingLabel, subtitleLabel,
                startButton, leaderboardButton, profileButton, instructionsButton, practiceButton, themeButton, exitButton);

        Scene scene = new Scene(layout, 1200, 800);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    /**
     * Shows the instructions dialog, styled to match the Navy+Neon theme:
     * dark background, glowing cyan header, and separate icon-labeled cards
     * for Goal / Allocate / Release / Finish / Warning instead of one plain
     * text block.
     */
    private void showInstructions() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Instructions");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(ThemeManager.stylesheetUrl());
        pane.getStyleClass().add("root-background");

        Label heading = new Label("\uD83D\uDD0D How to Play \u2014 Deadlock Escape Game");
        heading.getStyleClass().add("instr-heading");

        VBox content = new VBox(14,
                heading,
                createInstrCard("\uD83C\uDFAF GOAL",
                        "Guide every process to completion by granting its resources in a safe "
                                + "order. Finish ALL processes before the timer runs out.",
                        "instr-card-goal"),
                createInstrCard("\uD83D\uDCC0 ALLOCATE",
                        "Click a process node (left column), then a resource node (right column) "
                                + "on the graph to select them, then press Allocate. If the resource is "
                                + "free it is granted immediately; if another process holds it, your "
                                + "process starts WAITING.",
                        "instr-card-allocate"),
                createInstrCard("\u21A9 RELEASE",
                        "Release one resource from the selected process back to the system. "
                                + "A process finishes automatically when all of its resources are released.",
                        "instr-card-release"),
                createInstrCard("\u2705 FINISH",
                        "Manually finish a selected process that has no resources left, freeing "
                                + "everything it held.",
                        "instr-card-finish"),
                createInstrCard("\u26A0 WARNING",
                        "If processes wait for each other in a circle, that is a DEADLOCK and "
                                + "the game is lost. Press 'Detect Deadlock' to check your graph before "
                                + "it is too late!",
                        "instr-card-warn"));

        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20, 24, 16, 24));
        pane.setContent(content);

        pane.getButtonTypes().add(new ButtonType("Got it", ButtonBar.ButtonData.OK_DONE));
        dialog.showAndWait();
    }

    private VBox createInstrCard(String title, String body, String styleClass) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("instr-card-title");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("instr-card-text");
        bodyLabel.setWrapText(true);

        VBox card = new VBox(6, titleLabel, bodyLabel);
        card.getStyleClass().add(styleClass);
        card.setMaxWidth(560);
        card.setPadding(new Insets(12, 16, 12, 16));
        return card;
    }
}