package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import model.PlayerSession;

public class LoginScreen {

    private Stage stage;

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {

        Label titleLabel = new Label("Deadlock Escape Game");
        titleLabel.getStyleClass().add("title-text");

        Label subtitleLabel = new Label("Enter your name to begin");
        subtitleLabel.getStyleClass().add("subtitle-text");

        Label nameLabel = new Label("Player Name:");
        nameLabel.getStyleClass().add("info-label");

        TextField nameField = new TextField();
        nameField.setPromptText("Type your name here...");
        nameField.getStyleClass().add("login-text-field");
        nameField.setPrefWidth(280);
        nameField.setPrefHeight(40);

        Label errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-label");

        Button playButton = new Button("Play");
        playButton.getStyleClass().add("game-button");
        playButton.setPrefWidth(200);

        playButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                errorLabel.setText("Please enter your name!");
                return;
            }
            PlayerSession.getInstance().setPlayerName(name);
            MenuScreen menuScreen = new MenuScreen(stage);
            stage.setScene(menuScreen.getScene());
        });

        nameField.setOnAction(e -> playButton.fire());

        VBox layout = new VBox(15);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(titleLabel, subtitleLabel, nameLabel, nameField, errorLabel, playButton);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }
}
