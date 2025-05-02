package com.arakviel.infrastructure.file;

import com.arakviel.infrastructure.file.exception.FileStorageException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Інтерфейс для роботи зі зберіганням файлів.
 */
public interface FileStorageService {

    /**
     * Зберігає файл у вказаній директорії та повертає шлях до нього.
     *
     * @param inputStream потік даних файлу
     * @param fileName    ім’я файлу (з розширенням)
     * @param entityId    ідентифікатор сутності (наприклад, аудіокниги)
     * @return шлях до збереженого файлу
     * @throws FileStorageException якщо сталася помилка під час збереження
     */
    Path save(InputStream inputStream, String fileName, UUID entityId);

    /**
     * Отримує шлях до файлу за ідентифікатором сутності та ім’ям файлу.
     *
     * @param fileName ім’я файлу
     * @param entityId ідентифікатор сутності
     * @return шлях до файлу
     * @throws FileStorageException якщо файл не знайдено
     */
    Path getFilePath(String fileName, UUID entityId);

    /**
     * Видаляє файл за ідентифікатором сутності та ім’ям файлу.
     *
     * @param fileName ім’я файлу
     * @param entityId ідентифікатор сутності
     * @throws FileStorageException якщо сталася помилка під час видалення
     */
    void delete(String fileName, UUID entityId);

    /**
     * Перевіряє, чи існує файл.
     *
     * @param fileName ім’я файлу
     * @param entityId ідентифікатор сутності
     * @return true, якщо файл існує
     */
    boolean exists(String fileName, UUID entityId);
}