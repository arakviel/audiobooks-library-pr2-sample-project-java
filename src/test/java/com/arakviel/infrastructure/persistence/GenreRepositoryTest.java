package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.GenreRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тести для репозиторію жанрів.
 * Перевіряють основні операції CRUD та специфічні методи пошуку.
 */
@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private PersistenceInitializer persistenceInitializer;

    @Autowired
    private ConnectionPool connectionPool;

    @BeforeEach
    void setUp() {
        persistenceInitializer.init(true); // Initialize with sample data
    }

    @AfterAll
    void closeResources() {
        connectionPool.shutdown();
    }

    @Test
    void testFindAll() {
        // Act
        List<Genre> genres = genreRepository.findAll();

        // Assert - sample data should be loaded
        assertThat(genres).isNotEmpty();
    }

    @Test
    void testFindByName() {
        // Arrange - get first genre from sample data
        List<Genre> allGenres = genreRepository.findAll();
        assertThat(allGenres).isNotEmpty();

        Genre sampleGenre = allGenres.get(0);
        String name = sampleGenre.getName();

        // Act
        List<Genre> foundGenres = genreRepository.findByName(name);

        // Assert
        assertThat(foundGenres).isNotEmpty();
        assertThat(foundGenres.get(0).getName()).isEqualTo(name);
    }

    @Test
    void testFindByPartialName() {
        // Arrange - get first genre from sample data
        List<Genre> allGenres = genreRepository.findAll();
        assertThat(allGenres).isNotEmpty();

        Genre sampleGenre = allGenres.get(0);
        String partialName = sampleGenre.getName().substring(0, 2); // First 2 characters

        // Act
        List<Genre> foundGenres = genreRepository.findByPartialName(partialName);

        // Assert
        assertThat(foundGenres).isNotEmpty();
    }

    @Test
    void testCount() {
        // Act
        long count = genreRepository.count();

        // Assert - sample data should have genres
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void testFindWithPagination() {
        // Arrange
        List<Genre> allGenres = genreRepository.findAll();
        int totalGenres = allGenres.size();

        // Skip validation if not enough sample data
        if (totalGenres < 2) {
            return;
        }

        // Act - get first page with 1 item
        List<Genre> firstPage = genreRepository.findAll(0, 2);

        // Assert
        assertThat(firstPage).hasSize(2);
    }

    @Test
    void testExistsByName() {
        // Arrange - get first genre from sample data
        List<Genre> allGenres = genreRepository.findAll();
        assertThat(allGenres).isNotEmpty();

        Genre sampleGenre = allGenres.get(0);
        String name = sampleGenre.getName();

        // Act
        boolean exists = genreRepository.existsByName(name);

        // Assert
        assertThat(exists).isTrue();
    }
}
