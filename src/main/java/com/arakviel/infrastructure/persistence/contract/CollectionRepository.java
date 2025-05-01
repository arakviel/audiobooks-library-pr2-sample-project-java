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

    // TODO: мені треба List<Collection> по UUID audiobookId, а не findAudiobooksByCollectionId.

    /**
     * Прикріплення аудіокниги до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param audiobookId  ідентифікатор аудіокниги
     */
    void attachAudiobookToCollection(UUID collectionId, UUID audiobookId);

    // TODO: мені треба detachAudiobookFromCollection.
}