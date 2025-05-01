package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Інтерфейс репозиторію для специфічних операцій з прогресом прослуховування.
 */
public interface ListeningProgressRepository extends Repository<ListeningProgress, UUID> {

    /**
     * Пошук прогресу прослуховування за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список записів прогресу
     */
    List<ListeningProgress> findByUserId(UUID userId);

    /**
     * Пошук прогресу прослуховування за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список записів прогресу
     */
    List<ListeningProgress> findByAudiobookId(UUID audiobookId);
}
