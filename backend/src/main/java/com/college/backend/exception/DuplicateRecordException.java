package com.college.backend.exception;

/**
 * Exception thrown when an attempt is made to insert a duplicate record
 * violating unique constraints (e.g. unique email, roll number, or user_id).
 */
public class DuplicateRecordException extends DAOException {

    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
