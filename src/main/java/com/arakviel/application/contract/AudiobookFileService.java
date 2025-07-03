package com.arakviel.application.contract;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.file.exception.FileStorageException;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Інтерфейс для управління файлами аудіокниг.
 */
public interface AudiobookFileService {

    /**
     * Створює новий файл аудіокниги та завантажує його.
     *
     * @param audiobookFile файл аудіокниги для створення
     * @param inputStream   потік даних файлу
     * @param fileName      ім'я файлу
     * @return створений файл аудіокниги
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    AudiobookFile create(AudiobookFile audiobookFile, InputStream inputStream, String fileName);

    /**
     * Оновлює існуючий файл аудіокниги.
     *
     * @param id            ідентифікатор файлу для оновлення
     * @param audiobookFile оновлені дані файлу
     * @param inputStream   потік даних нового файлу, може бути null
     * @param fileName      ім'я нового файлу, може бути null
     * @return оновлений файл аудіокниги
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    AudiobookFile update(UUID id, AudiobookFile audiobookFile, InputStream inputStream, String fileName);

    /**
     * Видаляє файл аудіокниги та пов'язаний фізичний файл.
     *
     * @param id ідентифікатор файлу для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    void delete(UUID id);

    /**
     * Знаходить файл аудіокниги за ідентифікатором.
     *
     * @param id ідентифікатор файлу
     * @return Optional з файлом аудіокниги, якщо знайдено
     */
    Optional<AudiobookFile> findById(UUID id);

    /**
     * Знаходить всі файли аудіокниг з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список файлів аудіокниг
     */
    List<AudiobookFile> findAll(int offset, int limit);

    /**
     * Знаходить файли за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список файлів аудіокниги
     */
    List<AudiobookFile> findByAudiobookId(UUID audiobookId);

    /**
     * Знаходить файли за форматом.
     *
     * @param format формат файлу
     * @return список файлів з вказаним форматом
     */
    List<AudiobookFile> findByFormat(FileFormat format);

    /**
     * Знаходить файли аудіокниги за форматом.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param format      формат файлу
     * @return список файлів аудіокниги з вказаним форматом
     */
    List<AudiobookFile> findByAudiobookIdAndFormat(UUID audiobookId, FileFormat format);

    /**
     * Підраховує кількість файлів для аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return кількість файлів
     */
    long countByAudiobookId(UUID audiobookId);

    /**
     * Підраховує кількість файлів за форматом.
     *
     * @param format формат файлу
     * @return кількість файлів
     */
    long countByFormat(FileFormat format);

    /**
     * Обчислює загальний розмір файлів аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return загальний розмір у байтах
     */
    long calculateTotalSizeByAudiobookId(UUID audiobookId);

    /**
     * Знаходить найбільший файл аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return Optional з найбільшим файлом, якщо знайдено
     */
    Optional<AudiobookFile> findLargestFileByAudiobookId(UUID audiobookId);

    /**
     * Знаходить найменший файл аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return Optional з найменшим файлом, якщо знайдено
     */
    Optional<AudiobookFile> findSmallestFileByAudiobookId(UUID audiobookId);

    /**
     * Перевіряє, чи існує файл з таким шляхом для аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param filePath    шлях до файлу
     * @return true, якщо файл існує
     */
    boolean existsByAudiobookIdAndFilePath(UUID audiobookId, String filePath);

    /**
     * Видаляє всі файли аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    void deleteAllByAudiobookId(UUID audiobookId);

    /**
     * Копіює файл аудіокниги.
     *
     * @param sourceFileId      ідентифікатор вихідного файлу
     * @param targetAudiobookId ідентифікатор цільової аудіокниги
     * @param newFileName       нове ім'я файлу
     * @return скопійований файл аудіокниги
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    AudiobookFile copyFile(UUID sourceFileId, UUID targetAudiobookId, String newFileName);

    /**
     * Змінює формат файлу аудіокниги.
     *
     * @param fileId    ідентифікатор файлу
     * @param newFormat новий формат файлу
     * @return оновлений файл аудіокниги
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    AudiobookFile changeFormat(UUID fileId, FileFormat newFormat);

    /**
     * Отримує статистику файлів за форматами.
     *
     * @return мапа з кількістю файлів для кожного формату
     */
    java.util.Map<FileFormat, Long> getFormatStatistics();

    /**
     * Знаходить файли аудіокниги, відсортовані за розміром.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param ascending   true для сортування за зростанням, false за спаданням
     * @return список файлів, відсортованих за розміром
     */
    List<AudiobookFile> findByAudiobookIdOrderBySize(UUID audiobookId, boolean ascending);

    /**
     * Знаходить дублікати файлів (файли з однаковим розміром та форматом).
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список потенційних дублікатів
     */
    List<AudiobookFile> findPotentialDuplicates(UUID audiobookId);
}
