package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.AuthorRepository;
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
    void shouldSaveAndRetrieveAuthorByNameWhenPersisted() {
        // Arrange
        Author author = new Author(UUID.randomUUID(), "Isaac", "Asimov", "Science fiction writer", "/avatars/asimov.jpg");
        persistenceContext.registerNew(author);

        // Act
        persistenceContext.commit();
        List<Author> authors = authorRepository.findByName("Isaac", "Asimov");

        // Assert
        assertThat(authors).hasSize(1);
        assertThat(authors.getFirst())
                .extracting(Author::getFirstName, Author::getLastName)
                .containsExactly("Isaac", "Asimov");
    }

    @Test
    void shouldUpdateAuthorBiographyWhenModifiedAndPersisted() {
        // Arrange
        Author author = new Author(UUID.randomUUID(), "Frank", "Herbert", "Old biography", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        author.setBio("Science fiction author, best known for Dune");
        persistenceContext.registerUpdated(author.getId(), author);
        persistenceContext.commit();

        Author updatedAuthor = authorRepository.findById(author.getId()).orElse(null);

        // Assert
        assertThat(updatedAuthor).isNotNull();
        assertThat(updatedAuthor.getBio()).isEqualTo("Science fiction author, best known for Dune");
    }

    @Test
    void shouldFindAuthorsByLastNameWhenAuthorsExist() {
        // Arrange
        Author author1 = new Author(UUID.randomUUID(), "Agatha", "Christie", "Mystery writer", null);
        Author author2 = new Author(UUID.randomUUID(), "Arthur", "Christie", "Another Christie", null);
        Author author3 = new Author(UUID.randomUUID(), "George", "Orwell", "Dystopian fiction writer", null);
        persistenceContext.registerNew(author1);
        persistenceContext.registerNew(author2);
        persistenceContext.registerNew(author3);
        persistenceContext.commit();

        // Act
        List<Author> christieAuthors = authorRepository.findByPartialName("Christie");

        // Assert
        assertThat(christieAuthors).hasSize(2);
        assertThat(christieAuthors)
                .extracting(Author::getFirstName)
                .containsExactlyInAnyOrder("Agatha", "Arthur");
    }

    @Test
    void shouldReturnEmptyListWhenNoAuthorsMatchLastName() {
        // Arrange
        Author author = new Author(UUID.randomUUID(), "Ray", "Bradbury", "Science fiction writer", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        List<Author> authors = authorRepository.findByPartialName("Tolkien");

        // Assert
        assertThat(authors).isEmpty();
    }

    @Test
    void shouldFindAudiobooksByAuthorIdWhenAudiobooksExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        Author author = new Author(authorId, "J.R.R.", "Tolkien", "Fantasy writer", null);
        Genre genre = new Genre(genreId, "Fantasy", "Epic fantasy stories");
        Audiobook audiobook1 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "The Hobbit", 36000, 1937, "Adventure in Middle-earth", null);
        Audiobook audiobook2 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "The Lord of the Rings", 54000, 1954, "Epic fantasy trilogy", null);
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
                .containsExactlyInAnyOrder("The Hobbit", "The Lord of the Rings");
    }

    @Test
    void shouldReturnEmptyListWhenNoAudiobooksForAuthorId() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        Author author = new Author(authorId, "No", "Books", "Author without books", null);
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
        Author author1 = new Author(UUID.randomUUID(), "Stephen", "King", "Horror writer", null);
        Author author2 = new Author(UUID.randomUUID(), "Stephen", "Hawking", "Physicist", null);
        Author author3 = new Author(UUID.randomUUID(), "Michael", "Crichton", "Thriller writer", null);
        persistenceContext.registerNew(author1);
        persistenceContext.registerNew(author2);
        persistenceContext.registerNew(author3);
        persistenceContext.commit();

        // Act
        List<Author> stephens = authorRepository.findByPartialName("Stephen");

        // Assert
        assertThat(stephens).hasSize(2);
        assertThat(stephens)
                .extracting(Author::getLastName)
                .containsExactlyInAnyOrder("King", "Hawking");
    }

    @Test
    void shouldCountAudiobooksByAuthorIdWhenAudiobooksExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        Author author = new Author(authorId, "Jules", "Verne", "Adventure writer", null);
        Genre genre = new Genre(genreId, "Adventure", "Exploration stories");
        Audiobook audiobook1 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Twenty Thousand Leagues Under the Sea", 16200, 1920, "Submarine adventure", null);
        Audiobook audiobook2 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Around the World in Eighty Days", 12600, 1921, "Global journey", null);
        Audiobook audiobook3 = new Audiobook(
                UUID.randomUUID(), authorId, genreId, "Journey to the Center of the Earth", 14400, 1922, "Underground adventure", null);
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.registerNew(audiobook3);
        persistenceContext.commit();

        // Act
        long count = authorRepository.countAudiobooksByAuthorId(authorId);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroWhenNoAudiobooksForAuthorId() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        Author author = new Author(authorId, "Empty", "Author", "No audiobooks", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        long count = authorRepository.countAudiobooksByAuthorId(authorId);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldCheckExistsByNameWhenAuthorExists() {
        // Arrange
        Author author = new Author(UUID.randomUUID(), "Existing", "Author", "This author exists", null);
        persistenceContext.registerNew(author);
        persistenceContext.commit();

        // Act
        List<Author> authors = authorRepository.findByName("Existing", "Author");
        boolean exists = !authors.isEmpty();

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAuthorDoesNotExistByName() {
        // Act
        List<Author> authors = authorRepository.findByName("Non", "Existing");
        boolean exists = !authors.isEmpty();

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldDeleteAuthorAndVerifyAbsence() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        Author author = new Author(authorId, "Delete", "Me", "This author will be deleted", null);
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
        Author author1 = new Author(UUID.randomUUID(), "Author", "One", "First author", null);
        Author author2 = new Author(UUID.randomUUID(), "Author", "Two", "Second author", null);
        persistenceContext.registerNew(author1);
        persistenceContext.registerNew(author2);
        persistenceContext.commit();

        // Act
        List<Author> authors = authorRepository.findAll();

        // Assert
        assertThat(authors).hasSize(2);
        assertThat(authors)
                .extracting(Author::getLastName)
                .containsExactlyInAnyOrder("One", "Two");
    }
}
