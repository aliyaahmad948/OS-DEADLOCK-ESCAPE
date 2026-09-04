package logic;

import db.ScoreDatabase;
import model.GameMode;
import model.PlayerSession;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * ProgressManager.java
 *
 * In-memory progress tracking for the current session:
 * - Which levels are unlocked
 * - Which levels are completed (with stars)
 * - Which concepts have been mastered
 *
 * Structure: a 2D grid indexed by [mode][level-1].
 * Supports the level-unlocking system: Level 1 of each mode starts unlocked,
 * completing a level unlocks the next one in the same mode.
 *
 * Lightweight preferences (theme + onboarding flag) are persisted to a small
 * properties file in the user home directory so they survive app restarts.
 */
public class ProgressManager {

    private static ProgressManager instance;

    private static final String PREFS_FILE =
            System.getProperty("user.home", ".") + "/.deadlock-prefs.properties";

    private final int[][] starsGrid; // [mode][level-1] = stars earned (0 = not completed)
    private final boolean[][] unlocked;
    private int totalXp;
    private boolean hasSeenGameTutorial = false;
    private boolean darkMode = true;

    public ProgressManager() {
        int modes = GameMode.values().length;
        int levelsPerMode = 5;
        this.starsGrid = new int[modes][levelsPerMode];
        this.unlocked = new boolean[modes][levelsPerMode];

        // Level 1 of every mode starts unlocked
        for (GameMode mode : GameMode.values()) {
            unlocked[mode.ordinal()][0] = true;
        }

        loadPrefs();
        loadProgressFromDatabase();
    }

    /**
     * Returns the session-wide shared instance so progress persists across
     * screen changes for the entire run of the application.
     */
    public static ProgressManager getInstance() {
        if (instance == null) {
            instance = new ProgressManager();
        }
        return instance;
    }

    public boolean isLevelUnlocked(GameMode mode, int levelNumber) {
        int m = mode.ordinal();
        int l = levelNumber - 1;
        if (l < 0 || l >= 5) return false;
        return unlocked[m][l];
    }

    public int getStarsFor(GameMode mode, int levelNumber) {
        int m = mode.ordinal();
        int l = levelNumber - 1;
        if (l < 0 || l >= 5) return 0;
        return starsGrid[m][l];
    }

    public boolean isLevelCompleted(GameMode mode, int levelNumber) {
        return getStarsFor(mode, levelNumber) > 0;
    }

    /**
     * Marks a level as completed with the given stars (1-3).
     * Unlocks the next level in the same mode.
     * Adds XP proportional to stars earned.
     */
    public void completeLevel(GameMode mode, int levelNumber, int stars) {
        int m = mode.ordinal();
        int l = levelNumber - 1;
        if (l < 0 || l >= 5) return;

        if (stars > starsGrid[m][l]) {
            int delta = stars - starsGrid[m][l];
            starsGrid[m][l] = stars;
            totalXp += delta * 10;
        }

        if (l + 1 < 5) {
            unlocked[m][l + 1] = true;
        }
    }

    /**
     * Restores stars + level unlocks from the MySQL level_progress table so
     * profile progress survives app restarts. No-op if the player hasn't
     * logged in yet or the database is unreachable (in that case the game
     * simply stays session-only).
     */
    public void loadProgressFromDatabase() {
        String player = PlayerSession.getInstance().getPlayerName();
        if (player == null || player.isBlank()) return;

        List<String[]> rows;
        try {
            ScoreDatabase db = new ScoreDatabase();
            rows = db.loadLevelProgress(player);
            if (!db.isAvailable()) return;
        } catch (RuntimeException e) {
            return;
        }

        // Save progress back to memory (idempotent — using GREATEST-style merge)
        for (String[] row : rows) {
            try {
                GameMode mode = GameMode.valueOf(row[0]);
                int level = Integer.parseInt(row[1]) - 1;
                int stars = Integer.parseInt(row[2]);
                if (level < 0 || level >= 5 || stars <= 0) continue;

                int m = mode.ordinal();
                if (stars > starsGrid[m][level]) {
                    totalXp += (stars - starsGrid[m][level]) * 10;
                    starsGrid[m][level] = stars;
                }
                if (level + 1 < 5) {
                    unlocked[m][level + 1] = true;
                }
            } catch (RuntimeException ignored) {
                // one malformed row must not break the whole load
            }
        }
    }

    public int getTotalXp() {
        return totalXp;
    }

    public int levelsCompleted() {
        int count = 0;
        for (int m = 0; m < starsGrid.length; m++) {
            for (int l = 0; l < starsGrid[m].length; l++) {
                if (starsGrid[m][l] > 0) count++;
            }
        }
        return count;
    }

    public boolean conceptMastered(GameMode mode) {
        int m = mode.ordinal();
        for (int l = 0; l < 5; l++) {
            if (starsGrid[m][l] == 0) return false;
        }
        return true;
    }

    // ---------- Session preferences / onboarding ----------

    /**
     * True until the player has seen (and dismissed) the first-time
     * GameScreen tutorial hint. Never shown twice per session.
     */
    public boolean shouldShowGameTutorial() {
        return !hasSeenGameTutorial;
    }

    public void markGameTutorialSeen() {
        hasSeenGameTutorial = true;
        savePrefs();
    }

    /** True = Navy + Neon dark theme (default). False = light variant. */
    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        savePrefs();
    }

    // ---------- Preference persistence ----------

    private void loadPrefs() {
        if (!Files.exists(Paths.get(PREFS_FILE))) return;
        try (InputStream in = new FileInputStream(PREFS_FILE)) {
            Properties props = new Properties();
            props.load(in);
            this.darkMode = Boolean.parseBoolean(props.getProperty("darkMode", "true"));
            this.hasSeenGameTutorial = Boolean.parseBoolean(props.getProperty("tutorialSeen", "false"));
        } catch (IOException ignored) {
            // corrupt/unreadable prefs file — fall back to defaults
        }
    }

    private void savePrefs() {
        try {
            Properties props = new Properties();
            props.setProperty("darkMode", String.valueOf(darkMode));
            props.setProperty("tutorialSeen", String.valueOf(hasSeenGameTutorial));
            try (FileOutputStream out = new FileOutputStream(PREFS_FILE)) {
                props.store(out, "Deadlock Simulator preferences");
            }
        } catch (IOException ignored) {
            // non-critical preference persistence — never crash the game
        }
    }
}