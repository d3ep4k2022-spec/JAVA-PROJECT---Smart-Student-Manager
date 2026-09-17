package com.studentmanager.model;

import java.util.Objects;

/**
 * Abstract base for every human actor stored by the system.
 *
 * <p>This class exists to demonstrate three OOP ideas in one place:</p>
 * <ul>
 *   <li><b>Encapsulation</b> - fields are {@code private}, reachable only through accessors.</li>
 *   <li><b>Inheritance</b> - {@link Student} reuses the identity fields declared here.</li>
 *   <li><b>Polymorphism</b> - {@link #getRole()} is abstract, so each subclass answers
 *       differently while callers depend only on the {@code Person} type.</li>
 * </ul>
 *
 * <p>It is declared {@code abstract} because a bare "person" is never a valid
 * record in this domain; only concrete roles such as a student are.</p>
 */
public abstract class Person implements Identifiable {

    private final String id;
    private String name;
    private String email;

    protected Person(String id, String name, String email) {
        this.id = id == null ? "" : id.trim();
        this.name = name == null ? "" : name.trim();
        this.email = email == null ? "" : email.trim();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? "" : email.trim();
    }

    /**
     * Polymorphic hook. Each subclass reports the role it plays in the system.
     *
     * @return a short uppercase role label, e.g. {@code "STUDENT"}
     */
    public abstract String getRole();

    /**
     * Polymorphic hook used by the analytics report so it can describe any
     * {@code Person} subtype without a chain of {@code instanceof} checks.
     *
     * @return a one-line human-readable summary
     */
    public abstract String describe();

    /** Identity is defined purely by the business key, as it is in the CSV files. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Person person)) {
            return false;
        }
        return id.equalsIgnoreCase(person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase());
    }
}
