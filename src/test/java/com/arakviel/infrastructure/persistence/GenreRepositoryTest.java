package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.GenreRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
class GenreRepositoryTest {

    private final GenreRepository genreRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;
    private final PersistenceContext persistenceContext;

    @Autowired
    public GenreRepositoryTest(
            GenreRepository genreRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool,
            PersistenceContext persistenceContext) {
        this.genreRepository = genreRepository;
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
    void shouldSaveAndRetrieveGenreByNameWhenPersisted() {
        // Arrange
        Genre genre = new Genre(UUID.randomUUID(), "Science Fiction", "Futuristic and space-related stories");
        persistenceContext.registerNew(genre);

        // Act
        persistenceContext.commit();
        List<Genre> genres = genreRepository.findByName("Science Fiction");

        // Assert
        assertThat(genres).hasSize(1);
        assertThat(genres.getFirst())
                .extracting(Genre::getName, Genre::getDescription)
                .containsExactly("Science Fiction", "Futuristic and space-related stories");
    }

    @Test
    void shouldUpdateGenreDescriptionWhenModifiedAndPersisted() {
        // Arrange
        Genre genre = new Genre(UUID.randomUUID(), "Fantasy", "Old description");
        persistenceContext.registerNew(genre);
        persistenceContext.commit();

        // Act
        genre.setDescription("Magical and mythical stories");
        persistenceContext.registerUpdated(genre.getId(), genre);
        persistenceContext.commit();

        Genre updatedGenre = genreRepository.findById(genre.getId()).orElse(null);

        // Assert
        assertThat(updatedGenre).isNotNull();
        assertThat(updatedGenre.getDescription()).isEqualTo("Magical and mythical stories");
    }

    @Test
    void shouldFindAudiobooksByGenreIdWhenAudiobooksExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        Author author = new Author(authorId, "Mystery", "Writer", "Crime fiction author", null);
        Genre genre = new Genre(genreId, "Mystery", "Crime and detective stories");
        Audiobook audiobook1 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "The Detective", 7200, 2020, "Crime thriller", null);
        Audiobook audiobook2 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Murder Case", 8400, 2021, "Police procedural", null);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.commit();

        // Act
        List<Audiobook> audiobooks = genreRepository.findAudiobooksByGenreId(genreId);

        // Assert
        assertThat(audiobooks).hasSize(2);
        assertThat(audiobooks)
                .extracting(Audiobook::getTitle)
                .containsExactlyInAnyOrder("The Detective", "Murder Case");
    }

    @Test
    void shouldReturnEmptyListWhenNoAudiobooksForGenreId() {
        // Arrange
        UUID genreId = UUID.randomUUID();
        Genre genre = new Genre(genreId, "Empty Genre", "No audiobooks here");
        persistenceContext.registerNew(genre);
        persistenceContext.commit();

        // Act
        List<Audiobook> audiobooks = genreRepository.findAudiobooksByGenreId(genreId);

        // Assert
        assertThat(audiobooks).isEmpty();
    }

    @Test
    void shouldFindGenresByPartialNameWhenMatchesExist() {
        // Arrange
        Genre genre1 = new Genre(UUID.randomUUID(), "Science Fiction", "Sci-fi stories");
        Genre genre2 = new Genre(UUID.randomUUID(), "Science Fantasy", "Mix of science and fantasy");
        Genre genre3 = new Genre(UUID.randomUUID(), "Romance", "Love stories");
        persistenceContext.registerNew(genre1);
        persistenceContext.registerNew(genre2);
        persistenceContext.registerNew(genre3);
        persistenceContext.commit();

        // Act
        List<Genre> genres = genreRepository.findByPartialName("Science");

        // Assert
        assertThat(genres).hasSize(2);
        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Science Fiction", "Science Fantasy");
    }

    @Test
    void shouldReturnEmptyListWhenNoGenresMatchPartialName() {
        // Arrange
        Genre genre = new Genre(UUID.randomUUID(), "Horror", "Scary stories");
        persistenceContext.registerNew(genre);
        persistenceContext.commit();

        // Act
        List<Genre> genres = genreRepository.findByPartialName("Comedy");

        // Assert
        assertThat(genres).isEmpty();
    }

    @Test
    void shouldCountAudiobooksByGenreIdWhenAudiobooksExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        Author author = new Author(authorId, "Adventure", "Writer", "Adventure stories author", null);
        Genre genre = new Genre(genreId, "Adventure", "Action and adventure stories");
        Audiobook audiobook1 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Treasure Hunt", 9000, 2019, "Pirate adventure", null);
        Audiobook audiobook2 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Mountain Climb", 6600, 2020, "Climbing adventure", null);
        Audiobook audiobook3 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Ocean Voyage", 10800, 2021, "Sea adventure", null);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.registerNew(audiobook3);
        persistenceContext.commit();

        // Act
        long count = genreRepository.countAudiobooksByGenreId(genreId);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroWhenNoAudiobooksForGenreId() {
        // Arrange
        UUID genreId = UUID.randomUUID();
        Genre genre = new Genre(genreId, "Empty Count", "No audiobooks");
        persistenceContext.registerNew(genre);
        persistenceContext.commit();

        // Act
        long count = genreRepository.countAudiobooksByGenreId(genreId);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldCheckExistsByNameWhenGenreExists() {
        // Arrange
        Genre genre = new Genre(UUID.randomUUID(), "Existing Genre", "This genre exists");
        persistenceContext.registerNew(genre);
        persistenceContext.commit();

        // Act
        boolean exists = genreRepository.existsByName("Existing Genre");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenGenreDoesNotExistByName() {
        // Act
        boolean exists = genreRepository.existsByName("Non Existing Genre");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldDeleteGenreAndVerifyAbsence() {
        // Arrange
        UUID genreId = UUID.randomUUID();
        Genre genre = new Genre(genreId, "Delete Me", "This genre will be deleted");
        persistenceContext.registerNew(genre);
        persistenceContext.commit();

        // Act
        persistenceContext.registerDeleted(genre);
        persistenceContext.commit();

        Optional<Genre> deletedGenre = genreRepository.findById(genreId);

        // Assert
        assertThat(deletedGenre).isEmpty();
    }

    @Test
    void shouldSaveMultipleGenresAndRetrieveAll() {
        // Arrange
        Genre genre1 = new Genre(UUID.randomUUID(), "Drama", "Dramatic stories");
        Genre genre2 = new Genre(UUID.randomUUID(), "Comedy", "Funny stories");
        persistenceContext.registerNew(genre1);
        persistenceContext.registerNew(genre2);
        persistenceContext.commit();

        // Act
        List<Genre> genres = genreRepository.findAll();

        // Assert
        assertThat(genres).hasSize(2);
        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Drama", "Comedy");
    }
}
