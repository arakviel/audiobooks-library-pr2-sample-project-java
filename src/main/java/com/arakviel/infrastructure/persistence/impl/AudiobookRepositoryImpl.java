package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.exception.EntityMappingException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з аудіокнигами.
 */
public class AudiobookRepositoryImpl extends GenericRepository<Audiobook, UUID> implements AudiobookRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public AudiobookRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Audiobook.class, "audiobooks");
    }

    /**
     * Пошук аудіокниг за ідентифікатором автора.
     *
     * @param authorId ідентифікатор автора
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findByAuthorId(UUID authorId) {
        return findByField("author_id", authorId);
    }

    /**
     * Пошук аудіокниг за ідентифікатором жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findByGenreId(UUID genreId) {
        return findByField("genre_id", genreId);
    }

    /**
     * Отримання всіх файлів аудіокниги за її ідентифікатором (зв’язок один-до-багатьох).
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список файлів аудіокниги
     */
    @Override
    public List<AudiobookFile> findFilesByAudiobookId(UUID audiobookId) {
        String sql = "SELECT * FROM audiobook_files WHERE audiobook_id = ?";
        return executeQuery(sql, stmt -> stmt.setObject(1, audiobookId), this::mapResultSetToAudiobookFiles);
    }

    /**
     * Пошук усіх аудіокниг у колекції користувача (зв’язок багато-до-багатьох).
     *
     * @param collectionId ідентифікатор колекції
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findByCollectionId(UUID collectionId) {
        String baseSql = "SELECT a.* FROM audiobooks a JOIN collection_audiobooks ca ON a.id = ca.audiobook_id";
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("ca.collection_id = ?");
                    params.add(collectionId);
                },
                null, true, 0, Integer.MAX_VALUE, baseSql
        );
    }

    /**
     * Зіставлення ResultSet у список файлів аудіокниги.
     *
     * @param rs результат запиту
     * @return список файлів аудіокниги
     */
    private AudiobookFile mapResultSetToAudiobookFiles(ResultSet rs) {
        try {
            AudiobookFile file = new AudiobookFile();
            file.setId(rs.getObject("id", UUID.class));
            file.setAudiobookId(rs.getObject("audiobook_id", UUID.class));
            file.setFilePath(rs.getString("file_path"));
            String format = rs.getString("format");
            file.setFormat(format != null ? FileFormat.valueOf(format) : null);
            file.setSize(rs.getInt("size"));
            return file;
        } catch (Exception e) {
            throw new EntityMappingException("Помилка зіставлення ResultSet із файлом аудіокниги", e);
        }
    }
}