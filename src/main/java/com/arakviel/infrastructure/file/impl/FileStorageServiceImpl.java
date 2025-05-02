package com.arakviel.infrastructure.file.impl;

import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.file.exception.FileStorageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Реалізація сервісу для роботи зі зберіганням файлів, зокрема зображень.
 */
public class FileStorageServiceImpl implements FileStorageService {

    private final Path storageRoot;
    private final Set<String> allowedExtensions;
    private final long maxFileSize;

    /**
     * Конструктор із конфігурацією кореневої директорії та дозволених розширень.
     *
     * @param storageRootPath   шлях до кореневої директорії для зберігання файлів
     * @param allowedExtensions набір дозволених розширень файлів (наприклад, "jpg", "png")
     * @param maxFileSize       максимальний розмір файлу у байтах
     */
    public FileStorageServiceImpl(String storageRootPath, String[] allowedExtensions, long maxFileSize) {
        this.storageRoot = Paths.get(storageRootPath).toAbsolutePath().normalize();
        this.allowedExtensions = new HashSet<>(Arrays.asList(allowedExtensions));
        this.maxFileSize = maxFileSize;
        initializeStorage();
    }

    /**
     * Ініціалізація директорії для зберігання файлів.
     */
    private void initializeStorage() {
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new FileStorageException("Не вдалося ініціалізувати директорію для зберігання файлів: " + storageRoot, e);
        }
    }

    /**
     * Зберігає файл у вказаній директорії та повертає шлях до нього.
     *
     * @param inputStream потік даних файлу
     * @param fileName    ім’я файлу (з розширенням)
     * @param entityId    ідентифікатор сутності (наприклад, аудіокниги)
     * @return шлях до збереженого файлу
     * @throws FileStorageException якщо сталася помилка під час збереження
     */
    @Override
    public Path save(InputStream inputStream, String fileName, UUID entityId) {
        validateFileName(fileName);
        Path entityDir = createEntityDirectory(entityId);
        Path filePath = entityDir.resolve(fileName);

        try {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        } catch (IOException e) {
            throw new FileStorageException("Помилка збереження файлу: " + filePath, e);
        }
    }

    /**
     * Отримує шлях до файлу за ідентифікатором сутності та ім’ям файлу.
     *
     * @param fileName ім’я файлу
     * @param entityId ідентифікатор сутності
     * @return шлях до файлу
     * @throws FileStorageException якщо файл не знайдено
     */
    @Override
    public Path getFilePath(String fileName, UUID entityId) {
        validateFileName(fileName);
        Path filePath = storageRoot.resolve(entityId.toString()).resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new FileStorageException("Файл не знайдено: " + filePath);
        }

        return filePath;
    }

    /**
     * Видаляє файл за ідентифікатором сутності та ім’ям файлу.
     *
     * @param fileName ім’я файлу
     * @param entityId ідентифікатор сутності
     * @throws FileStorageException якщо сталася помилка під час видалення
     */
    @Override
    public void delete(String fileName, UUID entityId) {
        validateFileName(fileName);
        Path filePath = storageRoot.resolve(entityId.toString()).resolve(fileName);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new FileStorageException("Помилка видалення файлу: " + filePath, e);
        }
    }

    /**
     * Перевіряє, чи існує файл.
     *
     * @param fileName ім’я файлу
     * @param entityId ідентифікатор сутності
     * @return true, якщо файл існує
     */
    @Override
    public boolean exists(String fileName, UUID entityId) {
        validateFileName(fileName);
        Path filePath = storageRoot.resolve(entityId.toString()).resolve(fileName);
        return Files.exists(filePath);
    }

    /**
     * Створює директорію для сутності, якщо вона ще не існує.
     *
     * @param entityId ідентифікатор сутності
     * @return шлях до директорії
     */
    private Path createEntityDirectory(UUID entityId) {
        Path entityDir = storageRoot.resolve(entityId.toString());
        try {
            Files.createDirectories(entityDir);
            return entityDir;
        } catch (IOException e) {
            throw new FileStorageException("Помилка створення директорії для сутності: " + entityDir, e);
        }
    }

    /**
     * Валідує ім’я файлу, перевіряючи його розширення та коректність.
     *
     * @param fileName ім’я файлу
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new FileStorageException("Ім’я файлу не може бути порожнім");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new FileStorageException("Непідтримуваний формат файлу: " + extension);
        }

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new FileStorageException("Некоректне ім’я файлу: " + fileName);
        }
    }

    /**
     * Валідує розмір файлу, перевіряючи, чи не перевищує він максимальний ліміт.
     *
     * @param inputStream потік даних файлу
     * @throws FileStorageException якщо розмір файлу перевищує ліміт
     */
    private void validateFileSize(InputStream inputStream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int bytesRead;
            long totalSize = 0;

            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                totalSize += bytesRead;
                if (totalSize > maxFileSize) {
                    throw new FileStorageException("Розмір файлу перевищує 5 МБ");
                }
                buffer.write(data, 0, bytesRead);
            }

            // Відновлюємо InputStream для подальшого використання
            inputStream.close();
            inputStream = new java.io.ByteArrayInputStream(buffer.toByteArray());
        } catch (IOException e) {
            throw new FileStorageException("Помилка перевірки розміру файлу", e);
        }
    }

    /**
     * Отримує розширення файлу.
     *
     * @param fileName ім’я файлу
     * @return розширення файлу (без крапки)
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}