package com.studentmanager.service;

import com.studentmanager.exception.DataNotFoundException;
import com.studentmanager.exception.DuplicateRecordException;
import com.studentmanager.model.CsvSerializable;
import com.studentmanager.model.Identifiable;
import com.studentmanager.repository.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Generic base implementing the CRUD behaviour shared by every service.
 *
 * <p>This is a <b>template method</b> design: {@link #add(Identifiable)} fixes the
 * algorithm — validate, check for duplicates, persist — while the two abstract hooks
 * {@link #validate(Identifiable)} and {@link #entityName()} let each subclass supply
 * the parts that differ. Without this class, the duplicate check and the
 * not-found lookup would be copy-pasted three times.</p>
 *
 * @param <T> entity type handled by the concrete subclass
 */
public abstract class AbstractCrudService<T extends Identifiable & CsvSerializable> {

    /** Protected, not private, so subclasses can run entity-specific queries. */
    protected final Repository<T> repository;

    protected AbstractCrudService(Repository<T> repository) {
        this.repository = repository;
    }

    /** Hook: throw a {@code ValidationException} when the entity breaks a business rule. */
    protected abstract void validate(T entity);

    /** Hook: the human-readable entity name used in error messages, e.g. {@code "Student"}. */
    protected abstract String entityName();

    /** Hook: the id prefix used when auto-generating keys, e.g. {@code "S"}. */
    protected abstract String idPrefix();

    public List<T> findAll() {
        return repository.findAll();
    }

    public Optional<T> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Looks up an entity or fails loudly.
     *
     * @throws DataNotFoundException when no entity carries this id
     */
    public T require(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(entityName(), id));
    }

    /**
     * Template method: validates, rejects duplicates, then persists.
     *
     * @throws com.studentmanager.exception.ValidationException if a rule is broken
     * @throws DuplicateRecordException if the id is already taken
     */
    public void add(T entity) {
        validate(entity);
        if (repository.existsById(entity.getId())) {
            throw new DuplicateRecordException(entityName(), entity.getId());
        }
        repository.save(entity);
    }

    /** Validates then overwrites the stored entity carrying the same id. */
    public void update(T entity) {
        validate(entity);
        require(entity.getId());
        repository.update(entity);
    }

    /**
     * Removes an entity.
     *
     * @throws DataNotFoundException when the id is unknown, so the caller never
     *         gets a silent no-op on a typo
     */
    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new DataNotFoundException(entityName(), id);
        }
    }

    public int count() {
        return repository.count();
    }

    /**
     * Produces the next free sequential id, for example {@code S001} after {@code S007}
     * has been deleted but {@code S001..S006} remain — the scan takes the highest
     * numeric suffix currently in use and adds one, so ids are never recycled.
     *
     * @return a fresh id such as {@code "S008"}
     */
    public String nextId() {
        int highest = 0;
        for (T entity : repository.findAll()) {
            String id = entity.getId();
            if (id.toUpperCase().startsWith(idPrefix().toUpperCase())) {
                try {
                    highest = Math.max(highest,
                            Integer.parseInt(id.substring(idPrefix().length())));
                } catch (NumberFormatException ignored) {
                    // Manually entered ids that are not numeric simply do not affect the counter.
                }
            }
        }
        return String.format("%s%03d", idPrefix(), highest + 1);
    }
}
