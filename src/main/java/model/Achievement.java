package model;

/**
 * Achievement.java
 *
 * Describes a single unlockable achievement for the player.
 */
public class Achievement {

    private final String name;
    private final String description;

    public Achievement(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " - " + description;
    }
}