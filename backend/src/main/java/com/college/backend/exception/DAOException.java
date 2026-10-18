package com.college.backend.exception;

/**
 * Base unchecked exception for data access errors and unexpected database failures.
 */
public class DAOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
