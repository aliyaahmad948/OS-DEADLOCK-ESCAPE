package logic;

import model.GameMode;
import model.Process;
import model.Resource;

import java.util.List;

/**
 * OSGuide.java
 *
 * Generates contextual, educational explanations for every action the player
 * takes. The purpose is to teach Operating System deadlock concepts through
 * gameplay reasoning rather than long tutorials.
 *
 * Every message is tied to the player's LAST ACTION and the current game state,
 * reinforcing the EXPERIENCE -> ACTION -> CONSEQUENCE -> REASONING -> CONCEPT
 * learning cycle.
 */
public class OSGuide {

    private OSGuide() {
    }

    /**
     * Returns the educational explanation for a successfully allocated resource.
     */
    public static String allocationExplanation(Process p, Resource r, GameMode mode) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2714 ").append(r.getResourceName()).append(" was available, so ")
                .append(p.getProcessName()).append(" successfully acquired it.\n");

        if (mode == GameMode.MUTUAL_EXCLUSION) {
            sb.append("\nMUTUAL EXCLUSION\n");
            sb.append("A resource can be held by only ONE process at a time.\n");
            sb.append("Now ").append(r.getResourceName()).append(" is exclusively allocated to ")
                    .append(p.getProcessName()).append(".");
        }
        return sb.toString();
    }

    /**
     * Returns the educational explanation when a process ends up WAITING because
     * the requested resource is held by another process.
     */
    public static String waitingExplanation(Process p, Resource r, GameMode mode) {
        String holder = r.getAllocatedTo();
        StringBuilder sb = new StringBuilder();
        sb.append("\u26A0 ").append(p.getProcessName()).append(" is WAITING because ")
                .append(r.getResourceName()).append(" is currently held by ").append(holder).append(".\n");

        switch (mode) {
            case MUTUAL_EXCLUSION:
                sb.append("\nWhy? MUTUAL EXCLUSION\n");
                sb.append(r.getResourceName()).append(" is non-shareable — only ")
                        .append(holder).append(" can use it right now.");
                break;
            case HOLD_AND_WAIT:
                sb.append("\nHOLD AND WAIT\n");
                sb.append(p.getProcessName()).append(" is holding resources while waiting for ").append(holder)
                        .append(" to release ").append(r.getResourceName()).append(".");
                break;
            case NO_PREEMPTION:
                sb.append("\nNO PREEMPTION\n");
                sb.append(r.getResourceName()).append(" cannot be forcibly taken from ").append(holder)
                        .append(" while it is using it.");
                break;
            case CIRCULAR_WAIT:
                sb.append("\nCIRCULAR WAIT\n");
                sb.append("Check the dependency graph — this wait may be part of a cycle.");
                break;
            case DEADLOCK_ESCAPE:
                sb.append("\nAll four conditions check:\n");
                sb.append("\u2714 Mutual Exclusion\n\u2714 Hold and Wait\n\u2714 No Preemption\n");
                sb.append("If this wait forms a cycle with other processes, that is CIRCULAR WAIT -> DEADLOCK.");
                break;
        }
        return sb.toString();
    }

    /**
     * Returns the educational explanation when a process successfully releases a resource.
     */
    public static String releaseExplanation(Process p, Resource r, GameMode mode) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2714 ").append(p.getProcessName()).append(" released ").append(r.getResourceName())
                .append(". The resource is now available.\n");

        if (mode == GameMode.NO_PREEMPTION) {
            sb.append("\nA resource is released only by the process that owns it — the OS\n");
            sb.append("does NOT forcibly take it away. That is NO PREEMPTION.");
        }
        return sb.toString();
    }

    /**
     * Explanation when a process finishes.
     */
    public static String finishExplanation(Process p, List<String> releasedResources) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2714 ").append(p.getProcessName())
                .append(" finished and released all held resources.\n");
        if (releasedResources != null && !releasedResources.isEmpty()) {
            sb.append("Released: ").append(String.join(", ", releasedResources)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Explanation when a deadlock is detected.
     */
    public static String deadlockExplanation(String reason, List<String> cycle) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u26D4 DEADLOCK DETECTED\n\n");
        if (cycle != null && !cycle.isEmpty()) {
            sb.append("Cycle: ").append(String.join(" \u2192 ", cycle)).append("\n\n");
        }
        sb.append("Reason: Circular Wait\n");
        sb.append("The resource allocation graph contains a cycle of processes waiting\n");
        sb.append("for resources held by each other. No process can proceed.");
        return sb.toString();
    }
}