package com.arakviel.infrastructure.persistence.exception;

/**
 * Виняток, що виникає при помилках доступу до бази даних.
 */
public class DatabaseAccessException extends RuntimeException {

    public DatabaseAccessException(String message) {
        super(message);
    }

    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}