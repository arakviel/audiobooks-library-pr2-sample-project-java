package com.arakviel.infrastructure.persistence.exception;

/**
 * Виняток, що виникає при помилках зіставлення сутності з даними бази.
 */
public class EntityMappingException extends RuntimeException {

    public EntityMappingException(String message) {
        super(message);
    }

    public EntityMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}