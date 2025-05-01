package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;

import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з прогресом прослуховування.
 */
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
}