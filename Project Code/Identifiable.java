package com.studentmanager.model;

/**
 * Contract for any entity that owns a unique business identifier.
 *
 * <p>The generic repository layer is written against this interface rather than
 * against a concrete class, which is what allows a single {@code CsvRepository}
 * implementation to serve students, tasks and attendance records alike.</p>
 */
public interface Identifiable {

    /** @return the unique business key of this entity (never {@code null} or blank). */
    String getId();
}
