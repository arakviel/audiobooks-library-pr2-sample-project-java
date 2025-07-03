package com.arakviel.application.contract;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Інтерфейс для управління прогресом прослуховування аудіокниг користувачами.
 */
public interface ListeningProgressService {

    /**
     * Створює новий запис прогресу прослуховування.
     *
     * @param progress запис прогресу для створення
     * @return створений запис прогресу
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    ListeningProgress create(ListeningProgress progress);

    /**
     * Оновлює існуючий запис прогресу прослуховування.
     *
     * @param id       ідентифікатор запису прогресу для оновлення
     * @param progress оновлені дані прогресу
     * @return оновлений запис прогресу
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    ListeningProgress update(UUID id, ListeningProgress progress);

    /**
     * Видаляє запис прогресу прослуховування.
     *
     * @param id ідентифікатор запису прогресу для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     */
    void delete(UUID id);

    /**
     * Знаходить запис прогресу за ідентифікатором.
     *
     * @param id ідентифікатор запису прогресу
     * @return Optional з записом прогресу, якщо знайдено
     */
    Optional<ListeningProgress> findById(UUID id);

    /**
     * Знаходить всі записи прогресу з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список записів прогресу
     */
    List<ListeningProgress> findAll(int offset, int limit);

    /**
     * Знаходить записи прогресу за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список записів прогресу користувача
     */
    List<ListeningProgress> findByUserId(UUID userId);

    /**
     * Знаходить записи прогресу за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список записів прогресу для аудіокниги
     */
    List<ListeningProgress> findByAudiobookId(UUID audiobookId);

    /**
     * Знаходить запис прогресу для конкретного користувача та аудіокниги.
     *
     * @param userId      ідентифікатор користувача
     * @param audiobookId ідентифікатор аудіокниги
     * @return Optional з записом прогресу, якщо знайдено
     */
    Optional<ListeningProgress> findByUserIdAndAudiobookId(UUID userId, UUID audiobookId);

    /**
     * Оновлює позицію прослуховування для користувача та аудіокниги.
     * Створює новий запис, якщо він не існує.
     *
     * @param userId      ідентифікатор користувача
     * @param audiobookId ідентифікатор аудіокниги
     * @param position    нова позиція в секундах
     * @return оновлений або створений запис прогресу
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    ListeningProgress updateProgress(UUID userId, UUID audiobookId, int position);

    /**
     * Позначає аудіокнигу як завершену для користувача.
     *
     * @param userId      ідентифікатор користувача
     * @param audiobookId ідентифікатор аудіокниги
     * @return оновлений запис прогресу
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    ListeningProgress markAsCompleted(UUID userId, UUID audiobookId);

    /**
     * Скидає прогрес прослуховування для користувача та аудіокниги.
     *
     * @param userId      ідентифікатор користувача
     * @param audiobookId ідентифікатор аудіокниги
     * @return оновлений запис прогресу
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    ListeningProgress resetProgress(UUID userId, UUID audiobookId);

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
     * Підраховує кількість записів прогресу для користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість записів прогресу
     */
    long countByUserId(UUID userId);

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
     * Обчислює відсоток прогресу прослуховування аудіокниги.
     *
     * @param userId      ідентифікатор користувача
     * @param audiobookId ідентифікатор аудіокниги
     * @return відсоток прогресу (0-100)
     */
    double calculateProgressPercentage(UUID userId, UUID audiobookId);

    /**
     * Знаходить записи прогресу, оновлені після певної дати.
     *
     * @param userId ідентифікатор користувача
     * @param since  дата, після якої шукати оновлення
     * @return список записів прогресу
     */
    List<ListeningProgress> findUpdatedSince(UUID userId, LocalDateTime since);

    /**
     * Видаляє всі записи прогресу користувача.
     *
     * @param userId ідентифікатор користувача
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     */
    void deleteAllByUserId(UUID userId);

    /**
     * Видаляє всі записи прогресу для аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     */
    void deleteAllByAudiobookId(UUID audiobookId);
}
