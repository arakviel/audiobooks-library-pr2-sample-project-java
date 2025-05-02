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
}