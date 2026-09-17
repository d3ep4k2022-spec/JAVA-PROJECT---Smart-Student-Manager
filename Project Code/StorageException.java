package com.studentmanager.exception;

/**
 * Raised when the CSV layer cannot read or write.
 *
 * <p>The underlying {@link java.io.IOException} is always passed as the cause so the
 * original stack trace survives the translation from a checked to an unchecked type.</p>
 */
public class StorageException extends StudentManagerException {

    private static final long serialVersionUID = 1L;

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String category() {
        return "STORAGE";
    }
}
