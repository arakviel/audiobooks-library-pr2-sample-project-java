package com.arakviel.application.impl;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тести для {@link AuthorServiceImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private PersistenceContext persistenceContext;

    @Mock
    private FileStorageService fileStorageService;

    private AuthorServiceImpl authorService;
    private Author testAuthor;
    private UUID testAuthorId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authorService = new AuthorServiceImpl(authorRepository, persistenceContext, fileStorageService);

        testAuthorId = UUID.randomUUID();
        testAuthor = new Author(
                testAuthorId,
                "Джоан",
                "Роулінг",
                "Британська письменниця, авторка серії книг про Гаррі Поттера",
                null
        );
    }

    // Create method tests
    @Test
    void shouldCreateAuthorSuccessfullyWhenValidDataProvided() {
        // Arrange
        when(authorRepository.findByName(testAuthor.getFirstName(), testAuthor.getLastName())).thenReturn(Collections.emptyList());
        doNothing().when(persistenceContext).registerNew(any(Author.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Author result = authorService.create(testAuthor, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        verify(persistenceContext).registerNew(any(Author.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldCreateAuthorWithPhotoWhenPhotoProvided() {
        // Arrange
        InputStream photo = new ByteArrayInputStream("photo content".getBytes());
        String photoName = "author.jpg";
        Path photoPath = Paths.get("/storage/author.jpg");

        when(authorRepository.findByName(testAuthor.getFirstName(), testAuthor.getLastName())).thenReturn(Collections.emptyList());
        when(fileStorageService.save(photo, photoName, testAuthor.getId())).thenReturn(photoPath);
        doNothing().when(persistenceContext).registerNew(any(Author.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Author result = authorService.create(testAuthor, photo, photoName);

        // Assert
        assertThat(result.getImagePath()).isEqualTo(photoPath.toString());
        verify(fileStorageService).save(photo, photoName, testAuthor.getId());
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateAuthor() {
        // Arrange
        when(authorRepository.findByName(testAuthor.getFirstName(), testAuthor.getLastName())).thenReturn(Arrays.asList(testAuthor));

        // Act & Assert
        assertThatThrownBy(() -> authorService.create(testAuthor, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Автор з таким ім'ям та прізвищем уже існує");
    }

    // Update method tests
    @Test
    void shouldUpdateAuthorSuccessfullyWhenValidDataProvided() {
        // Arrange
        when(authorRepository.findById(testAuthorId)).thenReturn(Optional.of(testAuthor));
        doNothing().when(persistenceContext).registerUpdated(eq(testAuthorId), any(Author.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Author result = authorService.update(testAuthorId, testAuthor, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testAuthorId);
        verify(persistenceContext).registerUpdated(eq(testAuthorId), any(Author.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentAuthor() {
        // Arrange
        when(authorRepository.findById(testAuthorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authorService.update(testAuthorId, testAuthor, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Автор з ідентифікатором " + testAuthorId + " не існує");
    }

    // Delete method tests
    @Test
    void shouldDeleteAuthorSuccessfullyWhenAuthorExists() {
        // Arrange
        when(authorRepository.findById(testAuthorId)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.countAudiobooksByAuthorId(testAuthorId)).thenReturn(0L);
        doNothing().when(persistenceContext).registerDeleted(testAuthor);
        doNothing().when(persistenceContext).commit();

        // Act
        authorService.delete(testAuthorId);

        // Assert
        verify(persistenceContext).registerDeleted(testAuthor);
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenDeletingAuthorWithAudiobooks() {
        // Arrange
        when(authorRepository.findById(testAuthorId)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.countAudiobooksByAuthorId(testAuthorId)).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> authorService.delete(testAuthorId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Неможливо видалити автора, оскільки він пов'язаний з аудіокнигами");
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentAuthor() {
        // Arrange
        when(authorRepository.findById(testAuthorId)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        authorService.delete(testAuthorId);
        verify(persistenceContext, never()).registerDeleted(any());
    }

    // Find methods tests
    @Test
    void shouldFindAuthorByIdWhenAuthorExists() {
        // Arrange
        when(authorRepository.findById(testAuthorId)).thenReturn(Optional.of(testAuthor));

        // Act
        Optional<Author> result = authorService.findById(testAuthorId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAuthor);
    }

    @Test
    void shouldFindAllAuthorsWithPagination() {
        // Arrange
        List<Author> authors = Arrays.asList(testAuthor);
        when(authorRepository.findAll(0, 10)).thenReturn(authors);

        // Act
        List<Author> result = authorService.findAll(0, 10);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testAuthor);
    }

    @Test
    void shouldFindAuthorsByName() {
        // Arrange
        List<Author> authors = Arrays.asList(testAuthor);
        when(authorRepository.findByName("Джоан", "Роулінг")).thenReturn(authors);

        // Act
        List<Author> result = authorService.findByName("Джоан", "Роулінг");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testAuthor);
    }

    @Test
    void shouldFindAuthorsByPartialName() {
        // Arrange
        List<Author> authors = Arrays.asList(testAuthor);
        when(authorRepository.findByPartialName("Джоан")).thenReturn(authors);

        // Act
        List<Author> result = authorService.findByPartialName("Джоан");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testAuthor);
    }

    @Test
    void shouldFindAudiobooksByAuthorId() {
        // Arrange
        Audiobook audiobook = new Audiobook(
                UUID.randomUUID(),
                testAuthorId,
                UUID.randomUUID(),
                "Test Audiobook",
                3600,
                2023,
                "Description",
                null
        );
        List<Audiobook> audiobooks = Arrays.asList(audiobook);
        when(authorRepository.findAudiobooksByAuthorId(testAuthorId)).thenReturn(audiobooks);

        // Act
        List<Audiobook> result = authorService.findAudiobooksByAuthorId(testAuthorId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(audiobook);
    }

    @Test
    void shouldCountAudiobooksByAuthorId() {
        // Arrange
        when(authorRepository.countAudiobooksByAuthorId(testAuthorId)).thenReturn(5L);

        // Act
        long result = authorService.countAudiobooksByAuthorId(testAuthorId);

        // Assert
        assertThat(result).isEqualTo(5L);
    }
}
