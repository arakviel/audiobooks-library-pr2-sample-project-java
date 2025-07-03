package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з прогресом прослуховування.
 */
@Repository
public class ListeningProgressRepositoryImpl extends GenericRepository<ListeningProgress, UUID> implements ListeningProgressRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public ListeningProgressRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, ListeningProgress.class, "listening_progresses");
    }

    /**
     * Пошук прогресу прослуховування за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список записів прогресу
     */
    @Override
    public List<ListeningProgress> findByUserId(UUID userId) {
        return findByField("user_id", userId);
    }

    /**
     * Пошук прогресу прослуховування за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список записів прогресу
     */
    @Override
    public List<ListeningProgress> findByAudiobookId(UUID audiobookId) {
        return findByField("audiobook_id", audiobookId);
    }

    /**
     * Пошук прогресу прослуховування для конкретного користувача та аудіокниги.
     *
     * @param userId      ідентифікатор користувача
     * @param audiobookId ідентифікатор аудіокниги
     * @return Optional із прогресом прослуховування
     */
    @Override
    public Optional<ListeningProgress> findByUserIdAndAudiobookId(UUID userId, UUID audiobookId) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("user_id = ?");
                    whereClause.add("audiobook_id = ?");
                    params.add(userId);
                    params.add(audiobookId);
                },
                null, true, 0, 1
        ).stream().findFirst();
    }

    /**
     * Підрахунок записів прогресу для користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість записів прогресу
     */
    @Override
    public long countByUserId(UUID userId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("user_id = ?");
            params.add(userId);
        };
        return count(filter);
    }

    @Override
    public List<ListeningProgress> findRecentlyListened(UUID userId, int limit) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("user_id = ?");
                    params.add(userId);
                },
                "last_listened", false, 0, limit
        );
    }

    @Override
    public List<ListeningProgress> findCompletedByUserId(UUID userId) {
        String sql = "SELECT lp.* FROM listening_progresses lp " +
                     "JOIN audiobooks a ON lp.audiobook_id = a.id " +
                     "WHERE lp.user_id = ? AND lp.position >= a.duration";
        return executeQuery(sql, stmt -> stmt.setObject(1, userId));
    }

    @Override
    public List<ListeningProgress> findInProgressByUserId(UUID userId) {
        String sql = "SELECT lp.* FROM listening_progresses lp " +
                     "JOIN audiobooks a ON lp.audiobook_id = a.id " +
                     "WHERE lp.user_id = ? AND lp.position < a.duration AND lp.position > 0";
        return executeQuery(sql, stmt -> stmt.setObject(1, userId));
    }

    @Override
    public long countByAudiobookId(UUID audiobookId) {
        String sql = "SELECT COUNT(*) FROM listening_progresses WHERE audiobook_id = ?";
        return executeCountQuery(sql, stmt -> stmt.setObject(1, audiobookId));
    }

    @Override
    public long countCompletedByUserId(UUID userId) {
        String sql = "SELECT COUNT(*) FROM listening_progresses lp " +
                     "JOIN audiobooks a ON lp.audiobook_id = a.id " +
                     "WHERE lp.user_id = ? AND lp.position >= a.duration";
        return executeCountQuery(sql, stmt -> stmt.setObject(1, userId));
    }

    @Override
    public long countInProgressByUserId(UUID userId) {
        String sql = "SELECT COUNT(*) FROM listening_progresses lp " +
                     "JOIN audiobooks a ON lp.audiobook_id = a.id " +
                     "WHERE lp.user_id = ? AND lp.position < a.duration AND lp.position > 0";
        return executeCountQuery(sql, stmt -> stmt.setObject(1, userId));
    }

    @Override
    public List<ListeningProgress> findUpdatedSince(UUID userId, LocalDateTime since) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("user_id = ?");
                    whereClause.add("last_listened >= ?");
                    params.add(userId);
                    params.add(since);
                },
                "last_listened", false, 0, Integer.MAX_VALUE
        );
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        String sql = "DELETE FROM listening_progresses WHERE user_id = ?";
        executeUpdate(sql, stmt -> stmt.setObject(1, userId));
    }

    @Override
    public void deleteAllByAudiobookId(UUID audiobookId) {
        String sql = "DELETE FROM listening_progresses WHERE audiobook_id = ?";
        executeUpdate(sql, stmt -> stmt.setObject(1, audiobookId));
    }

    /**
     * Виконання запиту для підрахунку записів.
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
     * Виконання запиту для оновлення/видалення.
     */
    private void executeUpdate(String sql, ParameterSetter parameterSetter) {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (parameterSetter != null) {
                parameterSetter.setParameters(statement);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка виконання запиту оновлення", e);
        }
    }

    /**
     * Функціональний інтерфейс для встановлення параметрів запиту.
     */
    @FunctionalInterface
    private interface ParameterSetter {
        void setParameters(PreparedStatement statement) throws SQLException;
    }
}