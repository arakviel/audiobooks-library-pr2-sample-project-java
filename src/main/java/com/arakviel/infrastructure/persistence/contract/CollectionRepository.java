package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Collection;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Інтерфейс репозиторію для специфічних операцій з колекціями.
 */
public interface CollectionRepository extends Repository<Collection, UUID> {

    /**
     * Пошук колекцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    List<Collection> findByUserId(UUID userId);

    /**
     * Пошук аудіокниг у колекції за ідентифікатором колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return список аудіокниг
     */
    List<Audiobook> findAudiobooksByCollectionId(UUID collectionId);

    /**
     * Пошук колекцій за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список колекцій
     */
    List<Collection> findByAudiobookId(UUID audiobookId);

    /**
     * Прикріплення аудіокниги до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    void attachAudiobookToCollection(UUID collectionId, UUID audiobookId);

    /**
     * Від'єднання аудіокниги від колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    void detachAudiobookFromCollection(UUID collectionId, UUID audiobookId);

    /**
     * Підрахунок аудіокниг у колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return кількість аудіокниг
     */
    long countAudiobooksByCollectionId(UUID collectionId);

    /**
     * Пошук колекцій за назвою.
     *
     * @param name назва колекції
     * @return список колекцій
     */
    List<Collection> findByName(String name);

    /**
     * Видалення всіх аудіокниг із колекції.
     *
     * @param collectionId ідентифікатор колекції
     */
    void clearCollection(UUID collectionId);

    /**
     * Пошук колекцій за частковою відповідністю назви.
     *
     * @param partialName часткова назва колекції
     * @return список колекцій
     */
    List<Collection> findByPartialName(String partialName);

    /**
     * Додавання аудіокниги до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    void addAudiobookToCollection(UUID collectionId, UUID audiobookId);

    /**
     * Видалення аудіокниги з колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    void removeAudiobookFromCollection(UUID collectionId, UUID audiobookId);

    /**
     * Перевірка, чи містить колекція певну аудіокнигу.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     * @return true, якщо аудіокнига є в колекції
     */
    boolean containsAudiobook(UUID collectionId, UUID audiobookId);

    /**
     * Підрахунок кількості колекцій користувача.
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

    // ========== ПУБЛІЧНІ КОЛЕКЦІЇ ==========

    /**
     * Знаходить всі публічні колекції (user_id = NULL) з пагінацією.
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
}