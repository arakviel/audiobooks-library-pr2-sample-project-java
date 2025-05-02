package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
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
}