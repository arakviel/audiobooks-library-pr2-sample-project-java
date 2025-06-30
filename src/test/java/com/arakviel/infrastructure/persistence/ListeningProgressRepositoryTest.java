package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Genre;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class ListeningProgressRepositoryTest {

    private final ListeningProgressRepository listeningProgressRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;
    private final PersistenceContext persistenceContext;

    @Autowired
    public ListeningProgressRepositoryTest(
            ListeningProgressRepository listeningProgressRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool,
            PersistenceContext persistenceContext) {
        this.listeningProgressRepository = listeningProgressRepository;
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
    void shouldSaveAndRetrieveListeningProgressByIdWhenPersisted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        UUID progressId = UUID.randomUUID();
        
        User user = new User(userId, "listener", "password", "listener@example.com", null);
        Author author = new Author(authorId, "Progress", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Progress Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Progress Book", 7200, 2023, "Description", null);
        ListeningProgress progress = new ListeningProgress(progressId, userId, audiobookId, 3600, LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(progress);

        // Act
        persistenceContext.commit();
        Optional<ListeningProgress> foundProgress = listeningProgressRepository.findById(progressId);

        // Assert
        assertThat(foundProgress).isPresent();
        assertThat(foundProgress.get())
                .extracting(ListeningProgress::getUserId, ListeningProgress::getPosition)
                .containsExactly(userId, 3600);
    }

    @Test
    void shouldFindListeningProgressByUserIdWhenProgressExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobook1Id = UUID.randomUUID();
        UUID audiobook2Id = UUID.randomUUID();
        
        User user = new User(userId, "multi_listener", "password", "multilistener@example.com", null);
        Author author = new Author(authorId, "Multi", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Multi Genre", "Description");
        Audiobook audiobook1 = new Audiobook(audiobook1Id, authorId, genreId, "Book One", 3600, 2023, "Description", null);
        Audiobook audiobook2 = new Audiobook(audiobook2Id, authorId, genreId, "Book Two", 5400, 2023, "Description", null);
        ListeningProgress progress1 = new ListeningProgress(UUID.randomUUID(), userId, audiobook1Id, 1800, LocalDateTime.now());
        ListeningProgress progress2 = new ListeningProgress(UUID.randomUUID(), userId, audiobook2Id, 2700, LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.registerNew(progress1);
        persistenceContext.registerNew(progress2);
        persistenceContext.commit();

        // Act
        List<ListeningProgress> progressList = listeningProgressRepository.findByUserId(userId);

        // Assert
        assertThat(progressList).hasSize(2);
        assertThat(progressList)
                .extracting(ListeningProgress::getPosition)
                .containsExactlyInAnyOrder(1800, 2700);
    }

    @Test
    void shouldReturnEmptyListWhenNoProgressForUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "no_progress", "password", "noprogress@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        List<ListeningProgress> progressList = listeningProgressRepository.findByUserId(userId);

        // Assert
        assertThat(progressList).isEmpty();
    }

    @Test
    void shouldFindListeningProgressByAudiobookIdWhenProgressExists() {
        // Arrange
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        User user1 = new User(user1Id, "listener1", "password", "listener1@example.com", null);
        User user2 = new User(user2Id, "listener2", "password", "listener2@example.com", null);
        Author author = new Author(authorId, "Popular", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Popular Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Popular Book", 10800, 2023, "Description", null);
        ListeningProgress progress1 = new ListeningProgress(UUID.randomUUID(), user1Id, audiobookId, 2400, LocalDateTime.now());
        ListeningProgress progress2 = new ListeningProgress(UUID.randomUUID(), user2Id, audiobookId, 4800, LocalDateTime.now());
        
        persistenceContext.registerNew(user1);
        persistenceContext.registerNew(user2);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(progress1);
        persistenceContext.registerNew(progress2);
        persistenceContext.commit();

        // Act
        List<ListeningProgress> progressList = listeningProgressRepository.findByAudiobookId(audiobookId);

        // Assert
        assertThat(progressList).hasSize(2);
        assertThat(progressList)
                .extracting(ListeningProgress::getUserId)
                .containsExactlyInAnyOrder(user1Id, user2Id);
    }

    @Test
    void shouldReturnEmptyListWhenNoProgressForAudiobookId() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "No Progress", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "No Progress Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "No Progress Book", 7200, 2023, "Description", null);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.commit();

        // Act
        List<ListeningProgress> progressList = listeningProgressRepository.findByAudiobookId(audiobookId);

        // Assert
        assertThat(progressList).isEmpty();
    }

    @Test
    void shouldFindProgressByUserIdAndAudiobookIdWhenProgressExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        User user = new User(userId, "specific_listener", "password", "specific@example.com", null);
        Author author = new Author(authorId, "Specific", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Specific Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Specific Book", 9000, 2023, "Description", null);
        ListeningProgress progress = new ListeningProgress(UUID.randomUUID(), userId, audiobookId, 4500, LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(progress);
        persistenceContext.commit();

        // Act
        Optional<ListeningProgress> foundProgress = listeningProgressRepository.findByUserIdAndAudiobookId(userId, audiobookId);

        // Assert
        assertThat(foundProgress).isPresent();
        assertThat(foundProgress.get().getPosition()).isEqualTo(4500);
    }

    @Test
    void shouldReturnEmptyWhenNoProgressForUserIdAndAudiobookId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        User user = new User(userId, "no_specific_progress", "password", "nospecific@example.com", null);
        Author author = new Author(authorId, "No Specific", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "No Specific Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "No Specific Book", 7200, 2023, "Description", null);
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.commit();

        // Act
        Optional<ListeningProgress> foundProgress = listeningProgressRepository.findByUserIdAndAudiobookId(userId, audiobookId);

        // Assert
        assertThat(foundProgress).isEmpty();
    }

    @Test
    void shouldCountProgressByUserIdWhenProgressExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobook1Id = UUID.randomUUID();
        UUID audiobook2Id = UUID.randomUUID();
        UUID audiobook3Id = UUID.randomUUID();
        
        User user = new User(userId, "count_user", "password", "count@example.com", null);
        Author author = new Author(authorId, "Count", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Count Genre", "Description");
        Audiobook audiobook1 = new Audiobook(audiobook1Id, authorId, genreId, "Count Book 1", 3600, 2023, "Description", null);
        Audiobook audiobook2 = new Audiobook(audiobook2Id, authorId, genreId, "Count Book 2", 5400, 2023, "Description", null);
        Audiobook audiobook3 = new Audiobook(audiobook3Id, authorId, genreId, "Count Book 3", 7200, 2023, "Description", null);
        ListeningProgress progress1 = new ListeningProgress(UUID.randomUUID(), userId, audiobook1Id, 1800, LocalDateTime.now());
        ListeningProgress progress2 = new ListeningProgress(UUID.randomUUID(), userId, audiobook2Id, 2700, LocalDateTime.now());
        ListeningProgress progress3 = new ListeningProgress(UUID.randomUUID(), userId, audiobook3Id, 3600, LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.registerNew(audiobook3);
        persistenceContext.registerNew(progress1);
        persistenceContext.registerNew(progress2);
        persistenceContext.registerNew(progress3);
        persistenceContext.commit();

        // Act
        long count = listeningProgressRepository.countByUserId(userId);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroWhenNoProgressForUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "no_count_progress", "password", "nocount@example.com", null);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        // Act
        long count = listeningProgressRepository.countByUserId(userId);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldUpdateProgressPositionWhenModifiedAndPersisted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        UUID progressId = UUID.randomUUID();
        
        User user = new User(userId, "update_user", "password", "update@example.com", null);
        Author author = new Author(authorId, "Update", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Update Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Update Book", 10800, 2023, "Description", null);
        ListeningProgress progress = new ListeningProgress(progressId, userId, audiobookId, 2400, LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(progress);
        persistenceContext.commit();

        // Act
        progress.setPosition(4800);
        persistenceContext.registerUpdated(progress.getId(), progress);
        persistenceContext.commit();

        ListeningProgress updatedProgress = listeningProgressRepository.findById(progressId).orElse(null);

        // Assert
        assertThat(updatedProgress).isNotNull();
        assertThat(updatedProgress.getPosition()).isEqualTo(4800);
    }

    @Test
    void shouldDeleteProgressAndVerifyAbsence() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        UUID progressId = UUID.randomUUID();
        
        User user = new User(userId, "delete_user", "password", "delete@example.com", null);
        Author author = new Author(authorId, "Delete", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Delete Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Delete Book", 7200, 2023, "Description", null);
        ListeningProgress progress = new ListeningProgress(progressId, userId, audiobookId, 3600, LocalDateTime.now());
        
        persistenceContext.registerNew(user);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(progress);
        persistenceContext.commit();

        // Act
        persistenceContext.registerDeleted(progress);
        persistenceContext.commit();

        Optional<ListeningProgress> deletedProgress = listeningProgressRepository.findById(progressId);

        // Assert
        assertThat(deletedProgress).isEmpty();
    }

    @Test
    void shouldSaveMultipleProgressRecordsAndRetrieveAll() {
        // Arrange
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        User user1 = new User(user1Id, "multiple_user1", "password", "multiple1@example.com", null);
        User user2 = new User(user2Id, "multiple_user2", "password", "multiple2@example.com", null);
        Author author = new Author(authorId, "Multiple", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Multiple Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Multiple Book", 14400, 2023, "Description", null);
        ListeningProgress progress1 = new ListeningProgress(UUID.randomUUID(), user1Id, audiobookId, 3600, LocalDateTime.now());
        ListeningProgress progress2 = new ListeningProgress(UUID.randomUUID(), user2Id, audiobookId, 7200, LocalDateTime.now());
        
        persistenceContext.registerNew(user1);
        persistenceContext.registerNew(user2);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(progress1);
        persistenceContext.registerNew(progress2);
        persistenceContext.commit();

        // Act
        List<ListeningProgress> allProgress = listeningProgressRepository.findAll();

        // Assert
        assertThat(allProgress).hasSize(2);
        assertThat(allProgress)
                .extracting(ListeningProgress::getPosition)
                .containsExactlyInAnyOrder(3600, 7200);
    }
}

// Тепер створимо AudiobookFileRepositoryTest окремо
