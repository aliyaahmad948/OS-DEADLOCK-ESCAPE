package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import ui.LoginScreen;
import ui.MenuScreen;

/**
 * Main.java
 *
 * Entry point of the JavaFX application.
 * Instead of relying on Stage.setMaximized(true) (which can silently
 * un-toggle on Windows when setScene() is called), we directly size and
 * position the window to match the full visible screen area every time
 * the scene changes. This is a more reliable way to keep every screen
 * (Menu -> Level Select -> Game -> Result) filling the whole screen.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        LoginScreen loginScreen = new LoginScreen(primaryStage);
        Scene scene = loginScreen.getScene();

        primaryStage.setTitle("Deadlock Escape Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);

        // Whenever ANY screen calls stage.setScene(...), re-apply full
        // screen sizing right after, so every screen stays fullscreen.
        primaryStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> fillScreen(primaryStage));
            }
        });

        fillScreen(primaryStage);
        primaryStage.show();
    }

    /**
     * Resizes and repositions the given stage to exactly match the
     * visible bounds of the primary screen (excludes the taskbar).
     * More reliable across screen changes than setMaximized(true).
     */
    private void fillScreen(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    public static void main(String[] args) {
        launch(args);
    }
}