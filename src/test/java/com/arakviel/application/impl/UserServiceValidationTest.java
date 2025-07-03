package com.arakviel.application.impl;

import com.arakviel.application.contract.UserService;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Тести для перевірки нової системи валідації в UserServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceValidationTest {

    @Mock private UserRepository userRepository;
    @Mock private CollectionRepository collectionRepository;
    @Mock private ListeningProgressRepository listeningProgressRepository;
    @Mock private PersistenceContext persistenceContext;
    @Mock private FileStorageService fileStorageService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                collectionRepository,
                listeningProgressRepository,
                persistenceContext,
                fileStorageService
        );
    }

    // ========== CREATE USER VALIDATION TESTS ==========

    @Test
    void create_shouldCollectMultipleValidationErrors() {
        // Arrange
        User invalidUser = new User(
                UUID.randomUUID(),
                "", // порожній username
                null, // null password
                "invalid-email", // неправильний email
                null
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.create(invalidUser, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(3);
                    assertThat(errors).containsKeys("username", "passwordHash", "email");
                    assertThat(errors.get("username")).contains("не може бути порожнім");
                    assertThat(errors.get("passwordHash")).contains("не може бути null");
                    assertThat(errors.get("email")).contains("має неправильний формат email");
                });
    }

    @Test
    void create_shouldValidateDuplicateUsernameAndEmail() {
        // Arrange
        User user = new User(
                UUID.randomUUID(),
                "testuser",
                "password123",
                "test@example.com",
                null
        );

        // Mock duplicate checks
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(user, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("username", "email");
                    assertThat(errors.get("username")).contains("користувач з таким ім'ям уже існує");
                    assertThat(errors.get("email")).contains("користувач з таким email уже існує");
                });
    }

    // ========== UPDATE USER VALIDATION TESTS ==========

    @Test
    void update_shouldValidateNonExistentUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "password", "test@example.com", null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.update(userId, user, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).containsKey("id");
                    assertThat(errors.get("id")).contains("користувач з таким ідентифікатором не існує");
                });
    }

    @Test
    void update_shouldValidateDuplicateChanges() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = new User(userId, "olduser", "oldpass", "old@example.com", null);
        User updatedUser = new User(userId, "newuser", "newpass", "new@example.com", null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newuser")).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.update(userId, updatedUser, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("username", "email");
                });
    }

    // ========== SEARCH VALIDATION TESTS ==========

    @Test
    void findByUsername_shouldValidateEmptyUsername() {
        assertThatThrownBy(() -> userService.findByUsername(""))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    assertThat(validationException.hasFieldErrors("username")).isTrue();
                });
    }

    @Test
    void findByEmail_shouldValidateEmptyEmail() {
        assertThatThrownBy(() -> userService.findByEmail(null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    assertThat(validationException.hasFieldErrors("email")).isTrue();
                });
    }

    // ========== PASSWORD CHANGE VALIDATION TESTS ==========

    @Test
    void changePassword_shouldValidateMultipleFields() {
        assertThatThrownBy(() -> userService.changePassword(null, "", null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(3);
                    assertThat(errors).containsKeys("userId", "oldPassword", "newPassword");
                });
    }

    @Test
    void changePassword_shouldValidateNonExistentUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.changePassword(userId, "oldpass", "newpass"))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    assertThat(validationException.hasFieldErrors("userId")).isTrue();
                    assertThat(validationException.getFieldErrors("userId")).contains("користувач не знайдений");
                });
    }

    // ========== AUTHENTICATION VALIDATION TESTS ==========

    @Test
    void authenticate_shouldValidateCredentials() {
        assertThatThrownBy(() -> userService.authenticate("", null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    Map<String, List<String>> errors = validationException.getAllFieldErrors();
                    
                    assertThat(errors).hasSize(2);
                    assertThat(errors).containsKeys("username", "password");
                });
    }

    // ========== UUID VALIDATION TESTS ==========

    @Test
    void findCollectionsByUserId_shouldValidateNullUserId() {
        assertThatThrownBy(() -> userService.findCollectionsByUserId(null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    assertThat(validationException.hasFieldErrors("userId")).isTrue();
                });
    }

    @Test
    void findListeningProgressByUserId_shouldValidateNullUserId() {
        assertThatThrownBy(() -> userService.findListeningProgressByUserId(null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    assertThat(validationException.hasFieldErrors("userId")).isTrue();
                });
    }

    // ========== COMPREHENSIVE VALIDATION TESTS ==========

    @Test
    void userValidation_shouldProvideDetailedErrorInformation() {
        // Test that validation provides comprehensive error information
        User invalidUser = new User(UUID.randomUUID(), "ab", "", "not-email", null);

        assertThatThrownBy(() -> userService.create(invalidUser, null, null))
                .isInstanceOf(MultiFieldValidationException.class)
                .satisfies(exception -> {
                    MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
                    
                    // Test error count and fields
                    assertThat(validationException.getErrorCount()).isGreaterThan(0);
                    assertThat(validationException.getFieldsWithErrors()).isNotEmpty();
                    assertThat(validationException.hasErrors()).isTrue();
                    
                    // Test message contains field information
                    String message = validationException.getMessage();
                    assertThat(message).isNotBlank();
                });
    }
}
