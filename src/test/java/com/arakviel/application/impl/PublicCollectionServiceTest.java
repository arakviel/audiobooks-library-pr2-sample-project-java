package com.arakviel.application.impl;

import com.arakviel.application.contract.CollectionService;
import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Collection;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тести для функціоналу публічних колекцій в CollectionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class PublicCollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private AudiobookRepository audiobookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersistenceContext persistenceContext;

    private CollectionService collectionService;
    private Collection testPublicCollection;
    private UUID testCollectionId;

    @BeforeEach
    void setUp() {
        collectionService = new CollectionServiceImpl(
                collectionRepository,
                audiobookRepository,
                userRepository,
                persistenceContext
        );

        testCollectionId = UUID.randomUUID();
        testPublicCollection = new Collection(
                testCollectionId,
                null, // userId = null для публічної колекції
                "Популярна фантастика",
                LocalDateTime.now()
        );
    }

    // ========== CREATE PUBLIC COLLECTION TESTS ==========

    @Test
    void shouldCreatePublicCollectionSuccessfully() {
        // Arrange
        when(collectionRepository.existsPublicCollectionByName(testPublicCollection.getName())).thenReturn(false);
        doNothing().when(persistenceContext).registerNew(any(Collection.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Collection result = collectionService.createPublicCollection(testPublicCollection);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isNull(); // Публічна колекція
        assertThat(result.getName()).isEqualTo(testPublicCollection.getName());
        assertThat(result.getCreatedAt()).isNotNull();
        verify(persistenceContext).registerNew(any(Collection.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicatePublicCollection() {
        // Arrange
        when(collectionRepository.existsPublicCollectionByName(testPublicCollection.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> collectionService.createPublicCollection(testPublicCollection))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Публічна колекція з назвою '" + testPublicCollection.getName() + "' вже існує");
    }

    @Test
    void shouldThrowExceptionWhenCreatingPublicCollectionWithNullName() {
        // Arrange
        testPublicCollection.setName(null);

        // Act & Assert
        assertThatThrownBy(() -> collectionService.createPublicCollection(testPublicCollection))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Назва колекції не може бути порожньою");
    }

    @Test
    void shouldThrowExceptionWhenCreatingPublicCollectionWithEmptyName() {
        // Arrange
        testPublicCollection.setName("   ");

        // Act & Assert
        assertThatThrownBy(() -> collectionService.createPublicCollection(testPublicCollection))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Назва колекції не може бути порожньою");
    }

    // ========== FIND PUBLIC COLLECTIONS TESTS ==========

    @Test
    void shouldFindPublicCollectionsWithPagination() {
        // Arrange
        List<Collection> publicCollections = Arrays.asList(
                new Collection(UUID.randomUUID(), null, "Колекція 1", LocalDateTime.now()),
                new Collection(UUID.randomUUID(), null, "Колекція 2", LocalDateTime.now())
        );
        when(collectionRepository.findPublicCollections(0, 10)).thenReturn(publicCollections);

        // Act
        List<Collection> result = collectionService.findPublicCollections(0, 10);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(collection -> collection.getUserId() == null);
    }

    @Test
    void shouldFindPublicCollectionsByName() {
        // Arrange
        List<Collection> publicCollections = Arrays.asList(testPublicCollection);
        when(collectionRepository.findPublicCollectionsByName(testPublicCollection.getName()))
                .thenReturn(publicCollections);

        // Act
        List<Collection> result = collectionService.findPublicCollectionsByName(testPublicCollection.getName());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(testPublicCollection.getName());
        assertThat(result.get(0).getUserId()).isNull();
    }

    @Test
    void shouldFindPublicCollectionsByPartialName() {
        // Arrange
        String partialName = "фантаст";
        List<Collection> publicCollections = Arrays.asList(testPublicCollection);
        when(collectionRepository.findPublicCollectionsByPartialName(partialName))
                .thenReturn(publicCollections);

        // Act
        List<Collection> result = collectionService.findPublicCollectionsByPartialName(partialName);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("фантастика");
        assertThat(result.get(0).getUserId()).isNull();
    }

    // ========== COUNT AND EXISTS TESTS ==========

    @Test
    void shouldCountPublicCollections() {
        // Arrange
        when(collectionRepository.countPublicCollections()).thenReturn(5L);

        // Act
        long result = collectionService.countPublicCollections();

        // Assert
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void shouldReturnTrueWhenPublicCollectionExists() {
        // Arrange
        when(collectionRepository.existsPublicCollectionByName(testPublicCollection.getName())).thenReturn(true);

        // Act
        boolean result = collectionService.existsPublicCollectionByName(testPublicCollection.getName());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPublicCollectionDoesNotExist() {
        // Arrange
        when(collectionRepository.existsPublicCollectionByName("Неіснуюча колекція")).thenReturn(false);

        // Act
        boolean result = collectionService.existsPublicCollectionByName("Неіснуюча колекція");

        // Assert
        assertThat(result).isFalse();
    }

    // ========== IS PUBLIC COLLECTION TESTS ==========

    @Test
    void shouldReturnTrueForPublicCollection() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.of(testPublicCollection));

        // Act
        boolean result = collectionService.isPublicCollection(testCollectionId);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForPrivateCollection() {
        // Arrange
        Collection privateCollection = new Collection(testCollectionId, UUID.randomUUID(), "Приватна", LocalDateTime.now());
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.of(privateCollection));

        // Act
        boolean result = collectionService.isPublicCollection(testCollectionId);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenCheckingNonExistentCollection() {
        // Arrange
        when(collectionRepository.findById(testCollectionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> collectionService.isPublicCollection(testCollectionId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Колекція з ідентифікатором " + testCollectionId + " не існує");
    }

    // ========== POPULAR AND RECENT TESTS ==========

    @Test
    void shouldFindMostPopularPublicCollections() {
        // Arrange
        List<Collection> popularCollections = Arrays.asList(testPublicCollection);
        when(collectionRepository.findMostPopularPublicCollections(5)).thenReturn(popularCollections);

        // Act
        List<Collection> result = collectionService.findMostPopularPublicCollections(5);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isNull();
    }

    @Test
    void shouldFindRecentlyCreatedPublicCollections() {
        // Arrange
        List<Collection> recentCollections = Arrays.asList(testPublicCollection);
        when(collectionRepository.findRecentlyCreatedPublicCollections(5)).thenReturn(recentCollections);

        // Act
        List<Collection> result = collectionService.findRecentlyCreatedPublicCollections(5);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isNull();
    }

    // ========== VALIDATION TESTS ==========

    @Test
    void shouldThrowExceptionForInvalidPaginationOffset() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.findPublicCollections(-1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Offset не може бути від'ємним");
    }

    @Test
    void shouldThrowExceptionForInvalidPaginationLimit() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.findPublicCollections(0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Limit повинен бути більше нуля");
    }

    @Test
    void shouldThrowExceptionForNullCollectionName() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.findPublicCollectionsByName(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Назва колекції не може бути порожньою");
    }

    @Test
    void shouldThrowExceptionForEmptyPartialName() {
        // Act & Assert
        assertThatThrownBy(() -> collectionService.findPublicCollectionsByPartialName(""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Часткова назва колекції не може бути порожньою");
    }
}
