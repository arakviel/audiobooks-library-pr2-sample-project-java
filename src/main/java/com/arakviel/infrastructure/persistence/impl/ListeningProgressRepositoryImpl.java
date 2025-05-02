package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

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
}