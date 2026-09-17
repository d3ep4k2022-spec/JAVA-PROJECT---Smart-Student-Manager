package com.studentmanager.model;

/**
 * Importance of a task. Enums are used instead of {@code String} constants so that
 * an invalid priority is a compile-time error rather than a runtime surprise.
 *
 * <p>The {@code weight} field feeds the analytics urgency score.</p>
 */
public enum Priority {

    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High");

    private final int weight;
    private final String label;

    Priority(int weight, String label) {
        this.weight = weight;
        this.label = label;
    }

    public int getWeight() {
        return weight;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Case-insensitive lookup that also accepts the ordinal shown in the console menu.
     *
     * @param raw user input such as {@code "high"}, {@code "HIGH"} or {@code "3"}
     * @return the matching constant, or {@link #MEDIUM} when the input is blank
     * @throws IllegalArgumentException if the value matches no constant
     */
    public static Priority parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return MEDIUM;
        }
        String value = raw.trim();
        for (Priority priority : values()) {
            if (priority.name().equalsIgnoreCase(value)
                    || String.valueOf(priority.weight).equals(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + raw);
    }
}
