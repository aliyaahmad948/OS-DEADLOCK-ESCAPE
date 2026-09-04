package logic;

import model.Difficulty;
import model.GameMode;
import model.Level;
import model.Mission;

import java.util.ArrayList;
import java.util.List;

/**
 * LevelFactory.java
 *
 * Generates all level configurations for the Deadlock Escape Game.
 * Every game mode has 5 levels (20 concept levels + 5 combined levels = 25 total).
 *
 * IMPORTANT SOLVABILITY RULE:
 * In this game, a state where DeadlockDetector already finds a cycle is
 * UNWINNABLE — every process in the cycle is WAITING, and waiting processes
 * cannot release resources. Therefore every generated level MUST start in a
 * deadlock-FREE state (a safe state with a valid safe sequence).
 *
 * The circular-wait teaching happens when the player CREATES a cycle through a
 * careless request; the auto-detection then highlights the red cycle so the
 * player learns "that was Circular Wait".
 *
 * Level structures per mode (all guaranteed solvable, difficulty = structure):
 *  - MUTUAL_EXCLUSION : one contested hub, then parallel hubs, then a mesh of
 *                       overlapping contests (only one holder per resource).
 *  - HOLD_AND_WAIT    : chains where a process holds MULTIPLE resources while
 *                       waiting, branching into independent wait-chains.
 *  - NO_PREEMPTION    : converging chains of different lengths feeding a shared
 *                       non-preemptible resource (escape hatch).
 *  - CIRCULAR_WAIT    : open rings that would CLOSE into a cycle on the wrong
 *                       request, plus free distractor processes.
 *  - DEADLOCK_ESCAPE  : combined challenges (chain, contention, multi-hold,
 *                       banker hub, chaos web).
 *
 * Every level starts with P(i) holding R(i); wait-edges point a waiter at the
 * resource held by another process. All shapes are acyclic, so each level has
 * a valid safe sequence (verified by ensureSolvable before returning).
 */
public class LevelFactory {

    // Level name templates per mode
    private static final String[][] LEVEL_NAMES = {
            // MUTUAL_EXCLUSION
            {"The First Loop", "The Chain", "The Network", "Hidden Trap", "The Deadly Loop"},
            // HOLD_AND_WAIT
            {"Simple Hold", "Double Dependency", "Waiting Chain", "Long Dependency", "Hold & Wait Web"},
            // NO_PREEMPTION
            {"Ownership Basics", "Locked Resource", "Contested Resource", "Owner's Dilemma", "No Preemption Trap"},
            // CIRCULAR_WAIT
            {"First Circle", "Triple Cycle", "Hidden Cycle", "Deep Web", "The Perfect Loop"},
            // DEADLOCK_ESCAPE (combined)
            {"The Trap", "Detect the Deadlock", "Escape the Deadlock", "The Banker", "OS Chaos"}
    };

    private LevelFactory() {
    }

    /**
     * Returns all levels for a given game mode (5 levels per mode).
     */
    public static List<Level> getLevelsForMode(GameMode mode) {
        List<Level> levels = new ArrayList<>();
        for (int i = 0; i < mode.getLevelCount(); i++) {
            levels.add(createLevel(mode, i + 1));
        }
        return levels;
    }

    /**
     * Returns every level across all modes, ordered by mode then level number.
     * Total: 25 levels.
     */
    public static List<Level> getAllLevels() {
        List<Level> levels = new ArrayList<>();
        for (GameMode mode : GameMode.values()) {
            levels.addAll(getLevelsForMode(mode));
        }
        return levels;
    }

    /**
     * Creates a single level for the given mode and level number (1-5).
     */
    public static Level createLevel(GameMode mode, int levelNumber) {
        Level level = buildBase(mode, levelNumber);
        buildScenario(level, mode, levelNumber);
        return ensureSolvable(level);
    }

    // -----------------------------------------------------------------
    // Scenario builders
    // -----------------------------------------------------------------

    private static void buildScenario(Level level, GameMode mode, int levelNumber) {
        switch (mode) {
            case MUTUAL_EXCLUSION:
                buildMutualExclusionLevel(level, levelNumber, processCountFor(levelNumber));
                break;
            case HOLD_AND_WAIT:
                buildHoldAndWaitLevel(level, levelNumber);
                break;
            case NO_PREEMPTION:
                buildNoPreemptionLevel(level, levelNumber);
                break;
            case CIRCULAR_WAIT:
                buildCircularWaitLevel(level, levelNumber);
                break;
            case DEADLOCK_ESCAPE:
                buildCombinedLevel(level, levelNumber);
                break;
            default:
                buildVanillaChain(level);
        }
    }

    /**
     * Core scaffold: P(i) creates process P(i), resource R(i), and holds R(i).
     */
    private static void addCoreProcesses(Level level, int n) {
        for (int i = 1; i <= n; i++) {
            level.addProcess("P" + i);
            level.addResource("R" + i);
            level.addInitialAllocation("P" + i, "R" + i);
        }
    }

    /**
     * P(from) is waiting for R(to) — the resource held by P(to).
     * Every waiter waits on exactly ONE resource (model constraint).
     */
    private static void addWait(Level level, int from, int to) {
        level.addInitialRequest("P" + from, "R" + to);
    }

    /**
     * Give P(holder) an EXTRA held resource (R(resourceIndex)) on top of its core one.
     */
    private static void addExtraHeld(Level level, int resourceIndex, int holder) {
        level.addResource("R" + resourceIndex);
        level.addInitialAllocation("P" + holder, "R" + resourceIndex);
    }

    private static void buildFromShape(Level level, int n, int[][] waitPairs, int[][] extraHolds) {
        addCoreProcesses(level, n);
        for (int[] pair : waitPairs) {
            addWait(level, pair[0], pair[1]);
        }
        for (int[] hold : extraHolds) {
            addExtraHeld(level, hold[0], hold[1]);
        }
    }

    /**
     * Linear dependency chain (solvable by finishing in reverse order):
     * P(i) holds R(i) and requests R(i+1), except the LAST process which holds
     * its resource and requests nothing (the escape hatch).
     */
    private static void buildVanillaChain(Level level) {
        int n = processCountFor(level.getLevelNumber());
        addCoreProcesses(level, n);
        for (int i = 1; i < n; i++) {
            addWait(level, i, i + 1);
        }
        // P(n) is the free escape hatch.
    }

    /**
     * Mutual Exclusion: contested hubs (many processes want the SAME held
     * resource). Later levels add parallel hubs and a mesh of contests.
     * All shapes are acyclic; hub holders themselves wait on nothing.
     */
    private static void buildMutualExclusionLevel(Level level, int levelNumber, int n) {
        switch (levelNumber) {
            case 1: // simple chain, one teacher of the "follow the free process" rule
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}}, new int[][]{});
                break;
            case 2: // one contested hub: P2, P5, P6 all want R3 (held by P3)
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {5, 3}, {6, 3}}, new int[][]{});
                break;
            case 3: // two separate hubs in parallel: R3 and R7
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {4, 3}, {5, 6}, {6, 7}, {8, 7}}, new int[][]{});
                break;
            case 4: // heavy hub R5 (4-way contest) + secondary race on R3
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {6, 5}, {7, 5}, {8, 5}, {9, 3}},
                        new int[][]{});
                break;
            default: // mesh: contested R6 + contested R11 + connecting chain
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 6}, {3, 6}, {5, 6}, {7, 6},
                                {8, 9}, {9, 10}, {10, 11}, {12, 11}}, new int[][]{});
                break;
        }
    }

    /**
     * Hold and Wait: SOME processes hold MULTIPLE resources while waiting for
     * one more. Deeper levels branch the waits and scale the order-dependence
     * of who may be released first.
     */
    private static void buildHoldAndWaitLevel(Level level, int levelNumber) {
        int n = processCountFor(levelNumber);
        switch (levelNumber) {
            case 1:
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}}, new int[][]{});
                break;
            case 2: // full chain + one double-holder
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}},
                        new int[][]{{7, 3}});
                break;
            case 3: // branching chains + two double-holders feeding one hub R8
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 8}, {6, 8}, {7, 8}},
                        new int[][]{{9, 3}, {10, 4}});
                break;
            case 4: // two sub-chains + two contention points + two double-holders
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 10},
                                {7, 5}, {8, 5}, {9, 10}},
                        new int[][]{{11, 3}, {12, 6}});
                break;
            default: // web: many clusters, three double-holders, deep unwind order
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 12},
                                {7, 6}, {8, 9}, {9, 10}, {10, 11}, {11, 12}},
                        new int[][]{{13, 3}, {14, 6}, {15, 11}});
                break;
        }
    }

    /**
     * No Preemption: nothing can be taken from a busy process — chains of
     * DIFFERENT lengths converge on a shared non-preemptible escape resource.
     * Later levels add multiple convergence points and deeper tails.
     */
    private static void buildNoPreemptionLevel(Level level, int levelNumber) {
        int n = processCountFor(levelNumber);
        switch (levelNumber) {
            case 1: // two short chains converge on R4
                buildFromShape(level, n,
                        new int[][]{{2, 4}, {3, 4}}, new int[][]{});
                break;
            case 2: // chain depth 3 and depth 2 converge on R6
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 6}, {4, 5}, {5, 6}}, new int[][]{});
                break;
            case 3: // two independent convergence hubs: R4 and R8
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {5, 4}, {6, 7}, {7, 8}},
                        new int[][]{});
                break;
            case 4: // big convergence R5 + separate deep tail ending at R10
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {6, 5}, {7, 5},
                                {8, 9}, {9, 10}}, new int[][]{});
                break;
            default: // grand convergence: one heavily-guarded escape R12
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 12},
                                {7, 8}, {8, 12}, {9, 10}, {10, 12}, {11, 12}},
                        new int[][]{});
                break;
        }
    }

    /**
     * Circular Wait: OPEN rings — the graph looks cyclic but each ring has an
     * escape process; a careless request closes a ring into a real cycle.
     * Distractors (free processes) hide the escape. Late levels stack several
     * independent open rings, forcing the player to reason about each.
     */
    private static void buildCircularWaitLevel(Level level, int levelNumber) {
        int n = processCountFor(levelNumber);
        switch (levelNumber) {
            case 1: // single open ring
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}}, new int[][]{});
                break;
            case 2: // longer open ring + 1 distractor
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}}, new int[][]{});
                break;
            case 3: // two open rings + 1 distractor
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {5, 6}, {6, 7}}, new int[][]{});
                break;
            case 4: // two open rings of different lengths + 2 distractors
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {6, 7}, {7, 8}},
                        new int[][]{});
                break;
            default: // three open rings + 3 distractors
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {5, 6}, {6, 7}, {8, 9}},
                        new int[][]{});
                break;
        }
    }

    /**
     * Deadlock Escape (combined): mixes all four concepts, escalating from a
     * readable chain through contention + multi-hold to a true chaos web.
     */
    private static void buildCombinedLevel(Level level, int levelNumber) {
        int n = processCountFor(levelNumber);
        switch (levelNumber) {
            case 1:
                // The Trap: introduce the four conditions on a readable chain
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}}, new int[][]{});
                break;
            case 2:
                // Detect the Deadlock: chain + contested hand-off to a free hub
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 6}, {5, 3}}, new int[][]{});
                break;
            case 3:
                // Escape the Deadlock: contention on R5 with a side race and a double-holder
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {7, 5}, {8, 4}},
                        new int[][]{{9, 3}});
                break;
            case 4:
                // The Banker: many sources feed one escape hub R9 through R10
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 9}, {5, 6}, {6, 9},
                                {7, 8}, {8, 9}, {9, 10}},
                        new int[][]{{11, 3}});
                break;
            default:
                // OS Chaos: contention, branching chains and a multi-holder hub
                buildFromShape(level, n,
                        new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 6}, {5, 6}, {7, 8},
                                {8, 9}, {9, 6}, {10, 11}, {11, 12}},
                        new int[][]{{13, 2}, {14, 6}});
                break;
        }
    }

    /**
     * Safety net: every generated level MUST start solvable and deadlock-free
     * (a cycle at start would be unwinnable). Unless it does, fall back to the
     * vanilla chain for that level so the game can never ship an unwinnable level.
     */
    private static Level ensureSolvable(Level level) {
        GameManager gm = new GameManager();
        gm.loadLevel(level);
        boolean solvable = !gm.findSafeSequence().isEmpty();
        gm.checkForDeadlock();
        boolean deadlocked = gm.getState() == GameManager.GameState.LOST_DEADLOCK;
        if (!solvable || deadlocked) {
            System.out.println("WARN LevelFactory: " + level.getGameMode() + " L"
                    + level.getLevelNumber() + " started unsolvable — falling back to vanilla chain.");
            Level safe = buildBase(level.getGameMode(), level.getLevelNumber());
            buildVanillaChain(safe);
            return safe;
        }
        return level;
    }

    private static Level buildBase(GameMode mode, int levelNumber) {
        String name = LEVEL_NAMES[mode.ordinal()][levelNumber - 1];
        Difficulty difficulty = difficultyFor(mode, levelNumber);
        int timeLimit = timeLimitFor(difficulty, levelNumber);
        Level level = new Level(levelNumber, name, difficulty.getDisplayName(), timeLimit, mode);
        level.setDifficultyEnum(difficulty);
        level.setMission(createMission(mode, levelNumber));
        level.setHintCount(1 + difficulty.getSeverityLevel());
        return level;
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private static Difficulty difficultyFor(GameMode mode, int levelNumber) {
        if (mode == GameMode.DEADLOCK_ESCAPE) {
            switch (levelNumber) {
                case 1: return Difficulty.EASY;
                case 2: return Difficulty.MEDIUM;
                case 3: return Difficulty.HARD;
                case 4: return Difficulty.EXPERT;
                case 5: return Difficulty.CHAOS;
            }
        }
        switch (levelNumber) {
            case 1: return Difficulty.EASY;
            case 2: return Difficulty.MEDIUM;
            case 3: return Difficulty.MEDIUM;
            case 4: return Difficulty.HARD;
            default: return Difficulty.EXPERT;
        }
    }

    private static int processCountFor(int levelNumber) {
        switch (levelNumber) {
            case 1: return 4;
            case 2: return 6;
            case 3: return 8;
            case 4: return 10;
            default: return 12;
        }
    }

    private static int timeLimitFor(Difficulty difficulty, int levelNumber) {
        switch (difficulty) {
            case EASY:   return 120;
            case MEDIUM: return 120;
            case HARD:   return 100;
            case EXPERT: return 90;
            case CHAOS:  return 90;
            default:     return 120;
        }
    }

    private static Mission createMission(GameMode mode, int levelNumber) {
        Mission mission;
        switch (mode) {
            case MUTUAL_EXCLUSION:
                mission = new Mission("Complete all processes without creating a deadlock. Remember: a resource can only be held by one process at a time (Mutual Exclusion). " + structureFlavor(mode, levelNumber));
                break;
            case HOLD_AND_WAIT:
                mission = new Mission("Complete all processes. Watch how processes hold resources while waiting for others (Hold and Wait). " + structureFlavor(mode, levelNumber));
                break;
            case NO_PREEMPTION:
                mission = new Mission("Complete all processes. You cannot forcibly take a resource from a process while it uses it — solve the contention (No Preemption). " + structureFlavor(mode, levelNumber));
                break;
            case CIRCULAR_WAIT:
                mission = new Mission("Find the real escape hatch and finish every process. Be careful — a careless request can close the loop and create circular wait! " + structureFlavor(mode, levelNumber));
                break;
            case DEADLOCK_ESCAPE:
            default:
                mission = new Mission("Navigate this combined challenge using all four deadlock concepts to escape safely. " + structureFlavor(mode, levelNumber));
                break;
        }

        if (levelNumber >= 3) {
            mission.addBonusObjective("Finish within " + timeLimitFor(difficultyFor(mode, levelNumber), levelNumber) + " seconds");
            mission.setTargetTimeSeconds(timeLimitFor(difficultyFor(mode, levelNumber), levelNumber));
        }
        if (levelNumber >= 4) {
            mission.setMaxActions(levelNumber * 8);
        }

        return mission;
    }

    /**
     * Per-level structural flavor — tells the player exactly what NEW shape
     * this level adds, so difficulty reads as a change in graph structure,
     * not just "more nodes".
     */
    private static String structureFlavor(GameMode mode, int levelNumber) {
        switch (mode) {
            case MUTUAL_EXCLUSION:
                switch (levelNumber) {
                    case 1: return "Follow the simple chain from the free process backwards.";
                    case 2: return "Three processes now compete for ONE held resource — who goes first?";
                    case 3: return "Two separate contested resources run in parallel — solve each independently.";
                    case 4: return "A heavily contested resource plus a secondary race on another edge.";
                    default: return "A mesh of overlapping contests — every choice must respect the one-holder rule.";
                }
            case HOLD_AND_WAIT:
                switch (levelNumber) {
                    case 1: return "A straight chain where each process holds one resource while waiting.";
                    case 2: return "One process now holds TWO resources while waiting — release order matters.";
                    case 3: return "Branching chains meet at a hub, and two processes hold extra resources.";
                    case 4: return "Two contention points across deeper chains with more double-holders.";
                    default: return "A web of waiting clusters — several processes hold multiple resources, so unwind order is critical.";
                }
            case NO_PREEMPTION:
                switch (levelNumber) {
                    case 1: return "Two short chains race for one guarded resource.";
                    case 2: return "Two chains of different lengths converge on a single guard.";
                    case 3: return "Two convergence points in separate regions.";
                    case 4: return "A big convergence hub plus a separate deep tail of mixed depth.";
                    default: return "One heavily guarded exit shared by chains of very different lengths.";
                }
            case CIRCULAR_WAIT:
                switch (levelNumber) {
                    case 1: return "One open loop — see how a wrong request can close it into a cycle.";
                    case 2: return "A longer open loop, plus a free process that pretends to matter.";
                    case 3: return "Two open loops and one distractor — only one true escape path.";
                    case 4: return "Two open loops of different lengths with hidden distractors.";
                    default: return "Three open loops surrounded by free processes — close any of them and you deadlock.";
                }
            case DEADLOCK_ESCAPE:
            default:
                switch (levelNumber) {
                    case 1: return "Begin with a readable chain that combines all four concepts.";
                    case 2: return "A chain plus a contested hand-off — safe ordering avoids the trap.";
                    case 3: return "Contention, an extra-held resource and a side race in one graph.";
                    case 4: return "Three sources feed one escape hub — many safe orders exist, choose wisely.";
                    default: return "OS Chaos: contention, branching chains and a multi-holder hub at once.";
                }
        }
    }
}