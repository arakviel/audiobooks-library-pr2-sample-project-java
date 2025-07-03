package com.arakviel.application.impl;

import com.arakviel.application.contract.CollectionService;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.exception.ValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Collection;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація сервісу для управління колекціями аудіокниг.
 */
@Service
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final AudiobookRepository audiobookRepository;
    private final UserRepository userRepository;
    private final PersistenceContext persistenceContext;

    public CollectionServiceImpl(CollectionRepository collectionRepository,
                               AudiobookRepository audiobookRepository,
                               UserRepository userRepository,
                               PersistenceContext persistenceContext) {
        this.collectionRepository = collectionRepository;
        this.audiobookRepository = audiobookRepository;
        this.userRepository = userRepository;
        this.persistenceContext = persistenceContext;
    }

    /**
     * Створює нову колекцію для користувача.
     */
    @Override
    public Collection create(Collection collection) {
        validateCollection(collection);
        if (collection.getId() == null) {
            collection.setId(UUID.randomUUID());
        }
        if (collection.getCreatedAt() == null) {
            collection.setCreatedAt(LocalDateTime.now());
        }

        // Перевірка існування користувача (тільки якщо userId не null)
        if (collection.getUserId() != null && !userRepository.findById(collection.getUserId()).isPresent()) {
            throw new ValidationException("Користувач з ідентифікатором " + collection.getUserId() + " не існує.");
        }

        // Перевірка на дублювання назви колекції для користувача
        if (collection.getUserId() != null && existsByUserIdAndName(collection.getUserId(), collection.getName())) {
            throw new ValidationException("Колекція з назвою '" + collection.getName() + "' вже існує у користувача.");
        }

        persistenceContext.registerNew(collection);
        persistenceContext.commit();
        return collection;
    }

    /**
     * Оновлює існуючу колекцію.
     */
    @Override
    public Collection update(UUID id, Collection collection) {
        validateCollection(collection);
        collection.setId(id);

        // Перевірка існування колекції
        Optional<Collection> existingCollectionOpt = collectionRepository.findById(id);
        if (!existingCollectionOpt.isPresent()) {
            throw new ValidationException("Колекція з ідентифікатором " + id + " не існує.");
        }

        Collection existingCollection = existingCollectionOpt.get();

        // Зберігаємо тип колекції (публічна/приватна) та дату створення
        collection.setUserId(existingCollection.getUserId());
        collection.setCreatedAt(existingCollection.getCreatedAt());

        // Перевірка на дублювання назви (якщо назва змінюється)
        if (!existingCollection.getName().equals(collection.getName())) {
            if (collection.getUserId() == null) {
                // Публічна колекція
                if (existsPublicCollectionByName(collection.getName())) {
                    throw new ValidationException("Публічна колекція з назвою '" + collection.getName() + "' вже існує.");
                }
            } else {
                // Приватна колекція
                if (existsByUserIdAndName(collection.getUserId(), collection.getName())) {
                    throw new ValidationException("Колекція з назвою '" + collection.getName() + "' вже існує у користувача.");
                }
            }
        }

        persistenceContext.registerUpdated(id, collection);
        persistenceContext.commit();
        return collection;
    }

    /**
     * Видаляє колекцію та всі пов'язані з нею зв'язки з аудіокнигами.
     */
    @Override
    public void delete(UUID id) {
        Optional<Collection> collectionOpt = collectionRepository.findById(id);
        if (collectionOpt.isPresent()) {
            Collection collection = collectionOpt.get();
            persistenceContext.registerDeleted(collection);
            persistenceContext.commit();
        }
    }

    @Override
    public Optional<Collection> findById(UUID id) {
        return collectionRepository.findById(id);
    }

    @Override
    public List<Collection> findAll(int offset, int limit) {
        validatePagination(offset, limit);
        return collectionRepository.findAll(offset, limit);
    }

    @Override
    public List<Collection> findByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return collectionRepository.findByUserId(userId);
    }

    @Override
    public List<Collection> findByName(String name) {
        new ValidationHelper()
                .notEmpty("name", name)
                .throwIfHasErrors();
        return collectionRepository.findByName(name);
    }

    @Override
    public List<Collection> findByPartialName(String partialName) {
        new ValidationHelper()
                .notEmpty("partialName", partialName)
                .throwIfHasErrors();
        return collectionRepository.findByPartialName(partialName);
    }

    @Override
    public List<Audiobook> findAudiobooksByCollectionId(UUID collectionId) {
        new ValidationHelper()
                .validUuid("collectionId", collectionId)
                .throwIfHasErrors();
        return collectionRepository.findAudiobooksByCollectionId(collectionId);
    }

    @Override
    public List<Collection> findByAudiobookId(UUID audiobookId) {
        new ValidationHelper()
                .validUuid("audiobookId", audiobookId)
                .throwIfHasErrors();
        return collectionRepository.findByAudiobookId(audiobookId);
    }

    @Override
    public void addAudiobookToCollection(UUID collectionId, UUID audiobookId) {
        ValidationHelper validator = new ValidationHelper()
                .validUuid("collectionId", collectionId)
                .validUuid("audiobookId", audiobookId);

        // Перевірка існування колекції
        if (collectionId != null && !collectionRepository.findById(collectionId).isPresent()) {
            validator.addError("collectionId", "колекція з таким ідентифікатором не існує");
        }

        // Перевірка існування аудіокниги
        if (audiobookId != null && !audiobookRepository.findById(audiobookId).isPresent()) {
            validator.addError("audiobookId", "аудіокнига з таким ідентифікатором не існує");
        }

        // Перевірка, чи аудіокнига вже є в колекції
        if (collectionId != null && audiobookId != null && containsAudiobook(collectionId, audiobookId)) {
            validator.addError("audiobookId", "аудіокнига вже є в колекції");
        }

        validator.throwIfHasErrors();
        collectionRepository.addAudiobookToCollection(collectionId, audiobookId);
    }

    @Override
    public void removeAudiobookFromCollection(UUID collectionId, UUID audiobookId) {
        new ValidationHelper()
                .validUuid("collectionId", collectionId)
                .validUuid("audiobookId", audiobookId)
                .throwIfHasErrors();

        collectionRepository.removeAudiobookFromCollection(collectionId, audiobookId);
    }

    @Override
    public boolean containsAudiobook(UUID collectionId, UUID audiobookId) {
        new ValidationHelper()
                .validUuid("collectionId", collectionId)
                .validUuid("audiobookId", audiobookId)
                .throwIfHasErrors();
        return collectionRepository.containsAudiobook(collectionId, audiobookId);
    }

    @Override
    public long countAudiobooksByCollectionId(UUID collectionId) {
        new ValidationHelper()
                .validUuid("collectionId", collectionId)
                .throwIfHasErrors();
        return collectionRepository.countAudiobooksByCollectionId(collectionId);
    }

    @Override
    public long countByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return collectionRepository.countByUserId(userId);
    }

    @Override
    public List<Collection> findByUserIdAndName(UUID userId, String name) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .notEmpty("name", name)
                .throwIfHasErrors();
        return collectionRepository.findByUserIdAndName(userId, name);
    }

    @Override
    public boolean existsByUserIdAndName(UUID userId, String name) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .notEmpty("name", name)
                .throwIfHasErrors();
        return collectionRepository.existsByUserIdAndName(userId, name);
    }

    @Override
    public void copyAudiobooksToCollection(UUID sourceCollectionId, UUID destinationCollectionId) {
        if (sourceCollectionId == null) {
            throw new ValidationException("Ідентифікатор вихідної колекції не може бути null.");
        }
        if (destinationCollectionId == null) {
            throw new ValidationException("Ідентифікатор цільової колекції не може бути null.");
        }

        // Перевірка існування колекцій
        Optional<Collection> sourceOpt = collectionRepository.findById(sourceCollectionId);
        Optional<Collection> destinationOpt = collectionRepository.findById(destinationCollectionId);

        if (!sourceOpt.isPresent()) {
            throw new ValidationException("Вихідна колекція не існує.");
        }
        if (!destinationOpt.isPresent()) {
            throw new ValidationException("Цільова колекція не існує.");
        }

        // Перевірка, що колекції належать одному користувачу
        if (!sourceOpt.get().getUserId().equals(destinationOpt.get().getUserId())) {
            throw new ValidationException("Колекції повинні належати одному користувачу.");
        }

        List<Audiobook> audiobooks = findAudiobooksByCollectionId(sourceCollectionId);
        for (Audiobook audiobook : audiobooks) {
            if (!containsAudiobook(destinationCollectionId, audiobook.getId())) {
                collectionRepository.addAudiobookToCollection(destinationCollectionId, audiobook.getId());
            }
        }
    }

    @Override
    public void clearCollection(UUID collectionId) {
        if (collectionId == null) {
            throw new ValidationException("Ідентифікатор колекції не може бути null.");
        }

        List<Audiobook> audiobooks = findAudiobooksByCollectionId(collectionId);
        for (Audiobook audiobook : audiobooks) {
            collectionRepository.removeAudiobookFromCollection(collectionId, audiobook.getId());
        }
    }

    /**
     * Валідує дані колекції з використанням нового підходу.
     */
    private void validateCollection(Collection collection) {
        ValidationHelper validator = new ValidationHelper()
                .notNull("collection", collection);

        if (collection != null) {
            validator.notEmpty("name", collection.getName());
        }

        validator.throwIfHasErrors();
    }

    /**
     * Валідує дані колекції для приватних колекцій (з обов'язковим userId).
     */
    private void validatePrivateCollection(Collection collection) {
        new ValidationHelper()
                .notNull("collection", collection)
                .notEmpty("name", collection != null ? collection.getName() : null)
                .validUuid("userId", collection != null ? collection.getUserId() : null)
                .throwIfHasErrors();
    }

    /**
     * Валідує параметри пагінації з використанням нового підходу.
     */
    private void validatePagination(int offset, int limit) {
        new ValidationHelper()
                .nonNegative("offset", offset)
                .positive("limit", limit)
                .throwIfHasErrors();
    }

    // ========== ПУБЛІЧНІ КОЛЕКЦІЇ ==========

    /**
     * Створює нову публічну колекцію (загальнодоступну).
     */
    @Override
    public Collection createPublicCollection(Collection collection) {
        validateCollection(collection);
        if (collection.getId() == null) {
            collection.setId(UUID.randomUUID());
        }
        if (collection.getCreatedAt() == null) {
            collection.setCreatedAt(LocalDateTime.now());
        }

        // Встановлюємо userId в null для публічної колекції
        collection.setUserId(null);

        // Перевірка на дублювання назви публічної колекції
        if (existsPublicCollectionByName(collection.getName())) {
            throw new ValidationException("Публічна колекція з назвою '" + collection.getName() + "' вже існує.");
        }

        persistenceContext.registerNew(collection);
        persistenceContext.commit();
        return collection;
    }

    /**
     * Знаходить всі публічні колекції з пагінацією.
     */
    @Override
    public List<Collection> findPublicCollections(int offset, int limit) {
        validatePagination(offset, limit);
        return collectionRepository.findPublicCollections(offset, limit);
    }

    /**
     * Знаходить публічні колекції за назвою.
     */
    @Override
    public List<Collection> findPublicCollectionsByName(String name) {
        new ValidationHelper()
                .notEmpty("name", name)
                .throwIfHasErrors();
        return collectionRepository.findPublicCollectionsByName(name);
    }

    /**
     * Знаходить публічні колекції за частковою відповідністю назви.
     */
    @Override
    public List<Collection> findPublicCollectionsByPartialName(String partialName) {
        new ValidationHelper()
                .notEmpty("partialName", partialName)
                .throwIfHasErrors();
        return collectionRepository.findPublicCollectionsByPartialName(partialName);
    }

    /**
     * Підраховує кількість публічних колекцій.
     */
    @Override
    public long countPublicCollections() {
        return collectionRepository.countPublicCollections();
    }

    /**
     * Перевіряє, чи існує публічна колекція з такою назвою.
     */
    @Override
    public boolean existsPublicCollectionByName(String name) {
        new ValidationHelper()
                .notEmpty("name", name)
                .throwIfHasErrors();
        return collectionRepository.existsPublicCollectionByName(name);
    }

    /**
     * Перевіряє, чи є колекція публічною.
     */
    @Override
    public boolean isPublicCollection(UUID collectionId) {
        new ValidationHelper()
                .validUuid("collectionId", collectionId)
                .throwIfHasErrors();

        Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);
        if (!collectionOpt.isPresent()) {
            throw new ValidationException("Колекція з ідентифікатором " + collectionId + " не існує.");
        }

        return collectionOpt.get().getUserId() == null;
    }

    /**
     * Знаходить найпопулярніші публічні колекції (за кількістю аудіокниг).
     */
    @Override
    public List<Collection> findMostPopularPublicCollections(int limit) {
        new ValidationHelper()
                .positive("limit", limit)
                .throwIfHasErrors();
        return collectionRepository.findMostPopularPublicCollections(limit);
    }

    /**
     * Знаходить нещодавно створені публічні колекції.
     */
    @Override
    public List<Collection> findRecentlyCreatedPublicCollections(int limit) {
        new ValidationHelper()
                .positive("limit", limit)
                .throwIfHasErrors();
        return collectionRepository.findRecentlyCreatedPublicCollections(limit);
    }
}
