package com.studentmanager.model;

/**
 * Lifecycle state of a task.
 *
 * <p>Modelling this as an enum rather than a {@code boolean completed} flag lets the
 * system distinguish work that has been started from work that has not been touched,
 * which the analytics module reports separately.</p>
 */
public enum TaskStatus {

    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** @return {@code true} when no further work is expected on the task. */
    public boolean isClosed() {
        return this == COMPLETED;
    }

    /**
     * Case-insensitive lookup that also accepts the menu number 1-3.
     *
     * @throws IllegalArgumentException if the value matches no constant
     */
    public static TaskStatus parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return PENDING;
        }
        String value = raw.trim();
        TaskStatus[] all = values();
        for (int i = 0; i < all.length; i++) {
            if (all[i].name().equalsIgnoreCase(value) || String.valueOf(i + 1).equals(value)) {
                return all[i];
            }
        }
        throw new IllegalArgumentException("Unknown status: " + raw);
    }
}
