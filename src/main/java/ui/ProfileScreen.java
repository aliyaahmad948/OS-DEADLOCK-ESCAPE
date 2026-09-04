package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import logic.AchievementTracker;
import logic.ProgressManager;
import model.Achievement;
import model.GameMode;
import model.PlayerSession;

import java.util.List;

/**
 * ProfileScreen.java
 *
 * Shows the player's progress: name, XP, levels completed, concepts mastered,
 * achievements, per-mode star progress, a mastery chart, and overall stats.
 * Two-column layout so the right side is filled with a chart + stats instead
 * of being left empty.
 */
public class ProfileScreen {

    private final Stage stage;
    private final ProgressManager progress;

    public ProfileScreen(Stage stage, ProgressManager progress) {
        this.stage = stage;
        this.progress = progress;
    }

    public Scene getScene() {
        Label titleLabel = new Label("Player Profile");
        titleLabel.getStyleClass().add("title-text");

        Label nameLabel = new Label("Player: " + PlayerSession.getInstance().getPlayerName());
        nameLabel.getStyleClass().add("score-label");

        // ---------- Left column: concepts + achievements ----------
        VBox conceptBox = new VBox(8);
        conceptBox.setAlignment(Pos.CENTER);
        conceptBox.getStyleClass().add("panel-box");
        conceptBox.setMaxWidth(560);
        conceptBox.setPadding(new Insets(15));

        Label conceptHeader = new Label("Concepts Mastered");
        conceptHeader.getStyleClass().add("level-label");
        conceptBox.getChildren().add(conceptHeader);

        for (GameMode mode : GameMode.values()) {
            boolean mastered = progress.conceptMastered(mode);
            int completed = countModeLevels(mode);
            Label row = new Label((mastered ? "\u2714 " : "\u25CB ") + mode.getDisplayName()
                    + "  (" + completed + "/5 levels)");
            row.getStyleClass().add(mastered ? "info-card-finished" : "info-card");
            row.setWrapText(true);
            row.setMaxWidth(500);
            conceptBox.getChildren().add(row);
        }

        VBox achievementBox = new VBox(8);
        achievementBox.setAlignment(Pos.CENTER);
        achievementBox.getStyleClass().add("panel-box");
        achievementBox.setMaxWidth(560);
        achievementBox.setPadding(new Insets(15));

        Label achHeader = new Label("Achievements");
        achHeader.getStyleClass().add("level-label");
        achievementBox.getChildren().add(achHeader);

        List<Achievement> defs = AchievementTracker.allDefinitions();
        for (Achievement a : defs) {
            boolean unlocked = AchievementTracker.isUnlocked(a.getName());
            Label row = new Label((unlocked ? "\u2714 " : "\u25CB ") + a.getName()
                    + " \u2014 " + a.getDescription());
            row.getStyleClass().add(unlocked ? "info-card-finished" : "info-card");
            row.setWrapText(true);
            row.setMaxWidth(500);
            achievementBox.getChildren().add(row);
        }

        ScrollPane contentScroll = new ScrollPane(new VBox(15, conceptBox, achievementBox));
        contentScroll.setFitToWidth(true);
        contentScroll.setPrefHeight(500);
        contentScroll.getStyleClass().add("transparent-scroll");

        // ---------- Right column: mastery chart + overall stats ----------
        VBox chartPanel = createChartPanel();
        VBox statsPanel = createStatsPanel();

        VBox rightColumn = new VBox(15, chartPanel, statsPanel);
        rightColumn.setAlignment(Pos.CENTER);
        rightColumn.setPrefWidth(520);

        HBox mainRow = new HBox(20, contentScroll, rightColumn);
        mainRow.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("danger-button");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> {
            MenuScreen menuScreen = new MenuScreen(stage, progress);
            stage.setScene(menuScreen.getScene());
        });

        Button themeButton = new Button(progress.isDarkMode() ? "\uD83C\uDF19 Dark Theme" : "\u2600\uFE0F Light Theme");
        themeButton.getStyleClass().add("game-button");
        themeButton.setPrefWidth(200);
        themeButton.setOnAction(e -> {
            ThemeManager.toggleTheme(stage.getScene());
            themeButton.setText(progress.isDarkMode() ? "\uD83C\uDF19 Dark Theme" : "\u2600\uFE0F Light Theme");
        });

        HBox footerRow = new HBox(15, backButton, themeButton);
        footerRow.setAlignment(Pos.CENTER);

        Label summaryLabel = new Label("XP: " + progress.getTotalXp()
                + "   |   Levels Completed: " + progress.levelsCompleted() + " / 25");
        summaryLabel.getStyleClass().add("info-label");

        VBox layout = new VBox(15, titleLabel, nameLabel, summaryLabel, mainRow, footerRow);
        layout.getStyleClass().add("root-background");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        Scene scene = new Scene(layout, 1200, 800);
        ThemeManager.applyTheme(scene);
        return scene;
    }

    private VBox createChartPanel() {
        Label chartHeader = new Label("Concept Mastery");
        chartHeader.getStyleClass().add("level-label");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Concept Mode");

        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        yAxis.setLabel("Completed %");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().add("profile-chart");
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mastery");
        for (GameMode mode : GameMode.values()) {
            series.getData().add(new XYChart.Data<>(mode.getDisplayName(), masteryPercent(mode)));
        }
        chart.getData().add(series);

        VBox panel = new VBox(10, chartHeader, chart);
        panel.getStyleClass().add("panel-box");
        panel.setPrefWidth(500);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);
        return panel;
    }

    private VBox createStatsPanel() {
        Label statsHeader = new Label("Overall Stats");
        statsHeader.getStyleClass().add("level-label");

        Label xpRow = new Label("\u26A1 Total XP: " + progress.getTotalXp());
        xpRow.getStyleClass().add("info-card");

        Label avgStarsRow = new Label("\u2B50 Average Stars: " + averageStars());
        avgStarsRow.getStyleClass().add("info-card");

        Label doneRow = new Label("\u2705 Levels Completed: " + progress.levelsCompleted() + " / 25");
        doneRow.getStyleClass().add("info-card");

        Label masteredRow = new Label("\uD83C\uDFAF Concepts Mastered: " + masteredCount() + " / 5");
        masteredRow.getStyleClass().add("info-card");

        Label achRow = new Label("\uD83C\uDFC6 Achievements: "
                + AchievementTracker.getUnlockedAchievements().size() + " / "
                + AchievementTracker.allDefinitions().size());
        achRow.getStyleClass().add("info-card");

        VBox statsBox = new VBox(8, statsHeader, xpRow, avgStarsRow, doneRow, masteredRow, achRow);
        statsBox.getStyleClass().add("panel-box");
        statsBox.setPrefWidth(500);
        statsBox.setPadding(new Insets(15));
        statsBox.setAlignment(Pos.CENTER);
        return statsBox;
    }

    /** Percentage of levels completed in this mode (0-100). */
    private int masteryPercent(GameMode mode) {
        return countModeLevels(mode) * 100 / 5;
    }

    private int masteredCount() {
        int count = 0;
        for (GameMode mode : GameMode.values()) {
            if (progress.conceptMastered(mode)) count++;
        }
        return count;
    }

    private double averageStars() {
        int done = progress.levelsCompleted();
        if (done == 0) return 0.0;
        int total = 0;
        for (GameMode mode : GameMode.values()) {
            for (int i = 1; i <= 5; i++) {
                total += progress.getStarsFor(mode, i);
            }
        }
        return Math.round((total / (double) done) * 10.0) / 10.0;
    }

    private int countModeLevels(GameMode mode) {
        int count = 0;
        for (int i = 1; i <= 5; i++) {
            if (progress.isLevelCompleted(mode, i)) count++;
        }
        return count;
    }
}