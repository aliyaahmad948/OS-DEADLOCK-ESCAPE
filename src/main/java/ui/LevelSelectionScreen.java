package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import model.Level;

/**
 * LevelSelectionScreen.java
 *
 * Screen shown after clicking "Start Game" on the main menu.
 * Lets the player choose which level to play (Level 1, 2, or 3).
 * Styled using style.css classes to match the Navy+Neon theme.
 *
 * OS Concept Mapping:
 * Each button loads a different pre-built deadlock scenario (Level).
 */
public class LevelSelectionScreen {

    private Stage stage;

    public LevelSelectionScreen(Stage stage) {
        this.stage = stage;
    }

    /**
     * Builds and returns the Scene for level selection.
     */
    public Scene getScene() {

        Label titleLabel = new Label("Select a Level");
        titleLabel.getStyleClass().add("title-text");

        // Level 1 button (5 processes - Easy)
        Button level1Button = new Button("Level 1: The Basics\n(Easy - 5 Processes)");
        level1Button.getStyleClass().add("game-button");
        level1Button.setPrefWidth(220);
        level1Button.setPrefHeight(70);
        level1Button.setOnAction(e -> startLevel(Level.createLevel1()));

        // Level 2 button (10 processes - Medium)
        Button level2Button = new Button("Level 2: The Long Chain\n(Medium - 10 Processes)");        level2Button.getStyleClass().add("game-button");
        level2Button.setPrefWidth(220);
        level2Button.setPrefHeight(70);
        level2Button.setOnAction(e -> startLevel(Level.createLevel2()));

        // Level 3 button (15 processes - Hard)
        Button level3Button = new Button("Level 3: The Big Trap\n(Hard - 15 Processes)");
        level3Button.getStyleClass().add("game-button");
        level3Button.setPrefWidth(220);
        level3Button.setPrefHeight(70);
        level3Button.setOnAction(e -> startLevel(Level.createLevel3()));

        // Back button - returns to Main Menu
        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("danger-button");
        backButton.setOnAction(e -> {
            MenuScreen menuScreen = new MenuScreen(stage);
            stage.setScene(menuScreen.getScene());
        });

        HBox levelButtonsRow = new HBox(20, level1Button, level2Button, level3Button);
        levelButtonsRow.setAlignment(Pos.CENTER);

        VBox layout = new VBox(30);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(titleLabel, levelButtonsRow, backButton);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }

    /**
     * Called when a level button is clicked.
     * Navigates to the GameScreen, loaded with the chosen level.
     */
    private void startLevel(Level level) {
        GameScreen gameScreen = new GameScreen(stage, level);
        stage.setScene(gameScreen.getScene());
    }
}