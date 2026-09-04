package model;

/**
 * GameMode.java
 *
 * Defines the five major game modes in Deadlock Escape Game.
 * Each mode teaches a specific Operating System deadlock concept.
 *
 * MUTUAL_EXCLUSION   - Teach that a resource can be used by only one process at a time.
 * HOLD_AND_WAIT      - Teach that a process can hold resources while waiting for others.
 * NO_PREEMPTION      - Teach that resources cannot be forcibly taken from a process.
 * CIRCULAR_WAIT      - Teach circular dependency between processes and resources.
 * DEADLOCK_ESCAPE    - Combined mode: all concepts together.
 */
public enum GameMode {

    MUTUAL_EXCLUSION(
            "Mutual Exclusion",
            "Master resource sharing",
            "Learn why a resource can be used by only one process at a time."
    ),
    HOLD_AND_WAIT(
            "Hold & Wait",
            "Learn resource dependencies",
            "Understand how processes hold resources while waiting for others."
    ),
    NO_PREEMPTION(
            "No Preemption",
            "Understand resource ownership",
            "Discover why resources cannot be forcibly taken from a process."
    ),
    CIRCULAR_WAIT(
            "Circular Wait",
            "Find the deadly cycle",
            "Identify circular dependencies that lead to deadlock."
    ),
    DEADLOCK_ESCAPE(
            "Deadlock Escape",
            "Combine ALL concepts",
            "Apply all four Coffman conditions to survive the final challenge."
    );

    private final String displayName;
    private final String shortDescription;
    private final String fullDescription;

    GameMode(String displayName, String shortDescription, String fullDescription) {
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    /**
     * Returns the number of levels per mode (constant across all modes).
     */
    public int getLevelCount() {
        return 5;
    }
}
