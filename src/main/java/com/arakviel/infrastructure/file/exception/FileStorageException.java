package com.arakviel.infrastructure.file.exception;

/**
 * Виняток для обробки помилок під час роботи зі зберіганням файлів.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}