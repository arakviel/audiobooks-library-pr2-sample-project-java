package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тести для функціоналу публічних колекцій в CollectionRepository.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicCollectionRepositoryTest {

    private ApplicationContext context;
    private CollectionRepository collectionRepository;
    private PersistenceContext persistenceContext;
    private ConnectionPool connectionPool;
    private PersistenceInitializer persistenceInitializer;

    @BeforeAll
    void setUpAll() {
        context = new AnnotationConfigApplicationContext(InfrastructureConfig.class);
        collectionRepository = context.getBean(CollectionRepository.class);
        persistenceContext = context.getBean(PersistenceContext.class);
        connectionPool = context.getBean(ConnectionPool.class);
        persistenceInitializer = context.getBean(PersistenceInitializer.class);

        persistenceInitializer.init();
    }

    @AfterAll
    void tearDownAll() {
        connectionPool.shutdown();
    }

    @BeforeEach
    void setUp() {
        persistenceInitializer.clearData();
    }

    @AfterEach
    void tearDown() {
        persistenceInitializer.clearData();
    }

    // ========== FIND PUBLIC COLLECTIONS TESTS ==========

    @Test
    void shouldFindPublicCollectionsWithPagination() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "password", "test@example.com", null);
        persistenceContext.registerNew(user);

        Collection publicCollection1 = new Collection(UUID.randomUUID(), null, "Публічна колекція 1", LocalDateTime.now());
        Collection publicCollection2 = new Collection(UUID.randomUUID(), null, "Публічна колекція 2", LocalDateTime.now());
        Collection privateCollection = new Collection(UUID.randomUUID(), userId, "Приватна колекція", LocalDateTime.now());

        persistenceContext.registerNew(publicCollection1);
        persistenceContext.registerNew(publicCollection2);
        persistenceContext.registerNew(privateCollection);
        persistenceContext.commit();

        // Act
        List<Collection> result = collectionRepository.findPublicCollections(0, 10);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(collection -> collection.getUserId() == null);
        assertThat(result)
                .extracting(Collection::getName)
                .containsExactlyInAnyOrder("Публічна колекція 1", "Публічна колекція 2");
    }

    @Test
    void shouldFindPublicCollectionsByName() {
        // Arrange
        Collection publicCollection = new Collection(UUID.randomUUID(), null, "Фантастика", LocalDateTime.now());
        Collection anotherPublicCollection = new Collection(UUID.randomUUID(), null, "Детективи", LocalDateTime.now());

        persistenceContext.registerNew(publicCollection);
        persistenceContext.registerNew(anotherPublicCollection);
        persistenceContext.commit();

        // Act
        List<Collection> result = collectionRepository.findPublicCollectionsByName("Фантастика");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Фантастика");
        assertThat(result.get(0).getUserId()).isNull();
    }

    @Test
    void shouldFindPublicCollectionsByPartialName() {
        // Arrange
        Collection publicCollection1 = new Collection(UUID.randomUUID(), null, "Наукова фантастика", LocalDateTime.now());
        Collection publicCollection2 = new Collection(UUID.randomUUID(), null, "Фентезі", LocalDateTime.now());
        Collection publicCollection3 = new Collection(UUID.randomUUID(), null, "Детективи", LocalDateTime.now());

        persistenceContext.registerNew(publicCollection1);
        persistenceContext.registerNew(publicCollection2);
        persistenceContext.registerNew(publicCollection3);
        persistenceContext.commit();

        // Act
        List<Collection> result = collectionRepository.findPublicCollectionsByPartialName("фантаст");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result)
                .extracting(Collection::getName)
                .containsExactly("Наукова фантастика");
        assertThat(result).allMatch(collection -> collection.getUserId() == null);
    }

    @Test
    void shouldReturnEmptyListWhenNoPublicCollectionsFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "password", "test@example.com", null);
        persistenceContext.registerNew(user);

        Collection privateCollection = new Collection(UUID.randomUUID(), userId, "Приватна колекція", LocalDateTime.now());
        persistenceContext.registerNew(privateCollection);
        persistenceContext.commit();

        // Act
        List<Collection> result = collectionRepository.findPublicCollections(0, 10);

        // Assert
        assertThat(result).isEmpty();
    }

    // ========== COUNT TESTS ==========

    @Test
    void shouldCountPublicCollections() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "password", "test@example.com", null);
        persistenceContext.registerNew(user);

        Collection publicCollection1 = new Collection(UUID.randomUUID(), null, "Публічна 1", LocalDateTime.now());
        Collection publicCollection2 = new Collection(UUID.randomUUID(), null, "Публічна 2", LocalDateTime.now());
        Collection publicCollection3 = new Collection(UUID.randomUUID(), null, "Публічна 3", LocalDateTime.now());
        Collection privateCollection = new Collection(UUID.randomUUID(), userId, "Приватна", LocalDateTime.now());

        persistenceContext.registerNew(publicCollection1);
        persistenceContext.registerNew(publicCollection2);
        persistenceContext.registerNew(publicCollection3);
        persistenceContext.registerNew(privateCollection);
        persistenceContext.commit();

        // Act
        long result = collectionRepository.countPublicCollections();

        // Assert
        assertThat(result).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroWhenNoPublicCollections() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "password", "test@example.com", null);
        persistenceContext.registerNew(user);

        Collection privateCollection = new Collection(UUID.randomUUID(), userId, "Приватна", LocalDateTime.now());
        persistenceContext.registerNew(privateCollection);
        persistenceContext.commit();

        // Act
        long result = collectionRepository.countPublicCollections();

        // Assert
        assertThat(result).isEqualTo(0);
    }

    // ========== EXISTS TESTS ==========

    @Test
    void shouldReturnTrueWhenPublicCollectionExists() {
        // Arrange
        Collection publicCollection = new Collection(UUID.randomUUID(), null, "Унікальна назва", LocalDateTime.now());
        persistenceContext.registerNew(publicCollection);
        persistenceContext.commit();

        // Act
        boolean result = collectionRepository.existsPublicCollectionByName("Унікальна назва");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPublicCollectionDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "password", "test@example.com", null);
        persistenceContext.registerNew(user);

        // Створюємо приватну колекцію з такою ж назвою
        Collection privateCollection = new Collection(UUID.randomUUID(), userId, "Тестова назва", LocalDateTime.now());
        persistenceContext.registerNew(privateCollection);
        persistenceContext.commit();

        // Act
        boolean result = collectionRepository.existsPublicCollectionByName("Тестова назва");

        // Assert
        assertThat(result).isFalse(); // Приватна колекція не рахується
    }

    @Test
    void shouldReturnFalseWhenNoCollectionsExist() {
        // Act
        boolean result = collectionRepository.existsPublicCollectionByName("Неіснуюча назва");

        // Assert
        assertThat(result).isFalse();
    }

    // ========== POPULAR COLLECTIONS TESTS ==========

    @Test
    void shouldFindMostPopularPublicCollections() {
        // Arrange
        Collection publicCollection1 = new Collection(UUID.randomUUID(), null, "Популярна 1", LocalDateTime.now());
        Collection publicCollection2 = new Collection(UUID.randomUUID(), null, "Популярна 2", LocalDateTime.now());
        Collection publicCollection3 = new Collection(UUID.randomUUID(), null, "Популярна 3", LocalDateTime.now());

        persistenceContext.registerNew(publicCollection1);
        persistenceContext.registerNew(publicCollection2);
        persistenceContext.registerNew(publicCollection3);
        persistenceContext.commit();

        // Act
        List<Collection> result = collectionRepository.findMostPopularPublicCollections(2);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(collection -> collection.getUserId() == null);
    }

    // ========== RECENTLY CREATED TESTS ==========

    @Test
    void shouldFindRecentlyCreatedPublicCollections() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Collection oldCollection = new Collection(UUID.randomUUID(), null, "Стара колекція", now.minusDays(5));
        Collection recentCollection1 = new Collection(UUID.randomUUID(), null, "Нова колекція 1", now.minusHours(1));
        Collection recentCollection2 = new Collection(UUID.randomUUID(), null, "Нова колекція 2", now);

        persistenceContext.registerNew(oldCollection);
        persistenceContext.registerNew(recentCollection1);
        persistenceContext.registerNew(recentCollection2);
        persistenceContext.commit();

        // Act
        List<Collection> result = collectionRepository.findRecentlyCreatedPublicCollections(2);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(collection -> collection.getUserId() == null);
        // Перевіряємо, що результати відсортовані за датою створення (найновіші спочатку)
        assertThat(result.get(0).getCreatedAt()).isAfterOrEqualTo(result.get(1).getCreatedAt());
    }

    @Test
    void shouldRespectLimitInRecentlyCreatedPublicCollections() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 5; i++) {
            Collection collection = new Collection(UUID.randomUUID(), null, "Колекція " + i, now.minusHours(i));
            persistenceContext.registerNew(collection);
        }
        persistenceContext.commit();

        // Act
        List<Collection> result = collectionRepository.findRecentlyCreatedPublicCollections(3);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(collection -> collection.getUserId() == null);
    }
}
