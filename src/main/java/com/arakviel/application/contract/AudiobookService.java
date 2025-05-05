package com.arakviel.application.contract;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.file.exception.FileStorageException;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Інтерфейс для управління сутностями аудіокниг, включаючи операції з файлами обкладинок та аудіофайлами.
 */
public interface AudiobookService {

    /**
     * Створює нову аудіокнигу та, за потреби, завантажує обкладинку.
     *
     * @param audiobook      аудіокнига для створення
     * @param coverImage     потік даних обкладинки, може бути null
     * @param coverImageName ім'я файлу обкладинки, може бути null
     * @return створена аудіокнига
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    Audiobook create(Audiobook audiobook, InputStream coverImage, String coverImageName);

    /**
     * Оновлює існуючу аудіокнигу та, за потреби, оновлює обкладинку.
     *
     * @param id             ідентифікатор аудіокниги для оновлення
     * @param audiobook      оновлені дані аудіокниги
     * @param coverImage     потік даних нової обкладинки, може бути null
     * @param coverImageName ім'я файлу нової обкладинки, може бути null
     * @return оновлена аудіокнига
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    Audiobook update(UUID id, Audiobook audiobook, InputStream coverImage, String coverImageName);

    /**
     * Видаляє аудіокнигу та всі пов'язані файли.
     *
     * @param id ідентифікатор аудіокниги для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    void delete(UUID id);

    /**
     * Завантажує аудіофайл для певної аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param inputStream потік даних аудіофайлу
     * @param fileName    ім'я аудіофайлу
     * @param format      формат файлу
     * @param size        розмір файлу в байтах
     * @return створений аудіофайл
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    AudiobookFile uploadAudiobookFile(UUID audiobookId, InputStream inputStream, String fileName, FileFormat format, int size);

    /**
     * Знаходить аудіокнигу за ідентифікатором.
     *
     * @param id ідентифікатор аудіокниги
     * @return Optional з аудіокнигою, якщо знайдено
     */
    Optional<Audiobook> findById(UUID id);

    /**
     * Знаходить всі аудіокниги з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список аудіокниг
     */
    List<Audiobook> findAll(int offset, int limit);

    /**
     * Знаходить всі файли, пов'язані з аудіокнигою.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список аудіофайлів
     */
    List<AudiobookFile> findFilesByAudiobookId(UUID audiobookId);

    /**
     * Видаляє певний аудіофайл аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param fileId      ідентифікатор файлу для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    void deleteAudiobookFile(UUID audiobookId, UUID fileId);
}