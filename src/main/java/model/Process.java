package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Process.java
 *
 * Represents a "Process" in the Deadlock Escape Game.
 * In OS terms: a Process is like a running program that holds
 * some resources and may be waiting for others.
 *
 * OS Concept Mapping:
 * Process = Character in the game
 * heldResources = Resources this character is currently holding
 * waitingFor = The single resource this character is currently blocked on
 * finished = Whether this process has completed successfully (escaped)
 */
public class Process {

    // Name of the process (e.g., "P1", "P2")
    private String processName;

    // List of resource names currently held by this process
    private List<String> heldResources;

    // Name of the resource this process is currently waiting for.
    // If null or empty, the process is not waiting (not blocked).
    private String waitingFor;

    // Whether this process has finished (completed its task and released resources)
    private boolean finished;

    /**
     * Constructor - creates a new Process with a given name.
     * Starts with no held resources, not waiting, and not finished.
     */
    public Process(String processName) {
        this.processName = processName;
        this.heldResources = new ArrayList<>();
        this.waitingFor = null;
        this.finished = false;
    }

    // ---------- Getters ----------

    public String getProcessName() {
        return processName;
    }

    public List<String> getHeldResources() {
        return heldResources;
    }

    public String getWaitingFor() {
        return waitingFor;
    }

    public boolean isFinished() {
        return finished;
    }

    // ---------- Setters ----------

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setWaitingFor(String waitingFor) {
        this.waitingFor = waitingFor;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    // ---------- Resource Management Methods ----------

    /**
     * Adds a resource to this process's held resources list.
     * Also clears waitingFor if this process was waiting for that same resource,
     * since it has now been granted.
     */
    public void addResource(String resourceName) {
        if (!heldResources.contains(resourceName)) {
            heldResources.add(resourceName);
        }
        // If the process was waiting for this exact resource, it is no longer waiting.
        if (resourceName.equals(waitingFor)) {
            waitingFor = null;
        }
    }

    /**
     * Removes a resource from this process's held resources list.
     * Used when a process releases a resource (e.g., on completion).
     */
    public void removeResource(String resourceName) {
        heldResources.remove(resourceName);
    }

    /**
     * Checks whether this process currently holds a given resource.
     */
    public boolean holdsResource(String resourceName) {
        return heldResources.contains(resourceName);
    }

    /**
     * Checks whether this process is currently blocked (waiting for a resource).
     */
    public boolean isWaiting() {
        return waitingFor != null && !waitingFor.isEmpty();
    }

    /**
     * Returns a readable summary of this process's state.
     * Useful for debugging in console tests (Phase 2 checkpoint).
     */
    @Override
    public String toString() {
        return processName
                + " | Held: " + heldResources
                + " | WaitingFor: " + (waitingFor == null ? "-" : waitingFor)
                + " | Finished: " + finished;
    }
}