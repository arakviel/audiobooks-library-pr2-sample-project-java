package com.arakviel.application.impl;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.file.exception.FileStorageException;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookFileRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudiobookServiceImplTest {

    @Mock
    private AudiobookRepository audiobookRepository;

    @Mock
    private AudiobookFileRepository audiobookFileRepository;

    @Mock
    private PersistenceContext persistenceContext;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AudiobookServiceImpl audiobookService;

    private UUID audiobookId;
    private Audiobook audiobook;
    private InputStream coverImage;
    private String coverImageName;
    private Path coverImagePath;

    @BeforeEach
    void setUp() {
        audiobookId = UUID.randomUUID();
        audiobook = new Audiobook();
        coverImage = mock(InputStream.class);
        coverImageName = "cover.jpg";
        coverImagePath = Path.of("covers/" + audiobookId + "/" + coverImageName);
    }

    // Tests for create method
    @Test
    void givenAudiobookWithoutCover_whenCreatingAudiobook_thenShouldCreateSuccessfully() {
        // Arrange
        doNothing().when(persistenceContext).registerNew(any(Audiobook.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Audiobook result = audiobookService.create(audiobook, null, null);

        // Assert
        assertNotNull(result.getId());
        verify(persistenceContext).registerNew(audiobook);
        verify(persistenceContext).commit();
        verify(fileStorageService, never()).save(any(), any(), any());
    }

    @Test
    void givenAudiobookWithCover_whenCreatingAudiobook_thenShouldCreateWithCover() {
        // Arrange
        audiobook.setId(audiobookId);
        when(fileStorageService.save(coverImage, coverImageName, audiobookId)).thenReturn(coverImagePath);
        doNothing().when(persistenceContext).registerNew(any(Audiobook.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Audiobook result = audiobookService.create(audiobook, coverImage, coverImageName);

        // Assert
        assertEquals(coverImagePath.toString(), result.getCoverImagePath());
        verify(fileStorageService).save(coverImage, coverImageName, audiobookId);
        verify(persistenceContext).registerNew(audiobook);
        verify(persistenceContext).commit();
    }

    @Test
    void givenDatabaseError_whenCreatingAudiobook_thenShouldThrowDatabaseAccessException() {
        // Arrange
        doThrow(DatabaseAccessException.class).when(persistenceContext).commit();

        // Act & Assert
        assertThrows(DatabaseAccessException.class, () ->
                audiobookService.create(audiobook, null, null));
        verify(persistenceContext).registerNew(audiobook);
        verify(persistenceContext).commit();
    }

    // Tests for update method
    @Test
    void givenAudiobookWithoutCover_whenUpdatingAudiobook_thenShouldUpdateSuccessfully() {
        // Arrange
        doNothing().when(persistenceContext).registerUpdated(any(UUID.class), any(Audiobook.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Audiobook result = audiobookService.update(audiobookId, audiobook, null, null);

        // Assert
        assertEquals(audiobookId, result.getId());
        verify(persistenceContext).registerUpdated(audiobookId, audiobook);
        verify(persistenceContext).commit();
        verify(fileStorageService, never()).save(any(), any(), any());
        verify(fileStorageService, never()).delete(any(), any());
    }

    @Test
    void givenAudiobookWithExistingAndNewCover_whenUpdatingAudiobook_thenShouldReplaceCover() {
        // Arrange
        audiobook.setCoverImagePath("old-cover.jpg");
        doNothing().when(fileStorageService).delete("old-cover.jpg", audiobookId);
        when(fileStorageService.save(coverImage, coverImageName, audiobookId)).thenReturn(coverImagePath);
        doNothing().when(persistenceContext).registerUpdated(any(UUID.class), any(Audiobook.class));
        doNothing().when(persistenceContext).commit();

        // Act
        Audiobook result = audiobookService.update(audiobookId, audiobook, coverImage, coverImageName);

        // Assert
        assertEquals(coverImagePath.toString(), result.getCoverImagePath());
        verify(fileStorageService).delete("old-cover.jpg", audiobookId);
        verify(fileStorageService).save(coverImage, coverImageName, audiobookId);
        verify(persistenceContext).registerUpdated(audiobookId, audiobook);
        verify(persistenceContext).commit();
    }

    @Test
    void givenFileStorageError_whenUpdatingAudiobookWithCover_thenShouldThrowFileStorageException() {
        // Arrange
        audiobook.setCoverImagePath("old-cover.jpg");
        doNothing().when(fileStorageService).delete("old-cover.jpg", audiobookId);
        doThrow(FileStorageException.class).when(fileStorageService).delete("old-cover.jpg", audiobookId);

        // Act & Assert
        assertThrows(FileStorageException.class, () ->
                audiobookService.update(audiobookId, audiobook, coverImage, coverImageName));
        verify(fileStorageService).delete("old-cover.jpg", audiobookId);
        verify(fileStorageService, never()).save(any(), any(), any());
    }

    // Tests for delete method
    @Test
    void givenExistingAudiobookWithoutCoverOrFiles_whenDeletingAudiobook_thenShouldDeleteSuccessfully() {
        // Arrange
        audiobook.setId(audiobookId);
        when(audiobookRepository.findById(audiobookId)).thenReturn(Optional.of(audiobook));
        when(audiobookFileRepository.findByAudiobookId(audiobookId)).thenReturn(List.of());
        doNothing().when(persistenceContext).registerDeleted(any(Audiobook.class));
        doNothing().when(persistenceContext).commit();

        // Act
        audiobookService.delete(audiobookId);

        // Assert
        verify(audiobookRepository).findById(audiobookId);
        verify(persistenceContext).registerDeleted(audiobook);
        verify(persistenceContext).commit();
        verify(fileStorageService, never()).delete(any(), any());
    }

    @Test
    void givenExistingAudiobookWithCoverAndFiles_whenDeletingAudiobook_thenShouldDeleteAll() {
        // Arrange
        audiobook.setId(audiobookId);
        audiobook.setCoverImagePath("cover.jpg");
        AudiobookFile file = new AudiobookFile(UUID.randomUUID(), audiobookId, "file.mp3", FileFormat.MP3, 1000);
        when(audiobookRepository.findById(audiobookId)).thenReturn(Optional.of(audiobook));
        when(audiobookFileRepository.findByAudiobookId(audiobookId)).thenReturn(List.of(file));
        doNothing().when(fileStorageService).delete("cover.jpg", audiobookId);
        doNothing().when(fileStorageService).delete("file.mp3", audiobookId);
        doNothing().when(persistenceContext).registerDeleted(any());
        doNothing().when(persistenceContext).commit();

        // Act
        audiobookService.delete(audiobookId);

        // Assert
        verify(fileStorageService).delete("cover.jpg", audiobookId);
        verify(fileStorageService).delete("file.mp3", audiobookId);
        verify(persistenceContext).registerDeleted(file);
        verify(persistenceContext).registerDeleted(audiobook);
        verify(persistenceContext).commit();
    }

    @Test
    void givenNonExistingAudiobook_whenDeletingAudiobook_thenShouldDoNothing() {
        // Arrange
        when(audiobookRepository.findById(audiobookId)).thenReturn(Optional.empty());

        // Act
        audiobookService.delete(audiobookId);

        // Assert
        verify(audiobookRepository).findById(audiobookId);
        verify(persistenceContext, never()).registerDeleted(any());
        verify(persistenceContext, never()).commit();
        verify(fileStorageService, never()).delete(any(), any());
    }

    // Tests for uploadAudiobookFile method
    @Test
    void givenValidAudiobookFile_whenUploadingFile_thenShouldUploadSuccessfully() {
        // Arrange
        String fileName = "audio.mp3";
        FileFormat format = FileFormat.MP3;
        int size = 1000;
        Path filePath = Path.of("files/" + audiobookId + "/" + fileName);
        when(fileStorageService.save(coverImage, fileName, audiobookId)).thenReturn(filePath);
        doNothing().when(persistenceContext).registerNew(any(AudiobookFile.class));
        doNothing().when(persistenceContext).commit();

        // Act
        AudiobookFile result = audiobookService.uploadAudiobookFile(audiobookId, coverImage, fileName, format, size);

        // Assert
        assertNotNull(result.getId());
        assertEquals(filePath.toString(), result.getFilePath());
        assertEquals(audiobookId, result.getAudiobookId());
        verify(fileStorageService).save(coverImage, fileName, audiobookId);
        verify(persistenceContext).registerNew(any(AudiobookFile.class));
        verify(persistenceContext).commit();
    }

    @Test
    void givenFileStorageError_whenUploadingFile_thenShouldThrowFileStorageException() {
        // Arrange
        String fileName = "audio.mp3";
        FileFormat format = FileFormat.MP3;
        int size = 1000;
        when(fileStorageService.save(coverImage, fileName, audiobookId)).thenThrow(FileStorageException.class);

        // Act & Assert
        assertThrows(FileStorageException.class, () ->
                audiobookService.uploadAudiobookFile(audiobookId, coverImage, fileName, format, size));
        verify(fileStorageService).save(coverImage, fileName, audiobookId);
        verify(persistenceContext, never()).registerNew(any());
    }
}