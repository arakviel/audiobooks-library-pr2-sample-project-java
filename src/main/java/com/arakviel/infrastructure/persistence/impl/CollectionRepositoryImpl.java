package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Collection;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з колекціями.
 */
public class CollectionRepositoryImpl extends GenericRepository<Collection, UUID> implements CollectionRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public CollectionRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Collection.class, "collections");
    }

    /**
     * Пошук колекцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    @Override
    public List<Collection> findByUserId(UUID userId) {
        return findByField("user_id", userId);
    }

    /**
     * Пошук аудіокниг у колекції за ідентифікатором колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findAudiobooksByCollectionId(UUID collectionId) {
        String baseSql = "SELECT a.* FROM audiobooks a JOIN audiobook_collection ac ON a.id = ac.audiobook_id WHERE ac.collection_id = ?";
        return executeQuery(baseSql, stmt -> stmt.setObject(1, collectionId), this::mapResultSetToAudiobook);
    }

    /**
     * Прикріплення аудіокниги до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    @Override
    public void attachAudiobookToCollection(UUID collectionId, UUID audiobookId) {
        String sql = "INSERT INTO audiobook_collection (collection_id, audiobook_id) VALUES (?, ?)";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, collectionId);
            statement.setObject(2, audiobookId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка прикріплення аудіокниги до колекції: " + sql, e);
        }
    }

    /**
     * Зіставлення ResultSet у аудіокнигу.
     *
     * @param rs результат запиту
     * @return аудіокнига
     */
    private Audiobook mapResultSetToAudiobook(ResultSet rs) {
        try {
            Audiobook audiobook = new Audiobook();
            audiobook.setId(rs.getObject("id", UUID.class));
            audiobook.setAuthorId(rs.getObject("author_id", UUID.class));
            audiobook.setGenreId(rs.getObject("genre_id", UUID.class));
            audiobook.setTitle(rs.getString("title"));
            audiobook.setDuration(rs.getInt("duration"));
            audiobook.setReleaseYear(rs.getInt("release_year"));
            audiobook.setDescription(rs.getString("description"));
            audiobook.setCoverImagePath(rs.getString("cover_image_path"));
            return audiobook;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet із аудіокнигою", e);
        }
    }
}