package ui;

import db.ScoreDatabase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class LeaderboardScreen {

    private Stage stage;
    private ScoreDatabase db;

    public LeaderboardScreen(Stage stage) {
        this.stage = stage;
        this.db = new ScoreDatabase();
    }

    public Scene getScene() {

        Label titleLabel = new Label("Leaderboard");
        titleLabel.getStyleClass().add("title-text");

        VBox levelsContainer = new VBox(20);
        levelsContainer.setAlignment(Pos.CENTER);

        String[] levelNames = {"Level 1: The Basics", "Level 2: The Long Chain", "Level 3: The Big Trap"};
        for (int i = 1; i <= 3; i++) {
            VBox levelBox = createLevelLeaderboard(i, levelNames[i - 1]);
            levelsContainer.getChildren().add(levelBox);
        }

        ScrollPane scrollPane = new ScrollPane(levelsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("transparent-scroll");
        scrollPane.setPrefHeight(600);

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("danger-button");
        backButton.setOnAction(e -> {
            MenuScreen menuScreen = new MenuScreen(stage);
            stage.setScene(menuScreen.getScene());
        });

        VBox layout = new VBox(20, titleLabel, scrollPane, backButton);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }

    private VBox createLevelLeaderboard(int levelNumber, String levelName) {
        Label levelTitle = new Label(levelName);
        levelTitle.getStyleClass().add("level-label");

        Label highScoreLabel = new Label("Highest Score: " + db.getHighestScore(levelNumber)
                + "  |  Top Player: " + db.getHighestScorer(levelNumber));
        highScoreLabel.getStyleClass().add("score-label");

        List<String[]> scores = db.getLeaderboard(levelNumber);

        VBox scoreRows = new VBox(6);
        scoreRows.setAlignment(Pos.CENTER);

        if (scores.isEmpty()) {
            Label noScores = new Label("No scores recorded yet.");
            noScores.getStyleClass().add("info-label");
            scoreRows.getChildren().add(noScores);
        } else {
            int rank = 1;
            String lastPlayer = "";
            for (String[] row : scores) {
                String playerName = row[0];
                int score = Integer.parseInt(row[1]);
                String timestamp = row[2];

                String display = rank + ".  " + playerName + "  -  Score: " + score + "  (" + timestamp + ")";
                Label rowLabel = new Label(display);
                rowLabel.getStyleClass().add("info-card");
                rowLabel.setWrapText(true);
                rowLabel.setMaxWidth(500);

                if (!playerName.equals(lastPlayer)) {
                    rank++;
                }
                lastPlayer = playerName;

                scoreRows.getChildren().add(rowLabel);
            }
        }

        VBox levelBox = new VBox(8, levelTitle, highScoreLabel, scoreRows);
        levelBox.getStyleClass().add("panel-box");
        levelBox.setAlignment(Pos.CENTER);
        levelBox.setPadding(new Insets(15));
        levelBox.setMaxWidth(600);

        return levelBox;
    }
}
