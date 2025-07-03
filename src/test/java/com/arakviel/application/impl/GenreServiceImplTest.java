package com.arakviel.application.impl;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тести для {@link GenreServiceImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private PersistenceContext persistenceContext;

    private GenreServiceImpl genreService;
    private Genre testGenre;
    private UUID testGenreId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        genreService = new GenreServiceImpl(genreRepository, persistenceContext);

        testGenreId = UUID.randomUUID();
        testGenre = new Genre(
                testGenreId,
                "Фантастика",
                "Жанр художньої літератури, що зображує неіснуючі, фантастичні явища"
        );
    }

    // Create method tests
    @Test
    void shouldCreateGenreSuccessfullyWhenValidDataProvided() {
        // Arrange
        when(genreRepository.existsByName(testGenre.getName())).thenReturn(false);
        doNothing().when(persistenceContext).registerNew(any(Genre.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Genre result = genreService.create(testGenre);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        verify(persistenceContext).registerNew(any(Genre.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateGenre() {
        // Arrange
        when(genreRepository.existsByName(testGenre.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> genreService.create(testGenre))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Жанр з назвою");
    }

    // Update method tests
    @Test
    void shouldUpdateGenreSuccessfullyWhenValidDataProvided() {
        // Arrange
        Genre updatedGenre = new Genre(
                testGenreId,
                "Наукова фантастика",
                "Оновлений опис жанру"
        );

        when(genreRepository.findById(testGenreId)).thenReturn(Optional.of(testGenre));
        when(genreRepository.existsByName(updatedGenre.getName())).thenReturn(false);
        doNothing().when(persistenceContext).registerUpdated(eq(testGenreId), any(Genre.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Genre result = genreService.update(testGenreId, updatedGenre);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testGenreId);
        verify(persistenceContext).registerUpdated(eq(testGenreId), any(Genre.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentGenre() {
        // Arrange
        when(genreRepository.findById(testGenreId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> genreService.update(testGenreId, testGenre))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Жанр з ідентифікатором " + testGenreId + " не існує");
    }

    // Delete method tests
    @Test
    void shouldDeleteGenreSuccessfullyWhenGenreExists() {
        // Arrange
        when(genreRepository.findById(testGenreId)).thenReturn(Optional.of(testGenre));
        when(genreRepository.countAudiobooksByGenreId(testGenreId)).thenReturn(0L);
        doNothing().when(persistenceContext).registerDeleted(testGenre);
        doNothing().when(persistenceContext).commit();

        // Act
        genreService.delete(testGenreId);

        // Assert
        verify(persistenceContext).registerDeleted(testGenre);
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenDeletingGenreWithAudiobooks() {
        // Arrange
        when(genreRepository.findById(testGenreId)).thenReturn(Optional.of(testGenre));
        when(genreRepository.countAudiobooksByGenreId(testGenreId)).thenReturn(3L);

        // Act & Assert
        assertThatThrownBy(() -> genreService.delete(testGenreId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Неможливо видалити жанр, оскільки він пов'язаний з аудіокнигами");
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentGenre() {
        // Arrange
        when(genreRepository.findById(testGenreId)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        genreService.delete(testGenreId);
        verify(persistenceContext, never()).registerDeleted(any());
    }

    // Find methods tests
    @Test
    void shouldFindGenreByIdWhenGenreExists() {
        // Arrange
        when(genreRepository.findById(testGenreId)).thenReturn(Optional.of(testGenre));

        // Act
        Optional<Genre> result = genreService.findById(testGenreId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testGenre);
    }

    @Test
    void shouldFindAllGenresWithPagination() {
        // Arrange
        List<Genre> genres = Arrays.asList(testGenre);
        when(genreRepository.findAll(0, 10)).thenReturn(genres);

        // Act
        List<Genre> result = genreService.findAll(0, 10);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testGenre);
    }

    @Test
    void shouldFindGenresByName() {
        // Arrange
        List<Genre> genres = Arrays.asList(testGenre);
        when(genreRepository.findByName("Фантастика")).thenReturn(genres);

        // Act
        List<Genre> result = genreService.findByName("Фантастика");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testGenre);
    }

    @Test
    void shouldFindGenresByPartialName() {
        // Arrange
        List<Genre> genres = Arrays.asList(testGenre);
        when(genreRepository.findByPartialName("Фант")).thenReturn(genres);

        // Act
        List<Genre> result = genreService.findByPartialName("Фант");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testGenre);
    }

    @Test
    void shouldCheckIfGenreExistsByName() {
        // Arrange
        when(genreRepository.existsByName("Фантастика")).thenReturn(true);

        // Act
        boolean result = genreService.existsByName("Фантастика");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenGenreDoesNotExistByName() {
        // Arrange
        when(genreRepository.existsByName("Неіснуючий жанр")).thenReturn(false);

        // Act
        boolean result = genreService.existsByName("Неіснуючий жанр");

        // Assert
        assertThat(result).isFalse();
    }
}
