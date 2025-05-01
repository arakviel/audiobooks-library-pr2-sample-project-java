package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookFileRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;

import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з файлами аудіокниг.
 */
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
}