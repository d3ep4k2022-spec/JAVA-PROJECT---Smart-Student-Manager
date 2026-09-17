package com.studentmanager.repository;

import com.studentmanager.model.Identifiable;
import java.util.List;
import java.util.Optional;

/**
 * Storage contract for any {@link Identifiable} entity.
 *
 * <p>The service layer depends on this interface, never on {@link CsvRepository}.
 * That inversion is what makes the persistence choice replaceable: swapping CSV for
 * a database or an in-memory stub means writing a new implementation of this
 * interface, with no change to any service class.</p>
 *
 * @param <T> the entity type managed by this repository
 */
public interface Repository<T extends Identifiable> {

    /** @return every stored entity, in insertion order. Never {@code null}. */
    List<T> findAll();

    /**
     * @param id business key to look up, matched case-insensitively
     * @return the entity wrapped in an {@link Optional}, empty when absent
     */
    Optional<T> findById(String id);

    /** @return {@code true} when an entity with this id is already stored. */
    boolean existsById(String id);

    /** Appends a new entity and flushes to the backing store. */
    void save(T entity);

    /** Replaces the entity sharing this id and flushes. */
    void update(T entity);

    /**
     * Removes the entity with this id and flushes.
     *
     * @return {@code true} when something was actually removed
     */
    boolean deleteById(String id);

    /** @return the number of stored entities. */
    int count();

    /** Re-reads the backing store, discarding any in-memory cache. */
    void reload();
}
