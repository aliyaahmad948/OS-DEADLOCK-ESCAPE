package logic;

import model.Process;

import java.util.ArrayList;
import java.util.List;

/**
 * RecoveryAdvisor.java
 *
 * Teaches the fourth deadlock strategy: RECOVERY (resource preemption).
 * When the player's own careless request closes a wait-for cycle and a
 * deadlock is detected, this advisor analyzes the cycle and recommends the
 * most ECONOMICAL preemption — roll back the cycle process that holds the
 * fewest resources, releasing its held resource so the cycle is broken.
 *
 * This is a pure teaching/recommendation component; it never mutates game state.
 */
public final class RecoveryAdvisor {

    private RecoveryAdvisor() {
    }

    /**
     * Returns a human-readable recovery recommendation for the current deadlock.
     *
     * @param gameManager the live game (read-only access to process state)
     * @param cycleNodes  the process/resource names involved in the wait-for cycle
     */
    public static String recommendRecovery(GameManager gameManager, List<String> cycleNodes) {
        if (cycleNodes == null || cycleNodes.isEmpty()) {
            return "No cycle information available. Remember: deadlock needs all four "
                    + "Coffman conditions (Mutual Exclusion, Hold & Wait, No Preemption, "
                    + "Circular Wait) to be true at the same time.";
        }

        List<Process> cycleProcesses = new ArrayList<>();
        for (Process p : gameManager.getProcesses()) {
            if (p.isWaiting() && cycleNodes.contains(p.getProcessName())) {
                cycleProcesses.add(p);
            }
        }

        // Cheapest preemption candidate = the cycle member holding the FEWEST
        // resources, because rolling it back loses the least work.
        Process candidate = null;
        int minHeld = Integer.MAX_VALUE;
        for (Process p : cycleProcesses) {
            int held = p.getHeldResources().size();
            if (held < minHeld) {
                minHeld = held;
                candidate = p;
            }
        }

        if (candidate == null) {
            return "RECOVERY: break the Circular Wait by rolling back any process "
                    + "in the cycle to preempt its held resource.";
        }

        return "RECOVERY ADVISOR: the wait-for cycle is [" + String.join(" -> ", cycleNodes)
                + "]. Break the Circular Wait condition by PREEMPTING a resource: roll back "
                + candidate.getProcessName() + " (conceptually it was granted "
                + (minHeld == 1 ? "1 resource" : minHeld + " resources")
                + " — the cheapest member to undo). One preempted resource unblocks the next "
                + "process in the cycle, and the deadlock disappears.";
    }
}