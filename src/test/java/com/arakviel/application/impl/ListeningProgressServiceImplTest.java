package com.arakviel.application.impl;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
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
 * Тести для {@link ListeningProgressServiceImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ListeningProgressServiceImplTest {

    @Mock
    private ListeningProgressRepository listeningProgressRepository;

    @Mock
    private AudiobookRepository audiobookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersistenceContext persistenceContext;

    private ListeningProgressServiceImpl listeningProgressService;
    private ListeningProgress testProgress;
    private UUID testProgressId;
    private UUID testUserId;
    private UUID testAudiobookId;
    private Audiobook testAudiobook;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listeningProgressService = new ListeningProgressServiceImpl(
                listeningProgressRepository,
                audiobookRepository,
                userRepository,
                persistenceContext
        );

        testProgressId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testAudiobookId = UUID.randomUUID();
        testProgress = new ListeningProgress(
                testProgressId,
                testUserId,
                testAudiobookId,
                1800, // 30 minutes
                LocalDateTime.now()
        );

        testAudiobook = new Audiobook(
                testAudiobookId,
                UUID.randomUUID(), // authorId
                UUID.randomUUID(), // genreId
                "Test Audiobook",
                3600, // 1 hour duration
                2023,
                "Test description",
                null // coverImagePath
        );
    }

    // Create method tests
    @Test
    void shouldCreateProgressSuccessfullyWhenValidDataProvided() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(createTestUser()));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(testAudiobook));
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.empty());
        doNothing().when(persistenceContext).registerNew(any(ListeningProgress.class));
        doNothing().when(persistenceContext).commit();

        // Act
        ListeningProgress result = listeningProgressService.create(testProgress);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getLastListened()).isNotNull();
        verify(persistenceContext).registerNew(any(ListeningProgress.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenCreatingProgressForNonExistentUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.create(testProgress))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з ідентифікатором " + testUserId + " не існує");
    }

    @Test
    void shouldThrowExceptionWhenCreatingProgressForNonExistentAudiobook() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(createTestUser()));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.create(testProgress))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Аудіокнига з ідентифікатором " + testAudiobookId + " не існує");
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateProgress() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(createTestUser()));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(testAudiobook));
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.of(testProgress));

        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.create(testProgress))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Запис прогресу для цього користувача та аудіокниги вже існує");
    }

    @Test
    void shouldThrowExceptionWhenCreatingProgressWithNullData() {
        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.create(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Запис прогресу не може бути null");
    }

    @Test
    void shouldThrowExceptionWhenCreatingProgressWithNegativePosition() {
        // Arrange
        testProgress.setPosition(-1);

        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.create(testProgress))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Позиція не може бути від'ємною");
    }

    // Update method tests
    @Test
    void shouldUpdateProgressSuccessfullyWhenValidDataProvided() {
        // Arrange
        when(listeningProgressRepository.findById(testProgressId)).thenReturn(Optional.of(testProgress));
        doNothing().when(persistenceContext).registerUpdated(eq(testProgressId), any(ListeningProgress.class));
        doNothing().when(persistenceContext).commit();

        // Act
        ListeningProgress result = listeningProgressService.update(testProgressId, testProgress);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testProgressId);
        verify(persistenceContext).registerUpdated(eq(testProgressId), any(ListeningProgress.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProgress() {
        // Arrange
        when(listeningProgressRepository.findById(testProgressId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.update(testProgressId, testProgress))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Запис прогресу з ідентифікатором " + testProgressId + " не існує");
    }

    // Delete method tests
    @Test
    void shouldDeleteProgressSuccessfullyWhenProgressExists() {
        // Arrange
        when(listeningProgressRepository.findById(testProgressId)).thenReturn(Optional.of(testProgress));
        doNothing().when(persistenceContext).registerDeleted(testProgress);
        doNothing().when(persistenceContext).commit();

        // Act
        listeningProgressService.delete(testProgressId);

        // Assert
        verify(persistenceContext).registerDeleted(testProgress);
        verify(persistenceContext).commit();
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentProgress() {
        // Arrange
        when(listeningProgressRepository.findById(testProgressId)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        listeningProgressService.delete(testProgressId);
        verify(persistenceContext, never()).registerDeleted(any());
    }

    // Find methods tests
    @Test
    void shouldFindProgressByIdWhenProgressExists() {
        // Arrange
        when(listeningProgressRepository.findById(testProgressId)).thenReturn(Optional.of(testProgress));

        // Act
        Optional<ListeningProgress> result = listeningProgressService.findById(testProgressId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testProgress);
    }

    @Test
    void shouldFindProgressByUserId() {
        // Arrange
        List<ListeningProgress> progressList = Arrays.asList(testProgress);
        when(listeningProgressRepository.findByUserId(testUserId)).thenReturn(progressList);

        // Act
        List<ListeningProgress> result = listeningProgressService.findByUserId(testUserId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testProgress);
    }

    @Test
    void shouldFindProgressByUserIdAndAudiobookId() {
        // Arrange
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.of(testProgress));

        // Act
        Optional<ListeningProgress> result = listeningProgressService.findByUserIdAndAudiobookId(testUserId, testAudiobookId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testProgress);
    }

    // Update progress method tests
    @Test
    void shouldUpdateProgressWhenExistingProgressFound() {
        // Arrange
        int newPosition = 2400; // 40 minutes
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.of(testProgress));
        doNothing().when(persistenceContext).registerUpdated(eq(testProgress.getId()), any(ListeningProgress.class));
        doNothing().when(persistenceContext).commit();

        // Act
        ListeningProgress result = listeningProgressService.updateProgress(testUserId, testAudiobookId, newPosition);

        // Assert
        assertThat(result.getPosition()).isEqualTo(newPosition);
        verify(persistenceContext).registerUpdated(eq(testProgress.getId()), any(ListeningProgress.class));
    }

    @Test
    void shouldCreateNewProgressWhenNoExistingProgressFound() {
        // Arrange
        int newPosition = 1200; // 20 minutes
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(createTestUser()));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(testAudiobook));
        doNothing().when(persistenceContext).registerNew(any(ListeningProgress.class));
        doNothing().when(persistenceContext).commit();

        // Act
        ListeningProgress result = listeningProgressService.updateProgress(testUserId, testAudiobookId, newPosition);

        // Assert
        assertThat(result.getPosition()).isEqualTo(newPosition);
        verify(persistenceContext).registerNew(any(ListeningProgress.class));
    }

    // Mark as completed tests
    @Test
    void shouldMarkAsCompletedSuccessfully() {
        // Arrange
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(testAudiobook));
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.of(testProgress));
        doNothing().when(persistenceContext).registerUpdated(eq(testProgress.getId()), any(ListeningProgress.class));
        doNothing().when(persistenceContext).commit();

        // Act
        ListeningProgress result = listeningProgressService.markAsCompleted(testUserId, testAudiobookId);

        // Assert
        assertThat(result.getPosition()).isEqualTo(testAudiobook.getDuration());
    }

    @Test
    void shouldThrowExceptionWhenMarkingNonExistentAudiobookAsCompleted() {
        // Arrange
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.markAsCompleted(testUserId, testAudiobookId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Аудіокнига з ідентифікатором " + testAudiobookId + " не існує");
    }

    // Reset progress tests
    @Test
    void shouldResetProgressSuccessfully() {
        // Arrange
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.of(testProgress));
        doNothing().when(persistenceContext).registerUpdated(eq(testProgress.getId()), any(ListeningProgress.class));
        doNothing().when(persistenceContext).commit();

        // Act
        ListeningProgress result = listeningProgressService.resetProgress(testUserId, testAudiobookId);

        // Assert
        assertThat(result.getPosition()).isEqualTo(0);
    }

    // Calculate progress percentage tests
    @Test
    void shouldCalculateProgressPercentageCorrectly() {
        // Arrange
        testProgress.setPosition(1800); // 30 minutes of 60 minutes = 50%
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.of(testProgress));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(testAudiobook));

        // Act
        double result = listeningProgressService.calculateProgressPercentage(testUserId, testAudiobookId);

        // Assert
        assertThat(result).isEqualTo(50.0);
    }

    @Test
    void shouldReturnZeroPercentageWhenNoProgressExists() {
        // Arrange
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.empty());

        // Act
        double result = listeningProgressService.calculateProgressPercentage(testUserId, testAudiobookId);

        // Assert
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void shouldReturnZeroPercentageWhenAudiobookHasZeroDuration() {
        // Arrange
        testAudiobook.setDuration(0);
        when(listeningProgressRepository.findByUserIdAndAudiobookId(testUserId, testAudiobookId))
                .thenReturn(Optional.of(testProgress));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(testAudiobook));

        // Act
        double result = listeningProgressService.calculateProgressPercentage(testUserId, testAudiobookId);

        // Assert
        assertThat(result).isEqualTo(0.0);
    }

    // Count methods tests
    @Test
    void shouldCountProgressByUserId() {
        // Arrange
        when(listeningProgressRepository.countByUserId(testUserId)).thenReturn(5L);

        // Act
        long result = listeningProgressService.countByUserId(testUserId);

        // Assert
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void shouldCountCompletedByUserId() {
        // Arrange
        when(listeningProgressRepository.countCompletedByUserId(testUserId)).thenReturn(3L);

        // Act
        long result = listeningProgressService.countCompletedByUserId(testUserId);

        // Assert
        assertThat(result).isEqualTo(3L);
    }

    // Validation tests
    @Test
    void shouldThrowExceptionForNullUserId() {
        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.updateProgress(null, testAudiobookId, 100))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ідентифікатор користувача не може бути null");
    }

    @Test
    void shouldThrowExceptionForNullAudiobookId() {
        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.updateProgress(testUserId, null, 100))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ідентифікатор аудіокниги не може бути null");
    }

    @Test
    void shouldThrowExceptionForNegativePosition() {
        // Act & Assert
        assertThatThrownBy(() -> listeningProgressService.updateProgress(testUserId, testAudiobookId, -1))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Позиція не може бути від'ємною");
    }

    // Helper methods
    private com.arakviel.domain.entities.User createTestUser() {
        return new com.arakviel.domain.entities.User(
                testUserId,
                "testuser",
                "hashedpassword",
                "test@example.com",
                null
        );
    }
}
