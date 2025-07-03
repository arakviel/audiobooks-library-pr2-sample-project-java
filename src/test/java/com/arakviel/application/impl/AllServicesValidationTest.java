package com.arakviel.application.impl;

import com.arakviel.application.contract.*;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.domain.entities.*;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тести для перевірки нової системи валідації в усіх сервісах.
 */
@ExtendWith(MockitoExtension.class)
class AllServicesValidationTest {

    @Mock private CollectionRepository collectionRepository;
    @Mock private AudiobookRepository audiobookRepository;
    @Mock private UserRepository userRepository;
    @Mock private AudiobookFileRepository audiobookFileRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private ListeningProgressRepository listeningProgressRepository;
    @Mock private PersistenceContext persistenceContext;
    @Mock private FileStorageService fileStorageService;

    private CollectionService collectionService;
    private UserService userService;
    private AudiobookService audiobookService;
    private AudiobookFileService audiobookFileService;
    private AuthorService authorService;
    private GenreService genreService;
    private ListeningProgressService listeningProgressService;

    @BeforeEach
    void setUp() {
        collectionService = new CollectionServiceImpl(collectionRepository, audiobookRepository, userRepository, persistenceContext);
        userService = new UserServiceImpl(userRepository, collectionRepository, listeningProgressRepository, persistenceContext, fileStorageService);
        audiobookService = new AudiobookServiceImpl(audiobookRepository, audiobookFileRepository, authorRepository, genreRepository, persistenceContext, fileStorageService);
        audiobookFileService = new AudiobookFileServiceImpl(audiobookFileRepository, audiobookRepository, persistenceContext, fileStorageService);
        authorService = new AuthorServiceImpl(authorRepository, persistenceContext, fileStorageService);
        genreService = new GenreServiceImpl(genreRepository, persistenceContext);
        listeningProgressService = new ListeningProgressServiceImpl(listeningProgressRepository, audiobookRepository, userRepository, persistenceContext);
    }

    // ========== COLLECTION SERVICE TESTS ==========

    @Test
    void collectionService_shouldCollectMultipleValidationErrors() {
        assertThatThrownBy(() -> collectionService.findPublicCollections(-1, -5))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("offset", "limit");
                    assertThat(errors.get("offset")).contains("не може бути від'ємним");
                    assertThat(errors.get("limit")).contains("повинно бути більше нуля");
                });
    }

    // ========== USER SERVICE TESTS ==========

    @Test
    void userService_shouldCollectMultipleValidationErrors() {
        User invalidUser = new User(UUID.randomUUID(), "", null, "invalid-email", null);

        assertThatThrownBy(() -> userService.create(invalidUser, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(3);
                    assertThat(errors).containsKeys("username", "passwordHash", "email");
                });
    }

    // ========== AUDIOBOOK SERVICE TESTS ==========

    @Test
    void audiobookService_shouldValidateMultipleFields() {
        assertThatThrownBy(() -> audiobookService.findByDurationRange(-10, -5))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("minDuration", "maxDuration");
                });
    }

    // ========== AUDIOBOOK FILE SERVICE TESTS ==========

    @Test
    void audiobookFileService_shouldValidateMultipleFields() {
        assertThatThrownBy(() -> audiobookFileService.findByAudiobookIdAndFormat(null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("audiobookId", "format");
                });
    }

    @Test
    void audiobookFileService_shouldValidateAudiobookFile() {
        AudiobookFile invalidFile = new AudiobookFile(UUID.randomUUID(), null, "path", null, -100);

        assertThatThrownBy(() -> audiobookFileService.create(invalidFile, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(3);
                    assertThat(errors).containsKeys("audiobookId", "format", "size");
                });
    }

    // ========== AUTHOR SERVICE TESTS ==========

    @Test
    void authorService_shouldValidateMultipleFields() {
        assertThatThrownBy(() -> authorService.findByName(null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("firstName", "lastName");
                });
    }

    @Test
    void authorService_shouldValidateAuthor() {
        Author invalidAuthor = new Author(UUID.randomUUID(), "", null, null, null);

        assertThatThrownBy(() -> authorService.create(invalidAuthor, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("firstName", "lastName");
                });
    }

    // ========== GENRE SERVICE TESTS ==========

    @Test
    void genreService_shouldValidateGenre() {
        Genre invalidGenre = new Genre(UUID.randomUUID(), "", "");

        assertThatThrownBy(() -> genreService.create(invalidGenre))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).containsKey("name");
                    assertThat(errors.get("name")).contains("не може бути порожнім");
                });
    }

    // ========== LISTENING PROGRESS SERVICE TESTS ==========

    @Test
    void listeningProgressService_shouldValidateMultipleFields() {
        assertThatThrownBy(() -> listeningProgressService.findByUserIdAndAudiobookId(null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("userId", "audiobookId");
                });
    }

    @Test
    void listeningProgressService_shouldValidateUpdateProgress() {
        assertThatThrownBy(() -> listeningProgressService.updateProgress(null, null, -10))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(3);
                    assertThat(errors).containsKeys("userId", "audiobookId", "position");
                });
    }

    @Test
    void listeningProgressService_shouldValidateListeningProgress() {
        ListeningProgress invalidProgress = new ListeningProgress(UUID.randomUUID(), null, null, -5, LocalDateTime.now());

        assertThatThrownBy(() -> listeningProgressService.create(invalidProgress))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(3);
                    assertThat(errors).containsKeys("userId", "audiobookId", "position");
                });
    }

    // ========== COMPREHENSIVE VALIDATION TESTS ==========

    @Test
    void allServices_shouldProvideDetailedErrorMessages() {
        // Test that all services provide detailed error information
        assertThatThrownBy(() -> collectionService.findPublicCollectionsByName(""))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    
                    assertThat(validationException.hasErrors()).isTrue();
                    assertThat(validationException.getErrorCount()).isGreaterThan(0);
                    assertThat(validationException.getFieldsWithErrors()).isNotEmpty();
                    assertThat(validationException.getMessage()).isNotBlank();
                });
    }

    @Test
    void allServices_shouldHandleNullObjectsGracefully() {
        // Test that all services handle null objects properly
        assertThatThrownBy(() -> collectionService.createPublicCollection(null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    assertThat(validationException.hasFieldErrors("collection")).isTrue();
                });

        assertThatThrownBy(() -> userService.create(null, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    assertThat(validationException.hasFieldErrors("user")).isTrue();
                });

        assertThatThrownBy(() -> authorService.create(null, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    assertThat(validationException.hasFieldErrors("author")).isTrue();
                });
    }

    @Test
    void validationHelper_shouldSupportChaining() {
        // This test demonstrates that validation works consistently across all services
        // and that the ValidationHelper supports method chaining properly
        
        // All these should throw MultiFieldValidationException with multiple errors
        assertThatThrownBy(() -> collectionService.findPublicCollections(-1, 0))
                .isInstanceOf(MultiFieldValidationException.class);
        
        assertThatThrownBy(() -> audiobookFileService.copyFile(null, null, ""))
                .isInstanceOf(MultiFieldValidationException.class);
        
        assertThatThrownBy(() -> genreService.findByName(""))
                .isInstanceOf(MultiFieldValidationException.class);
    }
}
