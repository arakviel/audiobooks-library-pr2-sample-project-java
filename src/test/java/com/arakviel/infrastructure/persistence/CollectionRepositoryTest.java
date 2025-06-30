package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.Genre;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
class CollectionRepositoryTest {

    private final CollectionRepository collectionRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;
    private final PersistenceContext persistenceContext;

    @Autowired
    public CollectionRepositoryTest(
            CollectionRepository collectionRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool,
            PersistenceContext persistenceContext) {
        this.collectionRepository = collectionRepository;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
        this.persistenceContext = persistenceContext;
    }

    @BeforeEach
    void setUp() {
        persistenceContext.clearAndClose(); // Clear context
        persistenceInitializer.init(false); // Initialize without DML
        persistenceInitializer.clearData(); // Clear all data for isolation
        persistenceContext.activate(); // Activate context for this test
    }

    @AfterEach
    void tearDown() {
        persistenceContext.clearAndClose(); // Clear context after each test
    }

    @AfterAll
    void closeResources() {
        connectionPool.shutdown();
    }

    @Test
    void shouldSaveAndRetrieveCollectionByIdWhenPersisted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        User user = new User(userId, "collector", "password", "collector@example.com", null);
        Collection collection = new Collection(collectionId, userId, "My Favorite Books", LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(collection);

        // Act
        persistenceContext.commit();
        Optional<Collection> foundCollection = collectionRepository.findById(collectionId);

        // Assert
        assertThat(foundCollection).isPresent();
        assertThat(foundCollection.get())
                .extracting(Collection::getName, Collection::getUserId)
                .containsExactly("My Favorite Books", userId);
    }

    @Test
    void shouldFindCollectionsByUserIdWhenCollectionsExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "book_lover", "password", "booklover@example.com", null);
        Collection collection1 = new Collection(UUID.randomUUID(), userId, "Science Fiction", LocalDateTime.now());
        Collection collection2 = new Collection(UUID.randomUUID(), userId, "Mystery Novels", LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(collection1);
        persistenceContext.registerNew(collection2);
        persistenceContext.commit();

        // Act
        List<Collection> collections = collectionRepository.findByUserId(userId);

        // Assert
        assertThat(collections).hasSize(2);
        assertThat(collections)
                .extracting(Collection::getName)
                .containsExactlyInAnyOrder("Science Fiction", "Mystery Novels");
    }

    @Test
    void shouldReturnEmptyListWhenNoCollectionsForUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "no_collections", "password", "nocollections@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        List<Collection> collections = collectionRepository.findByUserId(userId);

        // Assert
        assertThat(collections).isEmpty();
    }

    @Test
    void shouldFindCollectionsByNameWhenCollectionsExist() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        User user1 = new User(userId1, "user1", "password", "user1@example.com", null);
        User user2 = new User(userId2, "user2", "password", "user2@example.com", null);
        Collection collection1 = new Collection(UUID.randomUUID(), userId1, "Favorites", LocalDateTime.now());
        Collection collection2 = new Collection(UUID.randomUUID(), userId2, "Favorites", LocalDateTime.now());
        
        persistenceContext.registerNew(user1);
        persistenceContext.registerNew(user2);
        persistenceContext.registerNew(collection1);
        persistenceContext.registerNew(collection2);
        persistenceContext.commit();

        // Act
        List<Collection> collections = collectionRepository.findByName("Favorites");

        // Assert
        assertThat(collections).hasSize(2);
        assertThat(collections)
                .extracting(Collection::getUserId)
                .containsExactlyInAnyOrder(userId1, userId2);
    }

    @Test
    void shouldFindAudiobooksByCollectionIdWhenAudiobooksExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        UUID audiobook1Id = UUID.randomUUID();
        UUID audiobook2Id = UUID.randomUUID();
        
        User user = new User(userId, "audiobook_collector", "password", "collector@example.com", null);
        Author author = new Author(authorId, "Famous", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Fiction", "Fictional stories");
        Collection collection = new Collection(collectionId, userId, "Best Fiction", LocalDateTime.now());
        Audiobook audiobook1 = new Audiobook(audiobook1Id, authorId, genreId, "Great Novel", 10800, 2020, "Amazing story", null);
        Audiobook audiobook2 = new Audiobook(audiobook2Id, authorId, genreId, "Another Novel", 14400, 2021, "Another story", null);
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(collection);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.commit();

        // Attach audiobooks to collection
        collectionRepository.attachAudiobookToCollection(collectionId, audiobook1Id);
        collectionRepository.attachAudiobookToCollection(collectionId, audiobook2Id);

        // Act
        List<Audiobook> audiobooks = collectionRepository.findAudiobooksByCollectionId(collectionId);

        // Assert
        assertThat(audiobooks).hasSize(2);
        assertThat(audiobooks)
                .extracting(Audiobook::getTitle)
                .containsExactlyInAnyOrder("Great Novel", "Another Novel");
    }

    @Test
    void shouldFindCollectionsByAudiobookIdWhenCollectionsExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        UUID collection1Id = UUID.randomUUID();
        UUID collection2Id = UUID.randomUUID();
        
        User user = new User(userId, "multi_collector", "password", "multi@example.com", null);
        Author author = new Author(authorId, "Popular", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Adventure", "Adventure stories");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Popular Book", 12600, 2022, "Popular story", null);
        Collection collection1 = new Collection(collection1Id, userId, "Adventure Collection", LocalDateTime.now());
        Collection collection2 = new Collection(collection2Id, userId, "Top Picks", LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(collection1);
        persistenceContext.registerNew(collection2);
        persistenceContext.commit();

        // Attach audiobook to multiple collections
        collectionRepository.attachAudiobookToCollection(collection1Id, audiobookId);
        collectionRepository.attachAudiobookToCollection(collection2Id, audiobookId);

        // Act
        List<Collection> collections = collectionRepository.findByAudiobookId(audiobookId);

        // Assert
        assertThat(collections).hasSize(2);
        assertThat(collections)
                .extracting(Collection::getName)
                .containsExactlyInAnyOrder("Adventure Collection", "Top Picks");
    }

    @Test
    void shouldAttachAndDetachAudiobookFromCollection() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        User user = new User(userId, "attach_detach_user", "password", "attachdetach@example.com", null);
        Author author = new Author(authorId, "Test", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Test Genre", "Test description");
        Collection collection = new Collection(collectionId, userId, "Test Collection", LocalDateTime.now());
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Test Book", 7200, 2023, "Test description", null);
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(collection);
        persistenceContext.registerNew(audiobook);
        persistenceContext.commit();

        // Act - Attach
        collectionRepository.attachAudiobookToCollection(collectionId, audiobookId);
        List<Audiobook> audiobooksAfterAttach = collectionRepository.findAudiobooksByCollectionId(collectionId);

        // Assert - Attached
        assertThat(audiobooksAfterAttach).hasSize(1);
        assertThat(audiobooksAfterAttach.getFirst().getTitle()).isEqualTo("Test Book");

        // Act - Detach
        collectionRepository.detachAudiobookFromCollection(collectionId, audiobookId);
        List<Audiobook> audiobooksAfterDetach = collectionRepository.findAudiobooksByCollectionId(collectionId);

        // Assert - Detached
        assertThat(audiobooksAfterDetach).isEmpty();
    }

    @Test
    void shouldCountAudiobooksByCollectionIdWhenAudiobooksExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        
        User user = new User(userId, "count_user", "password", "count@example.com", null);
        Author author = new Author(authorId, "Count", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Count Genre", "Count description");
        Collection collection = new Collection(collectionId, userId, "Count Collection", LocalDateTime.now());
        Audiobook audiobook1 = new Audiobook(UUID.randomUUID(), authorId, genreId, "Book 1", 3600, 2023, "Description 1", null);
        Audiobook audiobook2 = new Audiobook(UUID.randomUUID(), authorId, genreId, "Book 2", 5400, 2023, "Description 2", null);
        Audiobook audiobook3 = new Audiobook(UUID.randomUUID(), authorId, genreId, "Book 3", 7200, 2023, "Description 3", null);
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(collection);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.registerNew(audiobook3);
        persistenceContext.commit();

        // Attach audiobooks to collection
        collectionRepository.attachAudiobookToCollection(collectionId, audiobook1.getId());
        collectionRepository.attachAudiobookToCollection(collectionId, audiobook2.getId());
        collectionRepository.attachAudiobookToCollection(collectionId, audiobook3.getId());

        // Act
        long count = collectionRepository.countAudiobooksByCollectionId(collectionId);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroWhenNoAudiobooksInCollection() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        User user = new User(userId, "empty_collection_user", "password", "empty@example.com", null);
        Collection collection = new Collection(collectionId, userId, "Empty Collection", LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(collection);
        persistenceContext.commit();

        // Act
        long count = collectionRepository.countAudiobooksByCollectionId(collectionId);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldClearCollectionWhenAudiobooksExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        
        User user = new User(userId, "clear_user", "password", "clear@example.com", null);
        Author author = new Author(authorId, "Clear", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Clear Genre", "Clear description");
        Collection collection = new Collection(collectionId, userId, "Clear Collection", LocalDateTime.now());
        Audiobook audiobook = new Audiobook(UUID.randomUUID(), authorId, genreId, "Clear Book", 3600, 2023, "Clear description", null);
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(collection);
        persistenceContext.registerNew(audiobook);
        persistenceContext.commit();

        // Attach audiobook to collection
        collectionRepository.attachAudiobookToCollection(collectionId, audiobook.getId());

        // Verify audiobook is attached
        long countBefore = collectionRepository.countAudiobooksByCollectionId(collectionId);
        assertThat(countBefore).isEqualTo(1);

        // Act
        collectionRepository.clearCollection(collectionId);

        // Assert
        long countAfter = collectionRepository.countAudiobooksByCollectionId(collectionId);
        assertThat(countAfter).isEqualTo(0);
    }

    @Test
    void shouldUpdateCollectionNameWhenModifiedAndPersisted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        User user = new User(userId, "update_user", "password", "update@example.com", null);
        Collection collection = new Collection(collectionId, userId, "Old Name", LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(collection);
        persistenceContext.commit();

        // Act
        collection.setName("New Name");
        persistenceContext.registerUpdated(collection.getId(), collection);
        persistenceContext.commit();

        Collection updatedCollection = collectionRepository.findById(collectionId).orElse(null);

        // Assert
        assertThat(updatedCollection).isNotNull();
        assertThat(updatedCollection.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldDeleteCollectionAndVerifyAbsence() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        User user = new User(userId, "delete_user", "password", "delete@example.com", null);
        Collection collection = new Collection(collectionId, userId, "Delete Collection", LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(collection);
        persistenceContext.commit();

        // Act
        persistenceContext.registerDeleted(collection);
        persistenceContext.commit();

        Optional<Collection> deletedCollection = collectionRepository.findById(collectionId);

        // Assert
        assertThat(deletedCollection).isEmpty();
    }

    @Test
    void shouldSaveMultipleCollectionsAndRetrieveAll() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "multiple_collections_user", "password", "multiple@example.com", null);
        Collection collection1 = new Collection(UUID.randomUUID(), userId, "Collection One", LocalDateTime.now());
        Collection collection2 = new Collection(UUID.randomUUID(), userId, "Collection Two", LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(collection1);
        persistenceContext.registerNew(collection2);
        persistenceContext.commit();

        // Act
        List<Collection> collections = collectionRepository.findAll();

        // Assert
        assertThat(collections).hasSize(2);
        assertThat(collections)
                .extracting(Collection::getName)
                .containsExactlyInAnyOrder("Collection One", "Collection Two");
    }
}
