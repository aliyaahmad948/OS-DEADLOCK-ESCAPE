package model;

/**
 * Resource.java
 *
 * Represents a "Resource" (object) in the Deadlock Escape Game.
 * In OS terms: a Resource is something a Process needs to hold
 * in order to complete its task (like a printer, file, or lock).
 *
 * OS Concept Mapping:
 * Resource = Object/Item in the game
 * allocatedTo = Which process currently holds this resource (null if free)
 * available = Whether this resource is currently free for allocation
 */
public class Resource {

    // Name of the resource (e.g., "R1", "R2")
    private String resourceName;

    // Name of the process that currently holds this resource.
    // If null, the resource is not allocated to anyone.
    private String allocatedTo;

    // Whether this resource is currently available (not held by any process)
    private boolean available;

    /**
     * Constructor - creates a new Resource with a given name.
     * By default, a resource starts as available (not allocated to anyone).
     */
    public Resource(String resourceName) {
        this.resourceName = resourceName;
        this.allocatedTo = null;
        this.available = true;
    }

    // ---------- Getters ----------

    public String getResourceName() {
        return resourceName;
    }

    public String getAllocatedTo() {
        return allocatedTo;
    }

    public boolean isAvailable() {
        return available;
    }

    // ---------- Setters ----------

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Allocates this resource to a given process.
     * Marks it as no longer available.
     */
    public void allocateTo(String processName) {
        this.allocatedTo = processName;
        this.available = false;
    }

    /**
     * Releases this resource, making it available again.
     * Called when a process finishes and gives up its resources.
     */
    public void release() {
        this.allocatedTo = null;
        this.available = true;
    }

    /**
     * Returns a readable summary of this resource's state.
     * Useful for debugging in console tests (Phase 2 checkpoint).
     */
    @Override
    public String toString() {
        return resourceName
                + " | Available: " + available
                + " | AllocatedTo: " + (allocatedTo == null ? "-" : allocatedTo);
    }
}