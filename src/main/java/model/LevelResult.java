package model;

import java.util.ArrayList;
import java.util.List;

/**
 * LevelResult.java
 *
 * Captures the full statistics of a completed level.
 * Used by the Results screen to display score, stars, time, mistakes,
 * efficiency, and educational statistics.
 */
public class LevelResult {

    private boolean won;
    private int score;
    private int stars;
    private int timeUsedSeconds;
    private int mistakes;
    private double efficiencyPercent;
    private int processesCompleted;
    private int totalProcesses;
    private int deadlocksDetected;
    private int deadlocksPrevented;
    private int recoveryActions;
    private int hintsUsed;
    private String conceptLearned = "";
    private String levelName = "";
    private int levelNumber;
    private boolean usedSafeSequence;
    private boolean completedBonusObjective;

    public LevelResult() {
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getTimeUsedSeconds() {
        return timeUsedSeconds;
    }

    public void setTimeUsedSeconds(int timeUsedSeconds) {
        this.timeUsedSeconds = timeUsedSeconds;
    }

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }

    public double getEfficiencyPercent() {
        return efficiencyPercent;
    }

    public void setEfficiencyPercent(double efficiencyPercent) {
        this.efficiencyPercent = efficiencyPercent;
    }

    public int getProcessesCompleted() {
        return processesCompleted;
    }

    public void setProcessesCompleted(int processesCompleted) {
        this.processesCompleted = processesCompleted;
    }

    public int getTotalProcesses() {
        return totalProcesses;
    }

    public void setTotalProcesses(int totalProcesses) {
        this.totalProcesses = totalProcesses;
    }

    public int getDeadlocksDetected() {
        return deadlocksDetected;
    }

    public void setDeadlocksDetected(int deadlocksDetected) {
        this.deadlocksDetected = deadlocksDetected;
    }

    public int getDeadlocksPrevented() {
        return deadlocksPrevented;
    }

    public void setDeadlocksPrevented(int deadlocksPrevented) {
        this.deadlocksPrevented = deadlocksPrevented;
    }

    public int getRecoveryActions() {
        return recoveryActions;
    }

    public void setRecoveryActions(int recoveryActions) {
        this.recoveryActions = recoveryActions;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(int hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public String getConceptLearned() {
        return conceptLearned;
    }

    public void setConceptLearned(String conceptLearned) {
        this.conceptLearned = conceptLearned;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public boolean isUsedSafeSequence() {
        return usedSafeSequence;
    }

    public void setUsedSafeSequence(boolean usedSafeSequence) {
        this.usedSafeSequence = usedSafeSequence;
    }

    public boolean isCompletedBonusObjective() {
        return completedBonusObjective;
    }

    public void setCompletedBonusObjective(boolean completedBonusObjective) {
        this.completedBonusObjective = completedBonusObjective;
    }
}