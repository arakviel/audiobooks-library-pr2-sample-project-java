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

    @Override
    public List<AudiobookFile> findByAudiobookIdAndFormat(UUID audiobookId, FileFormat format) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("audiobook_id = ?");
                    whereClause.add("format = ?");
                    params.add(audiobookId);
                    params.add(format.name().toLowerCase());
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    @Override
    public long countByFormat(FileFormat format) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("format = ?");
            params.add(format.name().toLowerCase());
        };
        return count(filter);
    }

    @Override
    public long calculateTotalSizeByAudiobookId(UUID audiobookId) {
        // Простий підрахунок через суму - в реальному проекті краще використати SQL SUM
        List<AudiobookFile> files = findByAudiobookId(audiobookId);
        return files.stream().mapToLong(file -> file.getSize() != null ? file.getSize() : 0).sum();
    }

    @Override
    public java.util.Optional<AudiobookFile> findLargestFileByAudiobookId(UUID audiobookId) {
        List<AudiobookFile> files = findByAudiobookId(audiobookId);
        return files.stream()
                .filter(file -> file.getSize() != null)
                .max((f1, f2) -> Integer.compare(f1.getSize(), f2.getSize()));
    }

    @Override
    public java.util.Optional<AudiobookFile> findSmallestFileByAudiobookId(UUID audiobookId) {
        List<AudiobookFile> files = findByAudiobookId(audiobookId);
        return files.stream()
                .filter(file -> file.getSize() != null)
                .min((f1, f2) -> Integer.compare(f1.getSize(), f2.getSize()));
    }

    @Override
    public boolean existsByAudiobookIdAndFilePath(UUID audiobookId, String filePath) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("audiobook_id = ?");
            whereClause.add("file_path = ?");
            params.add(audiobookId);
            params.add(filePath);
        };
        return count(filter) > 0;
    }

    @Override
    public List<AudiobookFile> findByAudiobookIdOrderBySize(UUID audiobookId, boolean ascending) {
        List<AudiobookFile> files = findByAudiobookId(audiobookId);
        files.sort((f1, f2) -> {
            int size1 = f1.getSize() != null ? f1.getSize() : 0;
            int size2 = f2.getSize() != null ? f2.getSize() : 0;
            return ascending ? Integer.compare(size1, size2) : Integer.compare(size2, size1);
        });
        return files;
    }

    @Override
    public List<AudiobookFile> findPotentialDuplicates(UUID audiobookId) {
        List<AudiobookFile> files = findByAudiobookId(audiobookId);
        return files.stream()
                .filter(file -> file.getSize() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        file -> file.getFormat() + "_" + file.getSize()))
                .values()
                .stream()
                .filter(group -> group.size() > 1)
                .flatMap(java.util.List::stream)
                .collect(java.util.stream.Collectors.toList());
    }
}