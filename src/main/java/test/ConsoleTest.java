package test;

import logic.GameManager;
import model.Level;

/**
 * ConsoleTest.java
 *
 * TEMPORARY test class — NOT part of the final game.
 * Purpose: manually simulate game scenarios in the console to confirm
 * that DeadlockDetector and GameManager logic work correctly BEFORE
 * we touch any JavaFX/GUI code (Phase 2 checkpoint).
 *
 * Run this file directly (right-click -> Run 'ConsoleTest.main()').
 */
public class ConsoleTest {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("TEST 1: LEVEL 1 (should be a SAFE scenario)");
        System.out.println("========================================");
        testLevel1();

        System.out.println();
        System.out.println("========================================");
        System.out.println("TEST 2: LEVEL 2 (should trigger DEADLOCK)");
        System.out.println("========================================");
        testLevel2Deadlock();
    }

    private static void testLevel1() {
        GameManager gm = new GameManager();
        gm.loadLevel(Level.createLevel1());

        System.out.println("Initial state:");
        printState(gm);

        System.out.println("\n--> P2 finishes (releases R2)");
        System.out.println(gm.completeProcess("P2"));
        printState(gm);

        System.out.println("\n--> P1 requests R2 (now available)");
        System.out.println(gm.allocateResource("P1", "R2"));
        printState(gm);

        System.out.println("\n--> P1 finishes");
        System.out.println(gm.completeProcess("P1"));
        printState(gm);

        System.out.println("\nFinal Game State: " + gm.getState());
        System.out.println("Score: " + gm.getScore());
    }

    private static void testLevel2Deadlock() {
        GameManager gm = new GameManager();
        gm.loadLevel(Level.createLevel2());

        System.out.println("Initial state (allocations already applied by loadLevel):");
        printState(gm);

        System.out.println("\n--> Checking for deadlock in current graph...");
        gm.checkForDeadlock();

        System.out.println("\nFinal Game State: " + gm.getState());
        System.out.println("Explanation:\n" + gm.getLastMessage());

        System.out.println("\n--> Also testing findSafeSequence() (should be EMPTY since it's a deadlock):");
        System.out.println("Safe sequence result: " + gm.findSafeSequence());
    }

    private static void printState(GameManager gm) {
        System.out.println("Processes:");
        for (var p : gm.getProcesses()) {
            System.out.println("  " + p);
        }
        System.out.println("Resources:");
        for (var r : gm.getResources()) {
            System.out.println("  " + r);
        }
        System.out.println("State: " + gm.getState() + " | Score: " + gm.getScore());
    }
}