package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Інтерфейс репозиторію для специфічних операцій з файлами аудіокниг.
 */
public interface AudiobookFileRepository extends Repository<AudiobookFile, UUID> {

    /**
     * Пошук файлів аудіокниги за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список файлів аудіокниги
     */
    List<AudiobookFile> findByAudiobookId(UUID audiobookId);

    /**
     * Пошук файлів аудіокниги за форматом.
     *
     * @param format формат файлу
     * @return список файлів аудіокниги
     */
    List<AudiobookFile> findByFormat(FileFormat format);

    /**
     * Підрахунок файлів для аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return кількість файлів
     */
    long countByAudiobookId(UUID audiobookId);

    /**
     * Пошук файлів за діапазоном розміру.
     *
     * @param minSize мінімальний розмір (у байтах)
     * @param maxSize максимальний розмір (у байтах)
     * @return список файлів
     */
    List<AudiobookFile> findBySizeRange(int minSize, int maxSize);

    /**
     * Знаходить файли аудіокниги за форматом.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param format      формат файлу
     * @return список файлів аудіокниги з вказаним форматом
     */
    List<AudiobookFile> findByAudiobookIdAndFormat(UUID audiobookId, FileFormat format);

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