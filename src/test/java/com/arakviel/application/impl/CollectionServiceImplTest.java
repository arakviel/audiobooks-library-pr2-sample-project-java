package com.arakviel.application.impl;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Collection;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
 * Тести для {@link CollectionServiceImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CollectionServiceImplTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private AudiobookRepository audiobookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersistenceContext persistenceContext;

    private CollectionServiceImpl collectionService;
    private Collection testCollection;
    private UUID testCollectionId;
    private UUID testUserId;
    private UUID testAudiobookId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        collectionService = new CollectionServiceImpl(
                collectionRepository,
                audiobookRepository,
                userRepository,
                persistenceContext
        );

        testCollectionId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testAudiobookId = UUID.randomUUID();
        testCollection = new Collection(
                testCollectionId,
                testUserId,
                "Test Collection",
                LocalDateTime.now()
        );
    }

    // Create method tests
    @Test
    void shouldCreateCollectionSuccessfullyWhenValidDataProvided() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(createTestUser()));
        when(collectionRepository.existsByUserIdAndName(testUserId, testCollection.getName())).thenReturn(false);
        doNothing().when(persistenceContext).registerNew(any(Collection.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Collection result = collectionService.create(testCollection);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        verify(persistenceContext).registerNew(any(Collection.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenCreatingCollectionForNonExistentUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> collectionService.create(testCollection))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з ідентифікатором " + testUserId + " не існує");
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateCollection() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(createTestUser()));
        when(collectionRepository.existsByUserIdAndName(testUserId, testCollection.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> collectionService.create(testCollection))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Колекція з назвою '" + testCollection.getName() + "' вже існує у користувача");
    }

    @Test
    void shouldThrowExceptionWhenCreatingCollectionWithNullData() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.create(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Колекція не може бути null");
    }

    @Test
    void shouldThrowExceptionWhenCreatingCollectionWithEmptyName() {
        // Arrange
        testCollection.setName("");

        // Act & Assert
        assertThatThrownBy(() -> collectionService.create(testCollection))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Назва колекції не може бути порожньою");
    }

    // Update method tests
    @Test
    void shouldUpdateCollectionSuccessfullyWhenValidDataProvided() {
        // Arrange
        Collection updatedCollection = new Collection(
                testCollectionId,
                testUserId,
                "Updated Collection",
                LocalDateTime.now()
        );

        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.of(testCollection));
        when(collectionRepository.existsByUserIdAndName(testUserId, updatedCollection.getName())).thenReturn(false);
        doNothing().when(persistenceContext).registerUpdated(eq(testCollectionId), any(Collection.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Collection result = collectionService.update(testCollectionId, updatedCollection);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testCollectionId);
        verify(persistenceContext).registerUpdated(eq(testCollectionId), any(Collection.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCollection() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> collectionService.update(testCollectionId, testCollection))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Колекція з ідентифікатором " + testCollectionId + " не існує");
    }

    // Delete method tests
    @Test
    void shouldDeleteCollectionSuccessfullyWhenCollectionExists() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.of(testCollection));
        doNothing().when(persistenceContext).registerDeleted(testCollection);
        doNothing().when(persistenceContext).commit();

        // Act
        collectionService.delete(testCollectionId);

        // Assert
        verify(persistenceContext).registerDeleted(testCollection);
        verify(persistenceContext).commit();
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentCollection() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        collectionService.delete(testCollectionId);
        verify(persistenceContext, never()).registerDeleted(any());
    }

    // Find methods tests
    @Test
    void shouldFindCollectionByIdWhenCollectionExists() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.of(testCollection));

        // Act
        Optional<Collection> result = collectionService.findById(testCollectionId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testCollection);
    }

    @Test
    void shouldFindCollectionsByUserId() {
        // Arrange
        List<Collection> collections = Arrays.asList(testCollection);
        when(collectionRepository.findByUserId(testUserId)).thenReturn(collections);

        // Act
        List<Collection> result = collectionService.findByUserId(testUserId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCollection);
    }

    @Test
    void shouldFindCollectionsByPartialName() {
        // Arrange
        String partialName = "Test";
        List<Collection> collections = Arrays.asList(testCollection);
        when(collectionRepository.findByPartialName(partialName)).thenReturn(collections);

        // Act
        List<Collection> result = collectionService.findByPartialName(partialName);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCollection);
    }

    // Audiobook management tests
    @Test
    void shouldAddAudiobookToCollectionSuccessfully() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.of(testCollection));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(createTestAudiobook()));
        when(collectionRepository.containsAudiobook(testCollectionId, testAudiobookId)).thenReturn(false);
        doNothing().when(collectionRepository).addAudiobookToCollection(testCollectionId, testAudiobookId);

        // Act
        collectionService.addAudiobookToCollection(testCollectionId, testAudiobookId);

        // Assert
        verify(collectionRepository).addAudiobookToCollection(testCollectionId, testAudiobookId);
    }

    @Test
    void shouldThrowExceptionWhenAddingNonExistentAudiobookToCollection() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.of(testCollection));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> collectionService.addAudiobookToCollection(testCollectionId, testAudiobookId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Аудіокнига з ідентифікатором " + testAudiobookId + " не існує");
    }

    @Test
    void shouldThrowExceptionWhenAddingAudiobookToNonExistentCollection() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> collectionService.addAudiobookToCollection(testCollectionId, testAudiobookId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Колекція з ідентифікатором " + testCollectionId + " не існує");
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateAudiobookToCollection() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.of(testCollection));
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(createTestAudiobook()));
        when(collectionRepository.containsAudiobook(testCollectionId, testAudiobookId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> collectionService.addAudiobookToCollection(testCollectionId, testAudiobookId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Аудіокнига вже є в колекції");
    }

    @Test
    void shouldRemoveAudiobookFromCollectionSuccessfully() {
        // Arrange
        doNothing().when(collectionRepository).removeAudiobookFromCollection(testCollectionId, testAudiobookId);

        // Act
        collectionService.removeAudiobookFromCollection(testCollectionId, testAudiobookId);

        // Assert
        verify(collectionRepository).removeAudiobookFromCollection(testCollectionId, testAudiobookId);
    }

    @Test
    void shouldCheckIfCollectionContainsAudiobook() {
        // Arrange
        when(collectionRepository.containsAudiobook(testCollectionId, testAudiobookId)).thenReturn(true);

        // Act
        boolean result = collectionService.containsAudiobook(testCollectionId, testAudiobookId);

        // Assert
        assertThat(result).isTrue();
    }

    // Count methods tests
    @Test
    void shouldCountAudiobooksByCollectionId() {
        // Arrange
        when(collectionRepository.countAudiobooksByCollectionId(testCollectionId)).thenReturn(5L);

        // Act
        long result = collectionService.countAudiobooksByCollectionId(testCollectionId);

        // Assert
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void shouldCountCollectionsByUserId() {
        // Arrange
        when(collectionRepository.countByUserId(testUserId)).thenReturn(3L);

        // Act
        long result = collectionService.countByUserId(testUserId);

        // Assert
        assertThat(result).isEqualTo(3L);
    }

    // Validation tests
    @Test
    void shouldThrowExceptionForNullCollectionId() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.addAudiobookToCollection(null, testAudiobookId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ідентифікатор колекції не може бути null");
    }

    @Test
    void shouldThrowExceptionForNullAudiobookId() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.addAudiobookToCollection(testCollectionId, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ідентифікатор аудіокниги не може бути null");
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

    private Audiobook createTestAudiobook() {
        return new Audiobook(
                testAudiobookId,
                UUID.randomUUID(), // authorId
                UUID.randomUUID(), // genreId
                "Test Audiobook",
                3600, // duration
                2023,
                "Test description",
                null // coverImagePath
        );
    }
}
