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

    /**
     * Знаходить нещодавно прослухані аудіокниги користувача.
     *
     * @param userId ідентифікатор користувача
     * @param limit  кількість записів для отримання
     * @return список записів прогресу, відсортованих за датою останнього прослуховування
     */
    List<ListeningProgress> findRecentlyListened(UUID userId, int limit);

    /**
     * Знаходить завершені аудіокниги користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список записів прогресу для завершених аудіокниг
     */
    List<ListeningProgress> findCompletedByUserId(UUID userId);

    /**
     * Знаходить незавершені аудіокниги користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список записів прогресу для незавершених аудіокниг
     */
    List<ListeningProgress> findInProgressByUserId(UUID userId);

    /**
     * Підраховує кількість записів прогресу для аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return кількість записів прогресу
     */
    long countByAudiobookId(UUID audiobookId);

    /**
     * Підраховує кількість завершених аудіокниг користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість завершених аудіокниг
     */
    long countCompletedByUserId(UUID userId);

    /**
     * Підраховує кількість незавершених аудіокниг користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість незавершених аудіокниг
     */
    long countInProgressByUserId(UUID userId);

    /**
     * Знаходить записи прогресу, оновлені після певної дати.
     *
     * @param userId ідентифікатор користувача
     * @param since  дата, після якої шукати оновлення
     * @return список записів прогресу
     */
    List<ListeningProgress> findUpdatedSince(UUID userId, java.time.LocalDateTime since);

    /**
     * Видаляє всі записи прогресу користувача.
     *
     * @param userId ідентифікатор користувача
     */
    void deleteAllByUserId(UUID userId);

    /**
     * Видаляє всі записи прогресу для аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     */
    void deleteAllByAudiobookId(UUID audiobookId);
}