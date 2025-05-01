package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з користувачами.
 */
public class UserRepositoryImpl extends GenericRepository<User, UUID> implements UserRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public UserRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, User.class, "users");
    }

    /**
     * Пошук користувача за ім’ям користувача.
     *
     * @param username ім’я користувача
     * @return список користувачів
     */
    @Override
    public List<User> findByUsername(String username) {
        return findByField("username", username);
    }

    /**
     * Пошук користувача за електронною поштою.
     *
     * @param email електронна пошта
     * @return список користувачів
     */
    @Override
    public List<User> findByEmail(String email) {
        return findByField("email", email);
    }

    /**
     * Пошук колекцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    @Override
    public List<Collection> findCollectionsByUserId(UUID userId) {
        String baseSql = "SELECT * FROM collections WHERE user_id = ?";
        return executeQuery(baseSql, stmt -> stmt.setObject(1, userId), this::mapResultSetToCollection);
    }

    /**
     * Пошук прогресу прослуховування за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список записів прогресу
     */
    @Override
    public List<ListeningProgress> findListeningProgressByUserId(UUID userId) {
        String baseSql = "SELECT * FROM listening_progresses WHERE user_id = ?";
        return executeQuery(baseSql, stmt -> stmt.setObject(1, userId), this::mapResultSetToListeningProgress);
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

    /**
     * Зіставлення ResultSet у прогрес прослуховування.
     *
     * @param rs результат запиту
     * @return прогрес прослуховування
     */
    private ListeningProgress mapResultSetToListeningProgress(ResultSet rs) {
        try {
            ListeningProgress progress = new ListeningProgress();
            progress.setId(rs.getObject("id", UUID.class));
            progress.setUserId(rs.getObject("user_id", UUID.class));
            progress.setAudiobookId(rs.getObject("audiobook_id", UUID.class));
            progress.setPosition(rs.getInt("position"));
            Timestamp lastListened = rs.getTimestamp("last_listened");
            progress.setLastListened(lastListened != null ? lastListened.toLocalDateTime() : null);
            return progress;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet із прогресом прослуховування", e);
        }
    }
}