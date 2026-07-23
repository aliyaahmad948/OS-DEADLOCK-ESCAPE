package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import model.PlayerSession;

/**
 * MenuScreen.java
 *
 * The main menu screen of the game. Shown first when the app opens.
 * Provides three options: Start Game, Instructions, Exit.
 * Styled using style.css classes, including the Instructions popup.
 */
public class MenuScreen {

    private Stage stage;

    public MenuScreen(Stage stage) {
        this.stage = stage;
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
        Button instructionsButton = new Button("Instructions");
        Button exitButton = new Button("Exit");

        startButton.getStyleClass().add("game-button");
        leaderboardButton.getStyleClass().add("game-button");
        instructionsButton.getStyleClass().add("game-button");
        exitButton.getStyleClass().add("danger-button");

        startButton.setPrefWidth(200);
        leaderboardButton.setPrefWidth(200);
        instructionsButton.setPrefWidth(200);
        exitButton.setPrefWidth(200);

        startButton.setOnAction(e -> {
            LevelSelectionScreen levelSelectionScreen = new LevelSelectionScreen(stage);
            stage.setScene(levelSelectionScreen.getScene());
        });

        leaderboardButton.setOnAction(e -> {
            LeaderboardScreen leaderboardScreen = new LeaderboardScreen(stage);
            stage.setScene(leaderboardScreen.getScene());
        });

        instructionsButton.setOnAction(e -> showInstructions());

        exitButton.setOnAction(e -> stage.close());

        VBox layout = new VBox(15);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(titleLabel, greetingLabel, subtitleLabel, startButton, leaderboardButton, instructionsButton, exitButton);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }

    /**
     * Shows the instructions popup, styled with the same dark
     * Navy+Neon theme as the rest of the app (instead of default white).
     */
    private void showInstructions() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instructions");
        alert.setHeaderText("How to Play - Deadlock Escape Game");
        alert.setContentText(
                "Each Process needs Resources to finish its task.\n\n" +
                        "ALLOCATE: Select process + resource, click Allocate.\n" +
                        "  - If free, resource is granted immediately.\n" +
                        "  - If held by another process, your process starts WAITING.\n\n" +
                        "RELEASE: Select process + resource, click Release.\n" +
                        "  - Releases only ONE resource from the process.\n" +
                        "  - Process auto-finishes when all resources are released.\n\n" +
                        "FINISH: Manually finish a process with no resources left.\n\n" +
                        "GOAL: Release resources in the right order to finish every process.\n" +
                        "WARNING: If processes wait for each other in a circle, " +
                        "that's a DEADLOCK and you lose!"
        );

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("panel-box");

        alert.showAndWait();
    }
}