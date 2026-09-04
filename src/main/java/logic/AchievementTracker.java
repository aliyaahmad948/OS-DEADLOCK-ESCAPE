package logic;

/**
 * AchievementTracker.java
 *
 * Tracks lifetime statistics and unlocks achievements when thresholds are met.
 * Achievements are session-persistent via static counters.
 *
 * ACHIEVEMENTS:
 * - Deadlock Hunter    : Detect 10 deadlocks
 * - System Guardian    : Prevent 10 deadlocks (safe-state checks / avoidance)
 * - Banker             : Grant 10 safe requests (found safe sequences)
 * - Recovery Expert    : Perform 10 recovery actions
 * - Speed Runner       : Finish a level using under half the time limit
 * - OS Master          : Complete all 25 levels
 */
public class AchievementTracker {

    // Unlocked achievement names
    private static final java.util.Set<String> unlocked = new java.util.HashSet<>();

    // Lifetime counters
    public static int totalDeadlocksDetected = 0;
    public static int totalDeadlocksPrevented = 0;
    public static int totalSafeSequences = 0;
    public static int totalRecoveryActions = 0;
    public static int totalLevelsCompleted = 0;
    public static int starsEarned = 0;

    public static final String DEADLOCK_HUNTER = "Deadlock Hunter";
    public static final String SYSTEM_GUARDIAN = "System Guardian";
    public static final String BANKER = "Banker";
    public static final String RECOVERY_EXPERT = "Recovery Expert";
    public static final String SPEED_RUNNER = "Speed Runner";
    public static final String OS_MASTER = "OS Master";

    private AchievementTracker() {
    }

    /**
     * Updates stats and returns the name of any newly-unlocked achievement,
     * or null if nothing new was unlocked.
     */
    public static String recordLevelOutcome(boolean won, int deadlocksDetected, int deadlocksPrevented,
                                            int recoveryActions, boolean usedSafeSequence,
                                            int timeUsedSeconds, int timeLimitSeconds, int stars) {
        totalDeadlocksDetected += deadlocksDetected;
        totalDeadlocksPrevented += deadlocksPrevented;
        totalRecoveryActions += recoveryActions;
        if (usedSafeSequence) totalSafeSequences++;
        if (won) {
            totalLevelsCompleted++;
            starsEarned += stars;
        }

        String newly = null;

        if (totalDeadlocksDetected >= 10) newly = tryUnlock(DEADLOCK_HUNTER, newly);
        if (totalDeadlocksPrevented >= 10) newly = tryUnlock(SYSTEM_GUARDIAN, newly);
        if (totalSafeSequences >= 10) newly = tryUnlock(BANKER, newly);
        if (totalRecoveryActions >= 10) newly = tryUnlock(RECOVERY_EXPERT, newly);

        if (won && timeLimitSeconds > 0
                && timeUsedSeconds <= timeLimitSeconds / 2) {
            newly = tryUnlock(SPEED_RUNNER, newly);
        }

        if (totalLevelsCompleted >= 25) newly = tryUnlock(OS_MASTER, newly);

        return newly;
    }

    private static String tryUnlock(String achievement, String alreadyFound) {
        if (unlocked.add(achievement)) {
            return alreadyFound == null ? achievement : alreadyFound;
        }
        return alreadyFound;
    }

    public static boolean isUnlocked(String achievement) {
        return unlocked.contains(achievement);
    }

    public static java.util.Set<String> getUnlockedAchievements() {
        return new java.util.HashSet<>(unlocked);
    }

    /**
     * All achievement definitions with descriptions (for the Profile screen).
     */
    public static java.util.List<model.Achievement> allDefinitions() {
        java.util.List<model.Achievement> defs = new java.util.ArrayList<>();
        defs.add(new model.Achievement(DEADLOCK_HUNTER, "Detect 10 deadlocks."));
        defs.add(new model.Achievement(SYSTEM_GUARDIAN, "Prevent 10 deadlocks / reject 10 unsafe requests."));
        defs.add(new model.Achievement(BANKER, "Find 10 safe sequences / grant 10 safe requests."));
        defs.add(new model.Achievement(RECOVERY_EXPERT, "Perform 10 recovery actions."));
        defs.add(new model.Achievement(SPEED_RUNNER, "Finish a level in under half the time limit."));
        defs.add(new model.Achievement(OS_MASTER, "Complete all 25 levels."));
        return defs;
    }
}