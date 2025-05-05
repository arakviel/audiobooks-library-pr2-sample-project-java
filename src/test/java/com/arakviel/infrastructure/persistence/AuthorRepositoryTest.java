package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Author;
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
    public AuthorRepositoryTest(AuthorRepository authorRepository,
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
        persistenceInitializer.init(false);
    }

    @AfterAll
    void closeResources() {
        connectionPool.shutdown();
    }

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
}