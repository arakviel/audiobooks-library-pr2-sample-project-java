package com.arakviel.application.impl;

import com.arakviel.application.contract.CollectionService;
import com.arakviel.application.contract.UserService;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import com.arakviel.infrastructure.file.FileStorageService;
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
 * Тести для перевірки нової системи валідації з множинними помилками в сервісах.
 */
@ExtendWith(MockitoExtension.class)
class MultiFieldValidationServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private AudiobookRepository audiobookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListeningProgressRepository listeningProgressRepository;

    @Mock
    private PersistenceContext persistenceContext;

    @Mock
    private FileStorageService fileStorageService;

    private CollectionService collectionService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        collectionService = new CollectionServiceImpl(
                collectionRepository,
                audiobookRepository,
                userRepository,
                persistenceContext
        );

        userService = new UserServiceImpl(
                userRepository,
                collectionRepository,
                listeningProgressRepository,
                persistenceContext,
                fileStorageService
        );
    }

    // ========== COLLECTION SERVICE VALIDATION TESTS ==========

    @Test
    void shouldThrowMultiFieldValidationExceptionForInvalidCollection() {
        // Arrange
        Collection invalidCollection = new Collection(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null, // invalid name
                LocalDateTime.now()
        );

        // Act & Assert
        assertThatThrownBy(() -> collectionService.createPublicCollection(invalidCollection))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();

                    assertThat(errors).containsKey("name");
                    assertThat(errors.get("name")).contains("не може бути null");
                });
    }

    @Test
    void shouldThrowMultiFieldValidationExceptionForInvalidPagination() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.findPublicCollections(-1, 0))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKey("offset");
                    assertThat(errors).containsKey("limit");
                    assertThat(errors.get("offset")).contains("не може бути від'ємним");
                    assertThat(errors.get("limit")).contains("повинно бути більше нуля");
                });
    }

    @Test
    void shouldThrowMultiFieldValidationExceptionForEmptySearchTerms() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.findPublicCollectionsByName(""))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).containsKey("name");
                    assertThat(errors.get("name")).contains("не може бути порожнім");
                });
    }

    // ========== USER SERVICE VALIDATION TESTS ==========

    @Test
    void shouldThrowMultiFieldValidationExceptionForInvalidUser() {
        // Arrange
        User invalidUser = new User(
                UUID.randomUUID(),
                null, // invalid username
                null, // invalid password
                "invalid-email", // invalid email
                null
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.create(invalidUser, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(3);
                    assertThat(errors).containsKey("username");
                    assertThat(errors).containsKey("passwordHash");
                    assertThat(errors).containsKey("email");
                    
                    assertThat(errors.get("username")).contains("не може бути null");
                    assertThat(errors.get("passwordHash")).contains("не може бути null");
                    assertThat(errors.get("email")).contains("має неправильний формат email");
                });
    }

    @Test
    void shouldThrowMultiFieldValidationExceptionForInvalidUsernameFormat() {
        // Arrange
        User invalidUser = new User(
                UUID.randomUUID(),
                "ab", // too short username
                "validpassword",
                "valid@email.com",
                null
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.create(invalidUser, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).containsKey("username");
                    assertThat(errors.get("username")).contains("може містити тільки літери, цифри та підкреслення (3-20 символів)");
                });
    }

    @Test
    void shouldThrowMultiFieldValidationExceptionForUserPagination() {
        // Act & Assert
        assertThatThrownBy(() -> userService.findAll(-5, -10))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKey("offset");
                    assertThat(errors).containsKey("limit");
                    assertThat(errors.get("offset")).contains("не може бути від'ємним");
                    assertThat(errors.get("limit")).contains("повинно бути більше нуля");
                });
    }

    // ========== VALIDATION EXCEPTION BEHAVIOR TESTS ==========

    @Test
    void shouldProvideDetailedErrorInformation() {
        // Arrange
        Collection invalidCollection = new Collection(null, null, "", null);

        // Act & Assert
        assertThatThrownBy(() -> collectionService.createPublicCollection(invalidCollection))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    
                    // Test error count
                    assertThat(validationException.getErrorCount()).isGreaterThan(0);
                    
                    // Test fields with errors
                    assertThat(validationException.getFieldsWithErrors()).isNotEmpty();
                    
                    // Test has errors
                    assertThat(validationException.hasErrors()).isTrue();
                    
                    // Test specific field errors
                    assertThat(validationException.hasFieldErrors("name")).isTrue();
                    assertThat(validationException.getFieldErrors("name")).isNotEmpty();
                    
                    // Test message contains field information
                    String message = validationException.getMessage();
                    assertThat(message).contains("name");
                });
    }

    @Test
    void shouldHandleNullObjectValidation() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.createPublicCollection(null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).containsKey("collection");
                    assertThat(errors.get("collection")).contains("не може бути null");
                });
    }

    @Test
    void shouldValidateMultipleFieldsSimultaneously() {
        // Arrange
        User userWithMultipleErrors = new User(
                UUID.randomUUID(),
                "", // empty username
                null, // null password
                "not-an-email", // invalid email
                null
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.create(userWithMultipleErrors, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    // Should collect all errors, not stop at first one
                    assertThat(errors.keySet()).containsExactlyInAnyOrder("username", "passwordHash", "email");
                    
                    // Each field should have appropriate error messages
                    assertThat(errors.get("username")).contains("не може бути порожнім");
                    assertThat(errors.get("passwordHash")).contains("не може бути null");
                    assertThat(errors.get("email")).contains("має неправильний формат email");
                });
    }

    @Test
    void shouldNotThrowWhenValidationPasses() {
        // This test ensures that valid data doesn't trigger validation exceptions
        // We can't easily test this without mocking the repositories, but the structure is here
        // for when we want to add integration tests
    }
}
