package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Collection;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з колекціями.
 */
@Repository
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
     * Пошук колекцій за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список колекцій
     */
    @Override
    public List<Collection> findByAudiobookId(UUID audiobookId) {
        String baseSql = "SELECT c.* FROM collections c JOIN audiobook_collection ac ON c.id = ac.collection_id WHERE ac.audiobook_id = ?";
        return executeQuery(baseSql, stmt -> stmt.setObject(1, audiobookId), this::mapResultSetToCollection);
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
     * Від'єднання аудіокниги від колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    @Override
    public void detachAudiobookFromCollection(UUID collectionId, UUID audiobookId) {
        String sql = "DELETE FROM audiobook_collection WHERE collection_id = ? AND audiobook_id = ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, collectionId);
            statement.setObject(2, audiobookId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка від'єднання аудіокниги від колекції: " + sql, e);
        }
    }

    /**
     * Підрахунок аудіокниг у колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return кількість аудіокниг
     */
    @Override
    public long countAudiobooksByCollectionId(UUID collectionId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("collection_id = ?");
            params.add(collectionId);
        };
        return count(filter, "audiobook_collection");
    }

    /**
     * Пошук колекцій за назвою.
     *
     * @param name назва колекції
     * @return список колекцій
     */
    @Override
    public List<Collection> findByName(String name) {
        return findByField("name", name);
    }

    /**
     * Видалення всіх аудіокниг із колекції.
     *
     * @param collectionId ідентифікатор колекції
     */
    @Override
    public void clearCollection(UUID collectionId) {
        String sql = "DELETE FROM audiobook_collection WHERE collection_id = ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, collectionId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка очищення колекції: " + sql, e);
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

    /**
     * Зіставлення ResultSet у колекцію.
     *
     * @param rs результат запиту
     * @return колекція
     */
    private Collection mapResultSetToCollection(ResultSet rs) {
        try {
            Collection collection = new Collection();
            collection.setId(rs.getObject("id", UUID.class));
            collection.setUserId(rs.getObject("user_id", UUID.class));
            collection.setName(rs.getString("name"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            collection.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
            return collection;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet із колекцією", e);
        }
    }
}