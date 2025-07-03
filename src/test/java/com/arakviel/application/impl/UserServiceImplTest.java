package com.arakviel.application.impl;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
 * Тести для {@link UserServiceImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private ListeningProgressRepository listeningProgressRepository;

    @Mock
    private PersistenceContext persistenceContext;

    @Mock
    private FileStorageService fileStorageService;

    private UserServiceImpl userService;
    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(
                userRepository,
                collectionRepository,
                listeningProgressRepository,
                persistenceContext,
                fileStorageService
        );

        testUserId = UUID.randomUUID();
        testUser = new User(
                testUserId,
                "testuser",
                "hashedpassword",
                "test@example.com",
                null
        );
    }

    // Create method tests
    @Test
    void shouldCreateUserSuccessfullyWhenValidDataProvided() {
        // Arrange
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        doNothing().when(persistenceContext).registerNew(any(User.class));
        doNothing().when(persistenceContext).commit();

        // Act
        User result = userService.create(testUser, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        verify(persistenceContext).registerNew(any(User.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldCreateUserWithAvatarWhenAvatarProvided() {
        // Arrange
        InputStream avatar = new ByteArrayInputStream("avatar content".getBytes());
        String avatarName = "avatar.jpg";
        Path avatarPath = Paths.get("/storage/user/avatar.jpg");

        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(fileStorageService.save(avatar, avatarName, testUser.getId())).thenReturn(avatarPath);
        doNothing().when(persistenceContext).registerNew(any(User.class));
        doNothing().when(persistenceContext).commit();

        // Act
        User result = userService.create(testUser, avatar, avatarName);

        // Assert
        assertThat(result.getAvatarPath()).isEqualTo(avatarPath.toString());
        verify(fileStorageService).save(avatar, avatarName, testUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithDuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(testUser, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з таким ім'ям уже існує");
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithDuplicateEmail() {
        // Arrange
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(testUser, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з таким email уже існує");
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithNullData() {
        // Act & Assert
        assertThatThrownBy(() -> userService.create(null, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач не може бути null");
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithEmptyUsername() {
        // Arrange
        testUser.setUsername("");

        // Act & Assert
        assertThatThrownBy(() -> userService.create(testUser, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username не може бути порожнім");
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithInvalidEmail() {
        // Arrange
        testUser.setEmail("invalid-email");

        // Act & Assert
        assertThatThrownBy(() -> userService.create(testUser, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некоректний формат email");
    }

    // Update method tests
    @Test
    void shouldUpdateUserSuccessfullyWhenValidDataProvided() {
        // Arrange
        User updatedUser = new User(
                testUserId,
                "updateduser",
                "newhashedpassword",
                "updated@example.com",
                null
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(updatedUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(updatedUser.getEmail())).thenReturn(false);
        doNothing().when(persistenceContext).registerUpdated(eq(testUserId), any(User.class));
        doNothing().when(persistenceContext).commit();

        // Act
        User result = userService.update(testUserId, updatedUser, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        verify(persistenceContext).registerUpdated(eq(testUserId), any(User.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.update(testUserId, testUser, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з ідентифікатором " + testUserId + " не існує");
    }

    // Delete method tests
    @Test
    void shouldDeleteUserSuccessfullyWhenUserExists() {
        // Arrange
        testUser.setAvatarPath("/storage/user/avatar.jpg");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(fileStorageService).delete(anyString(), eq(testUserId));
        doNothing().when(persistenceContext).registerDeleted(testUser);
        doNothing().when(persistenceContext).commit();

        // Act
        userService.delete(testUserId);

        // Assert
        verify(fileStorageService).delete("avatar.jpg", testUserId);
        verify(persistenceContext).registerDeleted(testUser);
        verify(persistenceContext).commit();
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        userService.delete(testUserId);
        verify(persistenceContext, never()).registerDeleted(any());
    }

    // Find methods tests
    @Test
    void shouldFindUserByIdWhenUserExists() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(testUserId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(testUserId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllUsersWithPagination() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll(0, 10)).thenReturn(users);

        // Act
        List<User> result = userService.findAll(0, 10);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testUser);
    }

    @Test
    void shouldThrowExceptionForInvalidPagination() {
        // Act & Assert
        assertThatThrownBy(() -> userService.findAll(-1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Offset не може бути від'ємним");

        assertThatThrownBy(() -> userService.findAll(0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Limit повинен бути більше нуля");
    }

    // Authentication tests
    @Test
    void shouldAuthenticateUserSuccessfullyWithCorrectCredentials() {
        // Arrange
        String username = "testuser";
        String password = "password";
        String hashedPassword = userService.hashPassword(password); // We need to access the hash method
        testUser.setPasswordHash(hashedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Arrays.asList(testUser));

        // Act
        Optional<User> result = userService.authenticate(username, password);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    void shouldReturnEmptyWhenAuthenticatingWithWrongPassword() {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        testUser.setPasswordHash("differenthash");

        when(userRepository.findByUsername(username)).thenReturn(Arrays.asList(testUser));

        // Act
        Optional<User> result = userService.authenticate(username, password);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenAuthenticatingNonExistentUser() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Collections.emptyList());

        // Act
        Optional<User> result = userService.authenticate("nonexistent", "password");

        // Assert
        assertThat(result).isEmpty();
    }

    // Collection and progress methods tests
    @Test
    void shouldFindCollectionsByUserId() {
        // Arrange
        List<Collection> collections = Arrays.asList(
                new Collection(UUID.randomUUID(), testUserId, "Collection 1", LocalDateTime.now())
        );
        when(collectionRepository.findByUserId(testUserId)).thenReturn(collections);

        // Act
        List<Collection> result = userService.findCollectionsByUserId(testUserId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(testUserId);
    }

    @Test
    void shouldCountCollectionsByUserId() {
        // Arrange
        when(collectionRepository.countByUserId(testUserId)).thenReturn(5L);

        // Act
        long result = userService.countCollectionsByUserId(testUserId);

        // Assert
        assertThat(result).isEqualTo(5L);
    }
}
