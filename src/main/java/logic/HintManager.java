package logic;

import model.GameMode;
import model.Level;
import model.Process;

import java.util.ArrayList;
import java.util.List;

/**
 * HintManager.java
 *
 * Provides contextual, progressive hints for each level.
 * Hints become more specific ("giveaway") with each use.
 * Using a hint adds a small score penalty (applied via GameManager).
 */
public class HintManager {

    private final Level level;
    private final GameMode mode;
    private int hintsRevealed;
    private final int maxHints;

    public HintManager(Level level) {
        this.level = level;
        this.mode = level.getGameMode();
        this.maxHints = Math.max(1, level.getHintCount());
        this.hintsRevealed = 0;
    }

    public boolean hasHintsAvailable() {
        return hintsRevealed < maxHints;
    }

    public int getHintsRemaining() {
        return Math.max(0, maxHints - hintsRevealed);
    }

    public int getHintsUsed() {
        return hintsRevealed;
    }

    /**
     * Reveals the next contextual hint. Returns null if no hints remain.
     */
    public String revealHint() {
        if (!hasHintsAvailable()) {
            return null;
        }
        String hint = buildHint(hintsRevealed);
        hintsRevealed++;
        return hint;
    }

    private String buildHint(int index) {
        List<String> hints = new ArrayList<>();

        switch (mode) {
            case MUTUAL_EXCLUSION:
                hints.add("Check which process currently holds the resource another process wants.");
                hints.add("A resource can only be held by ONE process at a time. Release it first, then re-allocate.");
                hints.add("Find the process with no outstanding request — it can finish immediately and unlock the chain.");
                break;
            case HOLD_AND_WAIT:
                hints.add("Look for processes that HOLD resources while WAITING for more.");
                hints.add("Trace the dependency from the waiting process to the resource holder.");
                hints.add("Finish the process that is not waiting for any resource — it becomes the first link in the safe sequence.");
                break;
            case NO_PREEMPTION:
                hints.add("You cannot take a resource by force. The process holding it must release it first.");
                hints.add("Release owned resources in order, starting from the process with the fewest held resources.");
                hints.add("The escape hatch process (no request) frees the most resources fastest.");
                break;
            case CIRCULAR_WAIT:
                hints.add("Look for a CYCLE in the resource allocation graph.");
                hints.add("Trace the dependency from P1: which resource is it waiting for? Who holds it?");
                hints.add("If every process in a loop waits for a resource held by the next, that is a deadlock cycle.");
                break;
            case DEADLOCK_ESCAPE:
                hints.add("Think SAFETY: can every remaining process eventually finish?");
                hints.add("Check for circular waits; avoid granting a request that completes a loop.");
                hints.add("Recover by finishing processes in the safe sequence order.");
                break;
            default:
                hints.add("Look for an available resource and manage it carefully.");
        }

        if (index < hints.size()) {
            return hints.get(index);
        }
        return hints.get(hints.size() - 1);
    }
}