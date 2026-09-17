package com.studentmanager.exception;

/**
 * Root of the application's own exception hierarchy.
 *
 * <p>A common base lets the presentation layer catch every expected,
 * business-level failure in one {@code catch} block while still allowing
 * individual handlers where a specific reaction is needed. It extends
 * {@link RuntimeException} because these failures are caused by user input
 * rather than by recoverable environmental conditions, so forcing every call
 * site to declare them would add noise without adding safety.</p>
 */
public abstract class StudentManagerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected StudentManagerException(String message) {
        super(message);
    }

    protected StudentManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /** @return a short label shown before the message in the console, e.g. {@code "VALIDATION"}. */
    public abstract String category();
}
