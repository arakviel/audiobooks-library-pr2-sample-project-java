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
        String sql = "SELECT COUNT(*) FROM audiobook_collection WHERE collection_id = ?";
        return executeCountQuery(sql, stmt -> stmt.setObject(1, collectionId));
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
     * Пошук колекцій за частковою відповідністю назви.
     *
     * @param partialName часткова назва колекції
     * @return список колекцій
     */
    @Override
    public List<Collection> findByPartialName(String partialName) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("name ILIKE ?");
                    params.add("%" + partialName + "%");
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Додавання аудіокниги до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    @Override
    public void addAudiobookToCollection(UUID collectionId, UUID audiobookId) {
        attachAudiobookToCollection(collectionId, audiobookId);
    }

    /**
     * Видалення аудіокниги з колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    @Override
    public void removeAudiobookFromCollection(UUID collectionId, UUID audiobookId) {
        detachAudiobookFromCollection(collectionId, audiobookId);
    }

    /**
     * Перевірка, чи містить колекція певну аудіокнигу.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     * @return true, якщо аудіокнига є в колекції
     */
    @Override
    public boolean containsAudiobook(UUID collectionId, UUID audiobookId) {
        String sql = "SELECT COUNT(*) FROM audiobook_collection WHERE collection_id = ? AND audiobook_id = ?";
        return executeCountQuery(sql, stmt -> {
            stmt.setObject(1, collectionId);
            stmt.setObject(2, audiobookId);
        }) > 0;
    }

    /**
     * Підрахунок кількості колекцій користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість колекцій
     */
    @Override
    public long countByUserId(UUID userId) {
        String sql = "SELECT COUNT(*) FROM collections WHERE user_id = ?";
        return executeCountQuery(sql, stmt -> stmt.setObject(1, userId));
    }

    /**
     * Знаходить колекції користувача за назвою.
     *
     * @param userId ідентифікатор користувача
     * @param name   назва колекції
     * @return список колекцій
     */
    @Override
    public List<Collection> findByUserIdAndName(UUID userId, String name) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("user_id = ?");
                    whereClause.add("name = ?");
                    params.add(userId);
                    params.add(name);
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Перевіряє, чи існує колекція з такою назвою у користувача.
     *
     * @param userId ідентифікатор користувача
     * @param name   назва колекції
     * @return true, якщо колекція існує
     */
    @Override
    public boolean existsByUserIdAndName(UUID userId, String name) {
        String sql = "SELECT COUNT(*) FROM collections WHERE user_id = ? AND name = ?";
        return executeCountQuery(sql, stmt -> {
            stmt.setObject(1, userId);
            stmt.setString(2, name);
        }) > 0;
    }

    // ========== ПУБЛІЧНІ КОЛЕКЦІЇ ==========

    /**
     * Знаходить всі публічні колекції (user_id = NULL) з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список публічних колекцій
     */
    @Override
    public List<Collection> findPublicCollections(int offset, int limit) {
        return findAll(
                (whereClause, params) -> whereClause.add("user_id IS NULL"),
                null, true, offset, limit
        );
    }

    /**
     * Знаходить публічні колекції за назвою.
     *
     * @param name назва колекції
     * @return список публічних колекцій
     */
    @Override
    public List<Collection> findPublicCollectionsByName(String name) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("user_id IS NULL");
                    whereClause.add("name = ?");
                    params.add(name);
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Знаходить публічні колекції за частковою відповідністю назви.
     *
     * @param partialName часткова назва колекції
     * @return список публічних колекцій
     */
    @Override
    public List<Collection> findPublicCollectionsByPartialName(String partialName) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("user_id IS NULL");
                    whereClause.add("name ILIKE ?");
                    params.add("%" + partialName + "%");
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Підраховує кількість публічних колекцій.
     *
     * @return кількість публічних колекцій
     */
    @Override
    public long countPublicCollections() {
        String sql = "SELECT COUNT(*) FROM collections WHERE user_id IS NULL";
        return executeCountQuery(sql, null);
    }

    /**
     * Перевіряє, чи існує публічна колекція з такою назвою.
     *
     * @param name назва колекції
     * @return true, якщо публічна колекція існує
     */
    @Override
    public boolean existsPublicCollectionByName(String name) {
        String sql = "SELECT COUNT(*) FROM collections WHERE user_id IS NULL AND name = ?";
        return executeCountQuery(sql, stmt -> stmt.setString(1, name)) > 0;
    }

    /**
     * Знаходить найпопулярніші публічні колекції (за кількістю аудіокниг).
     *
     * @param limit кількість записів для отримання
     * @return список популярних публічних колекцій
     */
    @Override
    public List<Collection> findMostPopularPublicCollections(int limit) {
        String sql = """
            SELECT c.* FROM collections c
            LEFT JOIN audiobook_collection ac ON c.id = ac.collection_id
            WHERE c.user_id IS NULL
            GROUP BY c.id, c.user_id, c.name, c.created_at
            ORDER BY COUNT(ac.audiobook_id) DESC, c.created_at DESC
            LIMIT ?
            """;
        return executeQuery(sql, stmt -> stmt.setInt(1, limit), this::mapResultSetToCollection);
    }

    /**
     * Знаходить нещодавно створені публічні колекції.
     *
     * @param limit кількість записів для отримання
     * @return список нещодавно створених публічних колекцій
     */
    @Override
    public List<Collection> findRecentlyCreatedPublicCollections(int limit) {
        return findAll(
                (whereClause, params) -> whereClause.add("user_id IS NULL"),
                "created_at", false, 0, limit
        );
    }

    /**
     * Виконання запиту для підрахунку записів.
     *
     * @param sql           SQL запит
     * @param parameterSetter функція для встановлення параметрів
     * @return кількість записів
     */
    private long executeCountQuery(String sql, ParameterSetter parameterSetter) {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (parameterSetter != null) {
                parameterSetter.setParameters(statement);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка виконання запиту підрахунку", e);
        }
    }

    /**
     * Функціональний інтерфейс для встановлення параметрів запиту.
     */
    @FunctionalInterface
    private interface ParameterSetter {
        void setParameters(PreparedStatement statement) throws SQLException;
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