package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Mission.java
 *
 * Defines the mission/objective for a single level.
 * Every level has a primary mission and optional bonus objectives.
 *
 * Example usage:
 *   Mission m = new Mission("Complete all processes without creating a deadlock.");
 *   m.addBonusObjective("Finish within 60 seconds");
 *   m.addBonusObjective("Use fewer than 5 actions");
 *   m.setTargetTimeSeconds(60);
 *   m.setMaxActions(5);
 */
public class Mission {

    private String description;
    private List<String> bonusObjectives;
    private int targetTimeSeconds;
    private int maxActions;

    public Mission(String description) {
        this.description = description;
        this.bonusObjectives = new ArrayList<>();
        this.targetTimeSeconds = -1;
        this.maxActions = -1;
    }

    public void addBonusObjective(String objective) {
        bonusObjectives.add(objective);
    }

    // ---------- Getters ----------

    public String getDescription() {
        return description;
    }

    public List<String> getBonusObjectives() {
        return bonusObjectives;
    }

    public int getTargetTimeSeconds() {
        return targetTimeSeconds;
    }

    public int getMaxActions() {
        return maxActions;
    }

    // ---------- Setters ----------

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTargetTimeSeconds(int targetTimeSeconds) {
        this.targetTimeSeconds = targetTimeSeconds;
    }

    public void setMaxActions(int maxActions) {
        this.maxActions = maxActions;
    }
}
