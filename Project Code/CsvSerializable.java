package com.studentmanager.model;

/**
 * Contract for entities that can flatten themselves into a single CSV line.
 *
 * <p>Deserialisation is deliberately NOT part of this interface: Java does not
 * allow abstract static methods, so each model exposes its own static
 * {@code fromCsv(String)} factory and hands it to the repository as a
 * {@link java.util.function.Function} method reference.</p>
 */
public interface CsvSerializable {

    /** @return this entity encoded as one comma-separated line, without a trailing newline. */
    String toCsv();

    /**
     * Removes characters that would corrupt the flat-file format.
     * Declared {@code static} so every implementation shares one sanitiser.
     *
     * @param value raw field value, may be {@code null}
     * @return a comma-free, newline-free representation
     */
    static String sanitise(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ").replace("\n", " ").replace("\r", " ").trim();
    }
}
