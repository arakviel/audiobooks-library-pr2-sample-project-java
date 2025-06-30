package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.Genre;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserRepositoryTest {

    private final UserRepository userRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;
    private final PersistenceContext persistenceContext;

    @Autowired
    public UserRepositoryTest(
            UserRepository userRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool,
            PersistenceContext persistenceContext) {
        this.userRepository = userRepository;
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
    void shouldSaveAndRetrieveUserByUsernameWhenPersisted() {
        // Arrange
        User user = new User(UUID.randomUUID(), "john_doe", "hashedPassword123", "john@example.com", null);
        persistenceContext.registerNew(user);

        // Act
        persistenceContext.commit();
        List<User> users = userRepository.findByUsername("john_doe");

        // Assert
        assertThat(users).hasSize(1);
        assertThat(users.getFirst())
                .extracting(User::getUsername, User::getEmail)
                .containsExactly("john_doe", "john@example.com");
    }

    @Test
    void shouldFindUserByEmailWhenUserExists() {
        // Arrange
        User user = new User(UUID.randomUUID(), "jane_smith", "hashedPassword456", "jane@example.com", "/avatars/jane.jpg");
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        List<User> users = userRepository.findByEmail("jane@example.com");

        // Assert
        assertThat(users).hasSize(1);
        assertThat(users.getFirst())
                .extracting(User::getUsername, User::getAvatarPath)
                .containsExactly("jane_smith", "/avatars/jane.jpg");
    }

    @Test
    void shouldReturnEmptyListWhenUserNotFoundByUsername() {
        // Arrange
        User user = new User(UUID.randomUUID(), "existing_user", "password", "user@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        List<User> users = userRepository.findByUsername("non_existing_user");

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    void shouldFindUsersByPartialUsernameWhenMatchesExist() {
        // Arrange
        User user1 = new User(UUID.randomUUID(), "john_doe", "password1", "john@example.com", null);
        User user2 = new User(UUID.randomUUID(), "john_smith", "password2", "johnsmith@example.com", null);
        User user3 = new User(UUID.randomUUID(), "mary_jane", "password3", "mary@example.com", null);
        persistenceContext.registerNew(user1);
        persistenceContext.registerNew(user2);
        persistenceContext.registerNew(user3);
        persistenceContext.commit();

        // Act
        List<User> users = userRepository.findByPartialUsername("john");

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("john_doe", "john_smith");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersMatchPartialUsername() {
        // Arrange
        User user = new User(UUID.randomUUID(), "alice_wonder", "password", "alice@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        List<User> users = userRepository.findByPartialUsername("bob");

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    void shouldFindCollectionsByUserIdWhenCollectionsExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "collector", "password", "collector@example.com", null);
        Collection collection1 = new Collection(UUID.randomUUID(), userId, "My Favorites", LocalDateTime.now());
        Collection collection2 = new Collection(UUID.randomUUID(), userId, "Sci-Fi Collection", LocalDateTime.now());
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(collection1);
        persistenceContext.registerNew(collection2);
        persistenceContext.commit();

        // Act
        List<Collection> collections = userRepository.findCollectionsByUserId(userId);

        // Assert
        assertThat(collections).hasSize(2);
        assertThat(collections)
                .extracting(Collection::getName)
                .containsExactlyInAnyOrder("My Favorites", "Sci-Fi Collection");
    }

    @Test
    void shouldReturnEmptyListWhenNoCollectionsForUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "no_collections", "password", "nocollections@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        List<Collection> collections = userRepository.findCollectionsByUserId(userId);

        // Assert
        assertThat(collections).isEmpty();
    }

    @Test
    void shouldFindListeningProgressByUserIdWhenProgressExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        User user = new User(userId, "listener", "password", "listener@example.com", null);
        Author author = new Author(authorId, "Test", "Author", "Bio", null);
        Genre genre = new Genre(genreId, "Test Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Test Book", 3600, 2023, "Description", null);
        ListeningProgress progress = new ListeningProgress(UUID.randomUUID(), userId, audiobookId, 1800, LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(progress);
        persistenceContext.commit();

        // Act
        List<ListeningProgress> progressList = userRepository.findListeningProgressByUserId(userId);

        // Assert
        assertThat(progressList).hasSize(1);
        assertThat(progressList.getFirst().getPosition()).isEqualTo(1800);
    }

    @Test
    void shouldCountCollectionsByUserIdWhenCollectionsExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "counter", "password", "counter@example.com", null);
        Collection collection1 = new Collection(UUID.randomUUID(), userId, "Collection 1", LocalDateTime.now());
        Collection collection2 = new Collection(UUID.randomUUID(), userId, "Collection 2", LocalDateTime.now());
        Collection collection3 = new Collection(UUID.randomUUID(), userId, "Collection 3", LocalDateTime.now());
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(collection1);
        persistenceContext.registerNew(collection2);
        persistenceContext.registerNew(collection3);
        persistenceContext.commit();

        // Act
        long count = userRepository.countCollectionsByUserId(userId);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroWhenNoCollectionsForUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "no_collections_counter", "password", "nocounter@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        long count = userRepository.countCollectionsByUserId(userId);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldCountListeningProgressByUserIdWhenProgressExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobook1Id = UUID.randomUUID();
        UUID audiobook2Id = UUID.randomUUID();
        
        User user = new User(userId, "progress_counter", "password", "progress@example.com", null);
        Author author = new Author(authorId, "Progress", "Author", "Bio", null);
        Genre genre = new Genre(genreId, "Progress Genre", "Description");
        Audiobook audiobook1 = new Audiobook(audiobook1Id, authorId, genreId, "Book 1", 3600, 2023, "Description", null);
        Audiobook audiobook2 = new Audiobook(audiobook2Id, authorId, genreId, "Book 2", 7200, 2023, "Description", null);
        ListeningProgress progress1 = new ListeningProgress(UUID.randomUUID(), userId, audiobook1Id, 1800, LocalDateTime.now());
        ListeningProgress progress2 = new ListeningProgress(UUID.randomUUID(), userId, audiobook2Id, 3600, LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.registerNew(progress1);
        persistenceContext.registerNew(progress2);
        persistenceContext.commit();

        // Act
        long count = userRepository.countListeningProgressByUserId(userId);

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCheckExistsByUsernameWhenUserExists() {
        // Arrange
        User user = new User(UUID.randomUUID(), "existing_username", "password", "existing@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        boolean exists = userRepository.existsByUsername("existing_username");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldCheckExistsByEmailWhenUserExists() {
        // Arrange
        User user = new User(UUID.randomUUID(), "email_user", "password", "existing@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        boolean exists = userRepository.existsByEmail("existing@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExistByUsername() {
        // Act
        boolean exists = userRepository.existsByUsername("non_existing_user");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldUpdateUserEmailWhenModifiedAndPersisted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "update_user", "password", "old@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        user.setEmail("new@example.com");
        persistenceContext.registerUpdated(user.getId(), user);
        persistenceContext.commit();

        User updatedUser = userRepository.findById(userId).orElse(null);

        // Assert
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldDeleteUserAndVerifyAbsence() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "delete_user", "password", "delete@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        persistenceContext.registerDeleted(user);
        persistenceContext.commit();

        Optional<User> deletedUser = userRepository.findById(userId);

        // Assert
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void shouldSaveMultipleUsersAndRetrieveAll() {
        // Arrange
        User user1 = new User(UUID.randomUUID(), "user_one", "password1", "one@example.com", null);
        User user2 = new User(UUID.randomUUID(), "user_two", "password2", "two@example.com", null);
        persistenceContext.registerNew(user1);
        persistenceContext.registerNew(user2);
        persistenceContext.commit();

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("user_one", "user_two");
    }
}
