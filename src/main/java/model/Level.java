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
 *
 * PHASE 1 REFACTOR (backward-compatible):
 * Added optional metadata fields for the enhanced game architecture:
 * - gameMode        : which concept mode this level belongs to
 * - difficultyEnum  : difficulty rating
 * - conceptTaught   : what OS concept this level teaches
 * - mission         : the level's mission/objectives
 * - hintCount       : how many hints are available for this level
 *
 * All original fields, methods, and the original constructor are preserved
 * exactly so existing Level.createLevel1()/createLevel2()/createLevel3()
 * keep working without modification.
 */
public class Level {

    private int levelNumber;
    private String levelName;
    private String difficulty;

    // ----- Phase 1: new metadata fields (backward-compatible) -----
    private GameMode gameMode;
    private Difficulty difficultyEnum;
    private String conceptTaught;
    private Mission mission;
    private int hintCount;

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
        this.hintCount = 0;
    }

    /**
     * PHASE 1: Overloaded constructor that also accepts a GameMode.
     * Delegates to the original constructor for core fields, then stores
     * the game mode and sets default metadata. Does NOT change the original
     * constructor behavior.
     */
    public Level(int levelNumber, String levelName, String difficulty, int timeLimitSeconds, GameMode gameMode) {
        this(levelNumber, levelName, difficulty, timeLimitSeconds);
        this.gameMode = gameMode;
        this.conceptTaught = (gameMode == null) ? null : gameMode.getDisplayName();
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

    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
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

    // ---------- Phase 1: new metadata getters & setters (backward-compatible) ----------

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        if (gameMode != null) {
            this.conceptTaught = gameMode.getDisplayName();
        }
    }

    public Difficulty getDifficultyEnum() {
        return difficultyEnum;
    }

    public void setDifficultyEnum(Difficulty difficultyEnum) {
        this.difficultyEnum = difficultyEnum;
    }

    public String getConceptTaught() {
        return conceptTaught;
    }

    public void setConceptTaught(String conceptTaught) {
        this.conceptTaught = conceptTaught;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public int getHintCount() {
        return hintCount;
    }

    public void setHintCount(int hintCount) {
        this.hintCount = hintCount;
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
     * Level 2: 8 processes, 8 resources — BRANCHING scenario.
     *
     * Two separate chains share a common resource:
     *   Chain A: P1->R1->P2->R2->P3 (P3 has no request, escape hatch A)
     *   Chain B: P4->R4->P5->R5->P6 (P6 has no request, escape hatch B)
     *   Cross:  P7 holds R7, wants R2 (shared with Chain A)
     *           P8 holds R3, wants R8 (shared with Chain B)
     *
     * Two valid safe paths exist, but finishing wrong process first
     * can trap the cross-linked processes in deadlock.
     */
    public static Level createLevel2() {
        Level level = new Level(2, "Level 2: The Crossroads", "Medium", 120);

        String[] processes = {"P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8"};
        String[] resources = {"R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8"};
        for (String p : processes) level.addProcess(p);
        for (String r : resources) level.addResource(r);

        // Allocations
        level.addInitialAllocation("P1", "R1");
        level.addInitialAllocation("P2", "R2");
        level.addInitialAllocation("P3", "R3");
        level.addInitialAllocation("P4", "R4");
        level.addInitialAllocation("P5", "R5");
        level.addInitialAllocation("P6", "R6");
        level.addInitialAllocation("P7", "R7");
        level.addInitialAllocation("P8", "R8");

        // Requests - Chain A
        level.addInitialRequest("P1", "R2");
        level.addInitialRequest("P2", "R3");

        // Chain B
        level.addInitialRequest("P4", "R5");
        level.addInitialRequest("P5", "R6");

        // Cross-links (make it tricky)
        level.addInitialRequest("P7", "R2");
        level.addInitialRequest("P8", "R8");

        return level;
    }

    /**
     * Level 3: 12 processes, 12 resources — SHARED RESOURCE chains.
     *
     * Three chains that share resources between them:
     *   Chain A: P1->R1->P2->R2->P3->R3->P4 (P4 escape hatch)
     *   Chain B: P5->R5->P6->R6->P7->R7->P8 (P8 escape hatch)
     *   Chain C: P9->R9->P10->R10->P11->R11->P12 (P12 escape hatch)
     *   Cross-links:
     *     P2 also wants R6 (shared with Chain B)
     *     P6 also wants R9 (shared with Chain C)
     *     P10 also wants R3 (shared with Chain A)
     *
     * Must finish escape hatches in specific order, then unwind chains.
     * Wrong order on cross-links -> deadlock.
     */
    public static Level createLevel3() {
        Level level = new Level(3, "Level 3: The Web", "Hard", 150);

        String[] processes = {"P1","P2","P3","P4","P5","P6","P7","P8","P9","P10","P11","P12"};
        String[] resources = {"R1","R2","R3","R4","R5","R6","R7","R8","R9","R10","R11","R12"};
        for (String p : processes) level.addProcess(p);
        for (String r : resources) level.addResource(r);

        // Allocations
        for (int i = 1; i <= 12; i++) {
            level.addInitialAllocation("P" + i, "R" + i);
        }

        // Chain A internal
        level.addInitialRequest("P1", "R2");
        level.addInitialRequest("P2", "R3");
        level.addInitialRequest("P3", "R4");

        // Chain B internal
        level.addInitialRequest("P5", "R6");
        level.addInitialRequest("P6", "R7");
        level.addInitialRequest("P7", "R8");

        // Chain C internal
        level.addInitialRequest("P9", "R10");
        level.addInitialRequest("P10", "R11");
        level.addInitialRequest("P11", "R12");

        // Cross-links (make it a web)
        level.addInitialRequest("P2", "R6");
        level.addInitialRequest("P6", "R9");
        level.addInitialRequest("P10", "R3");

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