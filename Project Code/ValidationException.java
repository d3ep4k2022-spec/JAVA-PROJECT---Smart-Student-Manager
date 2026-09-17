package com.studentmanager.exception;

/** Raised when user-supplied data breaks a business rule (blank field, bad email, bad range). */
public class ValidationException extends StudentManagerException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    @Override
    public String category() {
        return "VALIDATION";
    }
}
