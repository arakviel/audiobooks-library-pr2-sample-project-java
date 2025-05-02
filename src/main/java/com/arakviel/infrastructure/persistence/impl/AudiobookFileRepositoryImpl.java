package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookFileRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з файлами аудіокниг.
 */
@Repository
public class AudiobookFileRepositoryImpl extends GenericRepository<AudiobookFile, UUID> implements AudiobookFileRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public AudiobookFileRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, AudiobookFile.class, "audiobook_files");
    }

    /**
     * Пошук файлів аудіокниги за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список файлів аудіокниги
     */
    @Override
    public List<AudiobookFile> findByAudiobookId(UUID audiobookId) {
        return findByField("audiobook_id", audiobookId);
    }

    /**
     * Пошук файлів аудіокниги за форматом.
     *
     * @param format формат файлу
     * @return список файлів аудіокниги
     */
    @Override
    public List<AudiobookFile> findByFormat(FileFormat format) {
        return findByField("format", format.name().toLowerCase());
    }

    /**
     * Підрахунок файлів для аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return кількість файлів
     */
    @Override
    public long countByAudiobookId(UUID audiobookId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("audiobook_id = ?");
            params.add(audiobookId);
        };
        return count(filter);
    }

    /**
     * Пошук файлів за діапазоном розміру.
     *
     * @param minSize мінімальний розмір (у байтах)
     * @param maxSize максимальний розмір (у байтах)
     * @return список файлів
     */
    @Override
    public List<AudiobookFile> findBySizeRange(int minSize, int maxSize) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("size >= ?");
                    whereClause.add("size <= ?");
                    params.add(minSize);
                    params.add(maxSize);
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }
}