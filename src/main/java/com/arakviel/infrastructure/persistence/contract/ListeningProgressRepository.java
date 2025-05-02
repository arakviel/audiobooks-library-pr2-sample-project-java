package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.Optional;
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

    // TODO: Audiobook last progress by lastListened field

    /**
     * Пошук прогресу прослуховування для конкретного користувача та аудіокниги.
     *
     * @param userId      ідентифікатор користувача
     * @param audiobookId ідентифікатор аудіокниги
     * @return Optional із прогресом прослуховування
     */
    Optional<ListeningProgress> findByUserIdAndAudiobookId(UUID userId, UUID audiobookId);

    /**
     * Підрахунок записів прогресу для користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість записів прогресу
     */
    long countByUserId(UUID userId);
}