package com.arakviel.application.contract;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Collection;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Інтерфейс для управління колекціями аудіокниг користувачів.
 */
public interface CollectionService {

    /**
     * Створює нову колекцію для користувача.
     *
     * @param collection колекція для створення
     * @return створена колекція
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила (наприклад, дублювання назви колекції для користувача)
     */
    Collection create(Collection collection);

    /**
     * Оновлює існуючу колекцію.
     *
     * @param id         ідентифікатор колекції для оновлення
     * @param collection оновлені дані колекції
     * @return оновлена колекція
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    Collection update(UUID id, Collection collection);

    /**
     * Видаляє колекцію та всі пов'язані з нею зв'язки з аудіокнигами.
     *
     * @param id ідентифікатор колекції для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     */
    void delete(UUID id);

    /**
     * Знаходить колекцію за ідентифікатором.
     *
     * @param id ідентифікатор колекції
     * @return Optional з колекцією, якщо знайдено
     */
    Optional<Collection> findById(UUID id);

    /**
     * Знаходить всі колекції з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список колекцій
     */
    List<Collection> findAll(int offset, int limit);

    /**
     * Знаходить колекції за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій користувача
     */
    List<Collection> findByUserId(UUID userId);

    /**
     * Знаходить колекції за назвою.
     *
     * @param name назва колекції
     * @return список колекцій
     */
    List<Collection> findByName(String name);

    /**
     * Знаходить колекції за частковою відповідністю назви.
     *
     * @param partialName часткова назва колекції
     * @return список колекцій
     */
    List<Collection> findByPartialName(String partialName);

    /**
     * Знаходить аудіокниги в колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return список аудіокниг у колекції
     */
    List<Audiobook> findAudiobooksByCollectionId(UUID collectionId);

    /**
     * Знаходить колекції, що містять певну аудіокнигу.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список колекцій
     */
    List<Collection> findByAudiobookId(UUID audiobookId);

    /**
     * Додає аудіокнигу до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо аудіокнига вже є в колекції або не існує
     */
    void addAudiobookToCollection(UUID collectionId, UUID audiobookId);

    /**
     * Видаляє аудіокнигу з колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     */
    void removeAudiobookFromCollection(UUID collectionId, UUID audiobookId);

    /**
     * Перевіряє, чи містить колекція певну аудіокнигу.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     * @return true, якщо аудіокнига є в колекції
     */
    boolean containsAudiobook(UUID collectionId, UUID audiobookId);

    /**
     * Підраховує кількість аудіокниг у колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return кількість аудіокниг
     */
    long countAudiobooksByCollectionId(UUID collectionId);

    /**
     * Підраховує кількість колекцій користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість колекцій
     */
    long countByUserId(UUID userId);

    /**
     * Знаходить колекції користувача за назвою.
     *
     * @param userId ідентифікатор користувача
     * @param name   назва колекції
     * @return список колекцій
     */
    List<Collection> findByUserIdAndName(UUID userId, String name);

    /**
     * Перевіряє, чи існує колекція з такою назвою у користувача.
     *
     * @param userId ідентифікатор користувача
     * @param name   назва колекції
     * @return true, якщо колекція існує
     */
    boolean existsByUserIdAndName(UUID userId, String name);

    /**
     * Копіює всі аудіокниги з однієї колекції в іншу.
     *
     * @param sourceCollectionId      ідентифікатор вихідної колекції
     * @param destinationCollectionId ідентифікатор цільової колекції
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо колекції не існують або належать різним користувачам
     */
    void copyAudiobooksToCollection(UUID sourceCollectionId, UUID destinationCollectionId);

    /**
     * Очищає колекцію (видаляє всі аудіокниги з неї).
     *
     * @param collectionId ідентифікатор колекції
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     */
    void clearCollection(UUID collectionId);

    // ========== ПУБЛІЧНІ КОЛЕКЦІЇ ==========

    /**
     * Створює нову публічну колекцію (загальнодоступну).
     *
     * @param collection колекція для створення (userId буде встановлено в null)
     * @return створена публічна колекція
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила (наприклад, дублювання назви публічної колекції)
     */
    Collection createPublicCollection(Collection collection);

    /**
     * Знаходить всі публічні колекції з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список публічних колекцій
     */
    List<Collection> findPublicCollections(int offset, int limit);

    /**
     * Знаходить публічні колекції за назвою.
     *
     * @param name назва колекції
     * @return список публічних колекцій
     */
    List<Collection> findPublicCollectionsByName(String name);

    /**
     * Знаходить публічні колекції за частковою відповідністю назви.
     *
     * @param partialName часткова назва колекції
     * @return список публічних колекцій
     */
    List<Collection> findPublicCollectionsByPartialName(String partialName);

    /**
     * Підраховує кількість публічних колекцій.
     *
     * @return кількість публічних колекцій
     */
    long countPublicCollections();

    /**
     * Перевіряє, чи існує публічна колекція з такою назвою.
     *
     * @param name назва колекції
     * @return true, якщо публічна колекція існує
     */
    boolean existsPublicCollectionByName(String name);

    /**
     * Перевіряє, чи є колекція публічною.
     *
     * @param collectionId ідентифікатор колекції
     * @return true, якщо колекція публічна (userId = null)
     */
    boolean isPublicCollection(UUID collectionId);

    /**
     * Знаходить найпопулярніші публічні колекції (за кількістю аудіокниг).
     *
     * @param limit кількість записів для отримання
     * @return список популярних публічних колекцій
     */
    List<Collection> findMostPopularPublicCollections(int limit);

    /**
     * Знаходить нещодавно створені публічні колекції.
     *
     * @param limit кількість записів для отримання
     * @return список нещодавно створених публічних колекцій
     */
    List<Collection> findRecentlyCreatedPublicCollections(int limit);

    /**
     * Підраховує загальну кількість колекцій.
     *
     * @return загальна кількість колекцій
     */
    long count();
}
