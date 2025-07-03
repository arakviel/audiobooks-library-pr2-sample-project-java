package com.arakviel.application.contract;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Genre;
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

    /**
     * Знаходить аудіокниги за назвою.
     *
     * @param title назва аудіокниги
     * @return список аудіокниг
     */
    List<Audiobook> findByTitle(String title);

    /**
     * Знаходить аудіокниги за частковою відповідністю назви.
     *
     * @param partialTitle часткова назва аудіокниги
     * @return список аудіокниг
     */
    List<Audiobook> findByPartialTitle(String partialTitle);

    /**
     * Знаходить аудіокниги за автором.
     *
     * @param authorId ідентифікатор автора
     * @return список аудіокниг
     */
    List<Audiobook> findByAuthorId(UUID authorId);

    /**
     * Знаходить аудіокниги за жанром.
     *
     * @param genreId ідентифікатор жанру
     * @return список аудіокниг
     */
    List<Audiobook> findByGenreId(UUID genreId);

    /**
     * Знаходить аудіокниги за роком публікації.
     *
     * @param year рік публікації
     * @return список аудіокниг
     */
    List<Audiobook> findByPublicationYear(int year);

    /**
     * Знаходить аудіокниги за діапазоном тривалості.
     *
     * @param minDuration мінімальна тривалість у секундах
     * @param maxDuration максимальна тривалість у секундах
     * @return список аудіокниг
     */
    List<Audiobook> findByDurationRange(int minDuration, int maxDuration);

    /**
     * Знаходить найпопулярніші аудіокниги (за кількістю прослуховувань).
     *
     * @param limit кількість записів для отримання
     * @return список популярних аудіокниг
     */
    List<Audiobook> findMostPopular(int limit);

    /**
     * Знаходить нещодавно додані аудіокниги.
     *
     * @param limit кількість записів для отримання
     * @return список нещодавно доданих аудіокниг
     */
    List<Audiobook> findRecentlyAdded(int limit);

    /**
     * Підраховує загальну кількість аудіокниг.
     *
     * @return загальна кількість аудіокниг
     */
    long count();

    /**
     * Підраховує кількість аудіокниг за автором.
     *
     * @param authorId ідентифікатор автора
     * @return кількість аудіокниг
     */
    long countByAuthorId(UUID authorId);

    /**
     * Підраховує кількість аудіокниг за жанром.
     *
     * @param genreId ідентифікатор жанру
     * @return кількість аудіокниг
     */
    long countByGenreId(UUID genreId);

    /**
     * Обчислює загальну тривалість всіх аудіокниг.
     *
     * @return загальна тривалість у секундах
     */
    long calculateTotalDuration();

    /**
     * Обчислює середню тривалість аудіокниг.
     *
     * @return середня тривалість у секундах
     */
    double calculateAverageDuration();

    /**
     * Знаходить найдовшу аудіокнигу.
     *
     * @return Optional з найдовшою аудіокнигою, якщо знайдено
     */
    Optional<Audiobook> findLongest();

    /**
     * Знаходить найкоротшу аудіокнигу.
     *
     * @return Optional з найкоротшою аудіокнигою, якщо знайдено
     */
    Optional<Audiobook> findShortest();

    /**
     * Перевіряє, чи існує аудіокнига з такою назвою та автором.
     *
     * @param title    назва аудіокниги
     * @param authorId ідентифікатор автора
     * @return true, якщо аудіокнига існує
     */
    boolean existsByTitleAndAuthorId(String title, UUID authorId);

    /**
     * Знаходить схожі аудіокниги за жанром та автором.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param limit       кількість записів для отримання
     * @return список схожих аудіокниг
     */
    List<Audiobook> findSimilar(UUID audiobookId, int limit);

    /**
     * Отримує статистику аудіокниг за жанрами.
     *
     * @return мапа з кількістю аудіокниг для кожного жанру
     */
    java.util.Map<Genre, Long> getGenreStatistics();

    /**
     * Отримує статистику аудіокниг за авторами.
     *
     * @return мапа з кількістю аудіокниг для кожного автора
     */
    java.util.Map<Author, Long> getAuthorStatistics();
}