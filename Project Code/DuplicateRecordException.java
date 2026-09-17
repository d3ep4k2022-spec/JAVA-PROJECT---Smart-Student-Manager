package com.studentmanager.exception;

/** Raised when an insert would violate the uniqueness of a business key. */
public class DuplicateRecordException extends StudentManagerException {

    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String entity, String id) {
        super(entity + " with id '" + id + "' already exists.");
    }

    @Override
    public String category() {
        return "DUPLICATE";
    }
}
