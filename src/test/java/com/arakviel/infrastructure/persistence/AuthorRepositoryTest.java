package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.AuthorRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.junit.jupiter.api.AfterAll;
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
class AuthorRepositoryTest {

    private final AuthorRepository authorRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;
    private final PersistenceContext persistenceContext;

    @Autowired
    public AuthorRepositoryTest(
            AuthorRepository authorRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool,
            PersistenceContext persistenceContext) {
        this.authorRepository = authorRepository;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
        this.persistenceContext = persistenceContext;
    }

    @BeforeEach
    void setUp() {
        persistenceInitializer.init(false); // Initialize without DML
        persistenceInitializer.clearData(); // Clear all data for isolation
    }

    @AfterAll
    void closeResources() {
        connectionPool.shutdown();
    }

/*    @AfterAll
    void closeResources() {
        connectionPool.shutdown();
    }*/

    @Test
    void shouldSaveAndRetrieveAuthorByNameWhenPersisted() {
        // Arrange
        Author author = new Author(UUID.randomUUID(), "John", "Doe", "Bio", null);
        persistenceContext.registerNew(author);

        // Act
        persistenceContext.commit();
        List<Author> authors = authorRepository.findByName("John", "Doe");

        // Assert
        assertThat(authors).hasSize(1);
        assertThat(authors.getFirst())
                .extracting(Author::getFirstName, Author::getLastName)
                .containsExactly("John", "Doe");
    }

    @Test
    void shouldUpdateAuthorFirstNameWhenModifiedAndPersisted() {
        // Arrange
        Author author = new Author(UUID.randomUUID(), "Jane", "Smith", "Bio", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        author.setFirstName("Janet");
        persistenceContext.registerUpdated(author.getId(), author);
        persistenceContext.commit();

        Author updatedAuthor = authorRepository.findById(author.getId()).orElse(null);

        // Assert
        assertThat(updatedAuthor).isNotNull();
        assertThat(updatedAuthor.getFirstName()).isEqualTo("Janet");
    }

    @Test
    void shouldFindAudiobooksByAuthorIdWhenAudiobooksExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        Author author = new Author(authorId, "Taras", "Shevchenko", "Ukrainian poet", null);
        Genre genre = new Genre(genreId, "Poetry", null);
        Audiobook audiobook1 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Kobzar", 3600, 2023, "Poetry collection", null);
        Audiobook audiobook2 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Haydamaky", 5400, 2022, "Epic poem", null);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.commit();

        // Act
        List<Audiobook> audiobooks = authorRepository.findAudiobooksByAuthorId(authorId);

        // Assert
        assertThat(audiobooks).hasSize(2);
        assertThat(audiobooks)
                .extracting(Audiobook::getTitle)
                .containsExactlyInAnyOrder("Kobzar", "Haydamaky");
    }

    @Test
    void shouldReturnEmptyListWhenNoAudiobooksForAuthorId() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        Author author = new Author(authorId, "No", "Books", "No audiobooks", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        List<Audiobook> audiobooks = authorRepository.findAudiobooksByAuthorId(authorId);

        // Assert
        assertThat(audiobooks).isEmpty();
    }

    @Test
    void shouldFindAuthorsByPartialNameWhenMatchesExist() {
        // Arrange
        Author author1 = new Author(UUID.randomUUID(), "Olena", "Shevchenko", "Bio", null);
        Author author2 = new Author(UUID.randomUUID(), "Taras", "Shevchenko", "Bio", null);
        Author author3 = new Author(UUID.randomUUID(), "Ivan", "Franko", "Bio", null);
        persistenceContext.registerNew(author1);
        persistenceContext.registerNew(author2);
        persistenceContext.registerNew(author3);
        persistenceContext.commit();

        // Act
        List<Author> authors = authorRepository.findByPartialName("Shev");

        // Assert
        assertThat(authors).hasSize(2);
        assertThat(authors)
                .extracting(Author::getLastName)
                .containsExactlyInAnyOrder("Shevchenko", "Shevchenko");
    }

    @Test
    void shouldReturnEmptyListWhenNoAuthorsMatchPartialName() {
        // Arrange
        Author author = new Author(UUID.randomUUID(), "Olena", "Shevchenko", "Bio", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        List<Author> authors = authorRepository.findByPartialName("NonExistent");

        // Assert
        assertThat(authors).isEmpty();
    }

    @Test
    void shouldCountAudiobooksByAuthorIdWhenAudiobooksExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        Author author = new Author(authorId, "Lesya", "Ukrainka", "Ukrainian writer", null);
        Genre genre = new Genre(genreId, "Drama", null);
        Audiobook audiobook1 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Lisova Pisnya", 7200, 2021, "Drama", null);
        Audiobook audiobook2 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Boyarynya", 4800, 2020, "Historical drama", null);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.commit();

        // Act
        long count = authorRepository.countAudiobooksByAuthorId(authorId);

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroWhenNoAudiobooksForAuthorId() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        Author author = new Author(authorId, "No", "Books", "No audiobooks", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        long count = authorRepository.countAudiobooksByAuthorId(authorId);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldDeleteAuthorAndVerifyAbsence() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        Author author = new Author(authorId, "Mykhailo", "Kotsiubynsky", "Bio", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        persistenceContext.registerDeleted(author);
        persistenceContext.commit();

        Optional<Author> deletedAuthor = authorRepository.findById(authorId);

        // Assert
        assertThat(deletedAuthor).isEmpty();
    }

    @Test
    void shouldSaveMultipleAuthorsAndRetrieveAll() {
        // Arrange
        Author author1 = new Author(UUID.randomUUID(), "Vasyl", "Stefanyk", "Bio", null);
        Author author2 = new Author(UUID.randomUUID(), "Olha", "Kobylyanska", "Bio", null);
        persistenceContext.registerNew(author1);
        persistenceContext.registerNew(author2);
        persistenceContext.commit();

        // Act
        List<Author> authors = authorRepository.findAll();

        // Assert
        assertThat(authors).hasSize(2);
        assertThat(authors)
                .extracting(Author::getFirstName)
                .containsExactlyInAnyOrder("Vasyl", "Olha");
    }
}