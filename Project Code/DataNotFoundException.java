package com.studentmanager.exception;

/** Raised when an operation targets an id that does not exist in storage. */
public class DataNotFoundException extends StudentManagerException {

    private static final long serialVersionUID = 1L;

    public DataNotFoundException(String entity, String id) {
        super(entity + " with id '" + id + "' was not found.");
    }

    public DataNotFoundException(String message) {
        super(message);
    }

    @Override
    public String category() {
        return "NOT FOUND";
    }
}
