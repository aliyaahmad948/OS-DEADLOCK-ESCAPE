package ui;

import javafx.scene.Scene;
import logic.ProgressManager;

/**
 * ThemeManager.java
 *
 * Central stylesheet switcher for the Navy+Neon theme (dark, default) and its
 * light variant. The active theme lives in ProgressManager so the choice can
 * be remembered across sessions and applied as the app opens.
 */
public final class ThemeManager {

    private static final String DARK_CSS = "/style.css";
    private static final String LIGHT_CSS = "/style-light.css";

    private ThemeManager() {
    }

    /** URL of the stylesheet matching the current theme preference. */
    public static String stylesheetUrl() {
        String path = ProgressManager.getInstance().isDarkMode() ? DARK_CSS : LIGHT_CSS;
        return ThemeManager.class.getResource(path).toExternalForm();
    }

    /** Applies the themed stylesheet to the given scene. */
    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(stylesheetUrl());
    }

    /** Flips the theme preference and re-applies it to the given scene. */
    public static void toggleTheme(Scene scene) {
        ProgressManager.getInstance().setDarkMode(!ProgressManager.getInstance().isDarkMode());
        applyTheme(scene);
    }
}