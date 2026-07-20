package logic;

import model.Level;
import model.Process;
import model.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * GameManager.java
 *
 * Central controller for gameplay logic. (Logic completely unchanged from
 * original — only addition: a getter exposing the internal DeadlockDetector,
 * so the graph visualization can read which exact nodes were part of the
 * last detected cycle.)
 */
public class GameManager {

    public enum GameState {
        PLAYING,
        WON,
        LOST_DEADLOCK,
        LOST_TIMEOUT
    }

    private Level currentLevel;
    private List<Process> processes;
    private List<Resource> resources;
    private DeadlockDetector detector;

    private int score;
    private int timeRemainingSeconds;
    private GameState state;
    private String lastMessage;

    private static final int POINTS_PER_ALLOCATION = 10;
    private static final int POINTS_PER_PROCESS_FINISHED = 25;
    private static final int POINTS_BONUS_ON_WIN = 50;

    public GameManager() {
        this.processes = new ArrayList<>();
        this.resources = new ArrayList<>();
        this.detector = new DeadlockDetector();
        this.score = 0;
        this.timeRemainingSeconds = 0;
        this.state = GameState.PLAYING;
        this.lastMessage = "";
    }

    public void loadLevel(Level level) {
        this.currentLevel = level;
        this.processes = new ArrayList<>();
        this.resources = new ArrayList<>();
        this.score = 0;
        this.state = GameState.PLAYING;
        this.lastMessage = "";
        this.timeRemainingSeconds = level.getTimeLimitSeconds();

        for (String pName : level.getProcessNames()) {
            processes.add(new Process(pName));
        }

        for (String rName : level.getResourceNames()) {
            resources.add(new Resource(rName));
        }

        for (String[] allocation : level.getInitialAllocations()) {
            String processName = allocation[0];
            String resourceName = allocation[1];

            Process p = findProcess(processName);
            Resource r = findResource(resourceName);
            if (p != null && r != null) {
                r.allocateTo(processName);
                p.addResource(resourceName);
            }
        }

        for (String[] request : level.getInitialRequests()) {
            String processName = request[0];
            String resourceName = request[1];

            Process p = findProcess(processName);
            if (p != null) {
                p.setWaitingFor(resourceName);
            }
        }
    }

    public String allocateResource(String processName, String resourceName) {
        if (state != GameState.PLAYING) {
            return "Game is already over.";
        }

        Process p = findProcess(processName);
        Resource r = findResource(resourceName);

        if (p == null || r == null) {
            return "Invalid process or resource.";
        }

        if (p.isFinished()) {
            return processName + " has already finished and cannot request resources.";
        }

        if (r.isAvailable()) {
            r.allocateTo(processName);
            p.addResource(resourceName);
            score += POINTS_PER_ALLOCATION;
            lastMessage = processName + " was granted " + resourceName + ".";
        } else {
            p.setWaitingFor(resourceName);
            lastMessage = processName + " is now waiting for " + resourceName
                    + " (held by " + r.getAllocatedTo() + ").";

            checkForDeadlock();
        }

        return lastMessage;
    }

    public String completeProcess(String processName) {
        if (state != GameState.PLAYING) {
            return "Game is already over.";
        }

        Process p = findProcess(processName);
        if (p == null) {
            return "Invalid process.";
        }

        if (p.isWaiting()) {
            return processName + " cannot finish while still waiting for " + p.getWaitingFor() + ".";
        }

        List<String> held = new ArrayList<>(p.getHeldResources());
        for (String resourceName : held) {
            Resource r = findResource(resourceName);
            if (r != null) {
                r.release();
            }
            p.removeResource(resourceName);
        }

        p.setFinished(true);
        score += POINTS_PER_PROCESS_FINISHED;
        lastMessage = processName + " finished and released all resources.";

        checkWinCondition();

        return lastMessage;
    }

    public void checkForDeadlock() {
        detector.buildGraph(processes, resources);
        boolean deadlock = detector.checkDeadlock();

        if (deadlock) {
            state = GameState.LOST_DEADLOCK;
            lastMessage = detector.getExplanation();
        }
    }

    public void checkWinCondition() {
        boolean allFinished = true;
        for (Process p : processes) {
            if (!p.isFinished()) {
                allFinished = false;
                break;
            }
        }

        if (allFinished) {
            state = GameState.WON;
            score += POINTS_BONUS_ON_WIN;
            lastMessage = "Mission Complete - Safe Sequence Found!";
        }
    }

    public List<String> findSafeSequence() {
        List<String> finished = new ArrayList<>();
        List<String> availableResources = new ArrayList<>();
        for (Resource r : resources) {
            if (r.isAvailable()) {
                availableResources.add(r.getResourceName());
            }
        }

        List<Process> remaining = new ArrayList<>();
        for (Process p : processes) {
            if (!p.isFinished()) {
                remaining.add(p);
            }
        }

        boolean progressMade = true;
        while (progressMade && !remaining.isEmpty()) {
            progressMade = false;

            for (int i = 0; i < remaining.size(); i++) {
                Process p = remaining.get(i);

                boolean canFinish = !p.isWaiting() || availableResources.contains(p.getWaitingFor());

                if (canFinish) {
                    finished.add(p.getProcessName());
                    availableResources.addAll(p.getHeldResources());
                    remaining.remove(i);
                    progressMade = true;
                    break;
                }
            }
        }

        if (!remaining.isEmpty()) {
            return new ArrayList<>();
        }

        return finished;
    }

    public void tick() {
        if (state != GameState.PLAYING) {
            return;
        }

        timeRemainingSeconds--;
        if (timeRemainingSeconds <= 0) {
            timeRemainingSeconds = 0;
            state = GameState.LOST_TIMEOUT;
            lastMessage = "Time's up! The processes could not escape in time.";
        }
    }

    private Process findProcess(String processName) {
        for (Process p : processes) {
            if (p.getProcessName().equals(processName)) {
                return p;
            }
        }
        return null;
    }

    private Resource findResource(String resourceName) {
        for (Resource r : resources) {
            if (r.getResourceName().equals(resourceName)) {
                return r;
            }
        }
        return null;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public int getScore() {
        return score;
    }

    public int getTimeRemainingSeconds() {
        return timeRemainingSeconds;
    }

    public GameState getState() {
        return state;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * NEW METHOD: exposes the internal DeadlockDetector so the UI's graph
     * visualization can read exactly which nodes were part of the last
     * detected deadlock cycle (via detector.getLastCycleNodes()).
     */
    public DeadlockDetector getDetector() {
        return detector;
    }
}