package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Level.java
 *
 * Represents a single Level of the Deadlock Escape Game.
 * This class is DATA-DRIVEN by design — a level is just a bundle of
 * data (process names, resource names, initial allocations, and requests).
 * This makes it easy to add more levels later without changing any
 * game logic code.
 *
 * OS Concept Mapping:
 * Level = A specific deadlock scenario/puzzle setup
 * initialAllocations = Which resource each process starts by holding
 * initialRequests = Which resource each process starts by waiting for
 */
public class Level {

    private int levelNumber;
    private String levelName;
    private String difficulty;

    private List<String> processNames;
    private List<String> resourceNames;

    // Initial allocations: each entry means "this process holds this resource" at level start.
    private List<String[]> initialAllocations;

    // Initial requests: each entry means "this process is waiting for this resource" at level start.
    private List<String[]> initialRequests;

    private int timeLimitSeconds;

    public Level(int levelNumber, String levelName, String difficulty, int timeLimitSeconds) {
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.difficulty = difficulty;
        this.timeLimitSeconds = timeLimitSeconds;
        this.processNames = new ArrayList<>();
        this.resourceNames = new ArrayList<>();
        this.initialAllocations = new ArrayList<>();
        this.initialRequests = new ArrayList<>();
    }

    // ---------- Methods to build up level data ----------

    public void addProcess(String processName) {
        if (!processNames.contains(processName)) {
            processNames.add(processName);
        }
    }

    public void addResource(String resourceName) {
        if (!resourceNames.contains(resourceName)) {
            resourceNames.add(resourceName);
        }
    }

    public void addInitialAllocation(String processName, String resourceName) {
        initialAllocations.add(new String[]{processName, resourceName});
    }

    public void addInitialRequest(String processName, String resourceName) {
        initialRequests.add(new String[]{processName, resourceName});
    }

    // ---------- Getters ----------

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getLevelName() {
        return levelName;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public List<String> getProcessNames() {
        return processNames;
    }

    public List<String> getResourceNames() {
        return resourceNames;
    }

    public List<String[]> getInitialAllocations() {
        return initialAllocations;
    }

    public List<String[]> getInitialRequests() {
        return initialRequests;
    }

    /**
     * Static factory method that builds Level 1 data.
     *
     * Level 1: 5 processes, 5 resources — a SAFE chain (no deadlock).
     * Scenario:
     * P1 holds R1, wants R2
     * P2 holds R2, wants R3
     * P3 holds R3, wants R4
     * P4 holds R4, wants R5
     * P5 holds R5, waits for nothing (free to finish immediately)
     *
     * Safe order: finish P5 -> P4 -> P3 -> P2 -> P1 (each release unlocks the next).
     * This teaches the player the basic idea of a safe sequence.
     */
    public static Level createLevel1() {
        int count = 5;
        Level level = new Level(1, "Level 1: The Basics", "Easy", 120);
        buildChainScenario(level, count);
        return level;
    }

    /**
     * Static factory method that builds Level 2 data.
     *
     * Level 2: 10 processes, 10 resources — a LONGER safe chain.
     * Scenario: P1 holds R1 wants R2, P2 holds R2 wants R3, ... , P9 holds R9 wants R10,
     * P10 holds R10 and waits for nothing (the "escape hatch").
     *
     * Solve order: finish P10 -> P9 -> P8 -> ... -> P1.
     *
     * NOTE: This previously used buildCircularScenario(), which created a full
     * circular wait with NO process able to go first — that made the level a
     * guaranteed, permanent deadlock with no way to win. Changed to
     * buildChainScenario() so the level is fully solvable, just with more
     * steps than Level 1.
     */
    public static Level createLevel2() {
        int count = 10;
        Level level = new Level(2, "Level 2: The Long Chain", "Medium", 150);
        buildChainScenario(level, count);
        return level;
    }

    /**
     * Static factory method that builds Level 3 data (OPTIONAL bonus level).
     *
     * Level 3: 15 processes, 15 resources — the LONGEST safe chain.
     * Solve order: finish P15 -> P14 -> ... -> P1.
     *
     * NOTE: Also switched from buildCircularScenario() to buildChainScenario()
     * for the same reason as Level 2 — a full circular wait cannot be won.
     */
    public static Level createLevel3() {
        int count = 15;
        Level level = new Level(3, "Level 3: The Big Trap", "Hard", 200);
        buildChainScenario(level, count);
        return level;
    }

    // ---------- Reusable scenario builders (keep levels data-driven) ----------

    /**
     * Builds a SAFE chain scenario with the given number of processes/resources:
     * P(i) holds R(i) and wants R(i+1), except the LAST process which holds
     * its resource but requests nothing (so it can finish immediately,
     * unlocking the chain in reverse order).
     */
    private static void buildChainScenario(Level level, int count) {
        for (int i = 1; i <= count; i++) {
            level.addProcess("P" + i);
            level.addResource("R" + i);
        }

        for (int i = 1; i <= count; i++) {
            level.addInitialAllocation("P" + i, "R" + i);
        }

        // Every process except the last one requests the "next" resource in the chain.
        for (int i = 1; i < count; i++) {
            level.addInitialRequest("P" + i, "R" + (i + 1));
        }
        // The last process (P{count}) intentionally has NO request, so it can
        // finish immediately and start unlocking the chain.
    }

    /**
     * Builds a FULL CIRCULAR WAIT scenario with the given number of
     * processes/resources: P(i) holds R(i) and wants R(i+1), and the LAST
     * process wraps around to want R(1). This closes the loop, guaranteeing
     * a deadlock (circular wait) among all processes.
     *
     * NOTE: Kept here for reference / possible future use (e.g. a dedicated
     * "unsolvable demo" mode), but no longer called by createLevel2() or
     * createLevel3(), since a full circular wait cannot be won by the player.
     */
    private static void buildCircularScenario(Level level, int count) {
        for (int i = 1; i <= count; i++) {
            level.addProcess("P" + i);
            level.addResource("R" + i);
        }

        for (int i = 1; i <= count; i++) {
            level.addInitialAllocation("P" + i, "R" + i);
        }

        for (int i = 1; i <= count; i++) {
            int nextResourceIndex = (i % count) + 1; // wraps last process back to R1
            level.addInitialRequest("P" + i, "R" + nextResourceIndex);
        }
    }

    /**
     * Returns a readable summary of this level's setup.
     * Useful for debugging in console tests.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(levelName).append(" (").append(difficulty).append(")\n");
        sb.append("Processes: ").append(processNames).append("\n");
        sb.append("Resources: ").append(resourceNames).append("\n");
        sb.append("Allocations: ");
        for (String[] a : initialAllocations) {
            sb.append(a[0]).append("->holds->").append(a[1]).append("  ");
        }
        sb.append("\nRequests: ");
        for (String[] r : initialRequests) {
            sb.append(r[0]).append("->wants->").append(r[1]).append("  ");
        }
        return sb.toString();
    }
}