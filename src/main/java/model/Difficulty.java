package model;

/**
 * Difficulty.java
 *
 * Defines difficulty levels for the Deadlock Escape Game.
 * Difficulty scales across multiple dimensions:
 * - Number of processes and resources
 * - Graph complexity and dependency chains
 * - Timer pressure
 * - Decision complexity
 */
public enum Difficulty {

    EASY("Easy", 1),
    MEDIUM("Medium", 2),
    HARD("Hard", 3),
    EXPERT("Expert", 4),
    CHAOS("Chaos", 5);

    private final String displayName;
    private final int severityLevel;

    Difficulty(String displayName, int severityLevel) {
        this.displayName = displayName;
        this.severityLevel = severityLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }
}
