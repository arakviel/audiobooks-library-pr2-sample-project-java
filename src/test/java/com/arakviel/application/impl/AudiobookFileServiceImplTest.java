package com.arakviel.application.impl;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookFileRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Тести для {@link AudiobookFileServiceImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AudiobookFileServiceImplTest {

    @Mock
    private AudiobookFileRepository audiobookFileRepository;

    @Mock
    private AudiobookRepository audiobookRepository;

    @Mock
    private PersistenceContext persistenceContext;

    @Mock
    private FileStorageService fileStorageService;

    private AudiobookFileServiceImpl audiobookFileService;
    private AudiobookFile testFile;
    private UUID testFileId;
    private UUID testAudiobookId;
    private Audiobook testAudiobook;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        audiobookFileService = new AudiobookFileServiceImpl(
                audiobookFileRepository,
                audiobookRepository,
                persistenceContext,
                fileStorageService
        );

        testFileId = UUID.randomUUID();
        testAudiobookId = UUID.randomUUID();
        testFile = new AudiobookFile(
                testFileId,
                testAudiobookId,
                "/storage/audiobook.mp3",
                FileFormat.MP3,
                1024000 // 1MB
        );

        testAudiobook = new Audiobook(
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

    // Create method tests
    @Test
    void shouldCreateFileSuccessfullyWhenValidDataProvided() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("audio content".getBytes());
        String fileName = "audiobook.mp3";
        Path savedPath = Paths.get("/storage/audiobook.mp3");

        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(testAudiobook));
        when(audiobookFileRepository.existsByAudiobookIdAndFilePath(testAudiobookId, testFile.getFilePath())).thenReturn(false);
        when(fileStorageService.save(inputStream, fileName, testAudiobookId)).thenReturn(savedPath);
        doNothing().when(persistenceContext).registerNew(any(AudiobookFile.class));
        doNothing().when(persistenceContext).commit();

        // Act
        AudiobookFile result = audiobookFileService.create(testFile, inputStream, fileName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getFilePath()).isEqualTo(savedPath.toString());
        verify(fileStorageService).save(inputStream, fileName, testAudiobookId);
        verify(persistenceContext).registerNew(any(AudiobookFile.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenCreatingFileForNonExistentAudiobook() {
        // Arrange
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.create(testFile, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Аудіокнига з ідентифікатором " + testAudiobookId + " не існує");
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateFile() {
        // Arrange
        when(audiobookRepository.findById(testAudiobookId)).thenReturn(Optional.of(testAudiobook));
        when(audiobookFileRepository.existsByAudiobookIdAndFilePath(testAudiobookId, testFile.getFilePath())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.create(testFile, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Файл з таким шляхом вже існує для цієї аудіокниги");
    }

    @Test
    void shouldThrowExceptionWhenCreatingFileWithNullData() {
        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.create(null, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Файл аудіокниги не може бути null");
    }

    @Test
    void shouldThrowExceptionWhenCreatingFileWithNegativeSize() {
        // Arrange
        testFile.setSize(-1);

        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.create(testFile, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Розмір файлу не може бути від'ємним");
    }

    // Update method tests
    @Test
    void shouldUpdateFileSuccessfullyWhenValidDataProvided() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("new audio content".getBytes());
        String fileName = "new_audiobook.mp3";
        Path savedPath = Paths.get("/storage/new_audiobook.mp3");

        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.of(testFile));
        when(fileStorageService.save(inputStream, fileName, testAudiobookId)).thenReturn(savedPath);
        doNothing().when(fileStorageService).delete(anyString(), eq(testAudiobookId));
        doNothing().when(persistenceContext).registerUpdated(eq(testFileId), any(AudiobookFile.class));
        doNothing().when(persistenceContext).commit();

        // Act
        AudiobookFile result = audiobookFileService.update(testFileId, testFile, inputStream, fileName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testFileId);
        verify(fileStorageService).delete("audiobook.mp3", testAudiobookId);
        verify(fileStorageService).save(inputStream, fileName, testAudiobookId);
        verify(persistenceContext).registerUpdated(eq(testFileId), any(AudiobookFile.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFile() {
        // Arrange
        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.update(testFileId, testFile, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Файл аудіокниги з ідентифікатором " + testFileId + " не існує");
    }

    // Delete method tests
    @Test
    void shouldDeleteFileSuccessfullyWhenFileExists() {
        // Arrange
        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.of(testFile));
        doNothing().when(fileStorageService).delete("audiobook.mp3", testAudiobookId);
        doNothing().when(persistenceContext).registerDeleted(testFile);
        doNothing().when(persistenceContext).commit();

        // Act
        audiobookFileService.delete(testFileId);

        // Assert
        verify(fileStorageService).delete("audiobook.mp3", testAudiobookId);
        verify(persistenceContext).registerDeleted(testFile);
        verify(persistenceContext).commit();
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentFile() {
        // Arrange
        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        audiobookFileService.delete(testFileId);
        verify(persistenceContext, never()).registerDeleted(any());
    }

    // Find methods tests
    @Test
    void shouldFindFileByIdWhenFileExists() {
        // Arrange
        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.of(testFile));

        // Act
        Optional<AudiobookFile> result = audiobookFileService.findById(testFileId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testFile);
    }

    @Test
    void shouldFindFilesByAudiobookId() {
        // Arrange
        List<AudiobookFile> files = Arrays.asList(testFile);
        when(audiobookFileRepository.findByAudiobookId(testAudiobookId)).thenReturn(files);

        // Act
        List<AudiobookFile> result = audiobookFileService.findByAudiobookId(testAudiobookId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testFile);
    }

    @Test
    void shouldFindFilesByFormat() {
        // Arrange
        List<AudiobookFile> files = Arrays.asList(testFile);
        when(audiobookFileRepository.findByFormat(FileFormat.MP3)).thenReturn(files);

        // Act
        List<AudiobookFile> result = audiobookFileService.findByFormat(FileFormat.MP3);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testFile);
    }

    @Test
    void shouldFindFilesByAudiobookIdAndFormat() {
        // Arrange
        List<AudiobookFile> files = Arrays.asList(testFile);
        when(audiobookFileRepository.findByAudiobookIdAndFormat(testAudiobookId, FileFormat.MP3)).thenReturn(files);

        // Act
        List<AudiobookFile> result = audiobookFileService.findByAudiobookIdAndFormat(testAudiobookId, FileFormat.MP3);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testFile);
    }

    // Count methods tests
    @Test
    void shouldCountFilesByAudiobookId() {
        // Arrange
        when(audiobookFileRepository.countByAudiobookId(testAudiobookId)).thenReturn(3L);

        // Act
        long result = audiobookFileService.countByAudiobookId(testAudiobookId);

        // Assert
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void shouldCountFilesByFormat() {
        // Arrange
        when(audiobookFileRepository.countByFormat(FileFormat.MP3)).thenReturn(5L);

        // Act
        long result = audiobookFileService.countByFormat(FileFormat.MP3);

        // Assert
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void shouldCalculateTotalSizeByAudiobookId() {
        // Arrange
        when(audiobookFileRepository.calculateTotalSizeByAudiobookId(testAudiobookId)).thenReturn(5120000L);

        // Act
        long result = audiobookFileService.calculateTotalSizeByAudiobookId(testAudiobookId);

        // Assert
        assertThat(result).isEqualTo(5120000L);
    }

    // File size methods tests
    @Test
    void shouldFindLargestFileByAudiobookId() {
        // Arrange
        when(audiobookFileRepository.findLargestFileByAudiobookId(testAudiobookId)).thenReturn(Optional.of(testFile));

        // Act
        Optional<AudiobookFile> result = audiobookFileService.findLargestFileByAudiobookId(testAudiobookId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testFile);
    }

    @Test
    void shouldFindSmallestFileByAudiobookId() {
        // Arrange
        when(audiobookFileRepository.findSmallestFileByAudiobookId(testAudiobookId)).thenReturn(Optional.of(testFile));

        // Act
        Optional<AudiobookFile> result = audiobookFileService.findSmallestFileByAudiobookId(testAudiobookId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testFile);
    }

    // Existence check tests
    @Test
    void shouldCheckIfFileExistsByAudiobookIdAndFilePath() {
        // Arrange
        when(audiobookFileRepository.existsByAudiobookIdAndFilePath(testAudiobookId, testFile.getFilePath())).thenReturn(true);

        // Act
        boolean result = audiobookFileService.existsByAudiobookIdAndFilePath(testAudiobookId, testFile.getFilePath());

        // Assert
        assertThat(result).isTrue();
    }

    // Delete all methods tests
    @Test
    void shouldDeleteAllFilesByAudiobookId() {
        // Arrange
        List<AudiobookFile> files = Arrays.asList(testFile);
        when(audiobookFileRepository.findByAudiobookId(testAudiobookId)).thenReturn(files);
        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.of(testFile));
        doNothing().when(fileStorageService).delete(anyString(), eq(testAudiobookId));
        doNothing().when(persistenceContext).registerDeleted(any(AudiobookFile.class));
        doNothing().when(persistenceContext).commit();

        // Act
        audiobookFileService.deleteAllByAudiobookId(testAudiobookId);

        // Assert
        verify(fileStorageService).delete("audiobook.mp3", testAudiobookId);
        verify(persistenceContext).registerDeleted(testFile);
        verify(persistenceContext).commit();
    }

    // Copy file tests
    @Test
    void shouldCopyFileSuccessfully() {
        // Arrange
        UUID targetAudiobookId = UUID.randomUUID();
        String newFileName = "copied_audiobook.mp3";

        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.of(testFile));
        when(audiobookRepository.findById(targetAudiobookId)).thenReturn(Optional.of(testAudiobook));
        doNothing().when(persistenceContext).registerNew(any(AudiobookFile.class));
        doNothing().when(persistenceContext).commit();

        // Act
        AudiobookFile result = audiobookFileService.copyFile(testFileId, targetAudiobookId, newFileName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAudiobookId()).isEqualTo(targetAudiobookId);
        assertThat(result.getFormat()).isEqualTo(testFile.getFormat());
        assertThat(result.getSize()).isEqualTo(testFile.getSize());
        verify(persistenceContext).registerNew(any(AudiobookFile.class));
        verify(persistenceContext).commit();
    }

    @Test
    void shouldThrowExceptionWhenCopyingNonExistentFile() {
        // Arrange
        UUID targetAudiobookId = UUID.randomUUID();
        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.copyFile(testFileId, targetAudiobookId, "new_file.mp3"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Вихідний файл не існує");
    }

    // Change format tests
    @Test
    void shouldChangeFormatSuccessfully() {
        // Arrange
        FileFormat newFormat = FileFormat.FLAC;
        when(audiobookFileRepository.findById(testFileId)).thenReturn(Optional.of(testFile));
        doNothing().when(persistenceContext).registerUpdated(eq(testFileId), any(AudiobookFile.class));
        doNothing().when(persistenceContext).commit();

        // Act
        AudiobookFile result = audiobookFileService.changeFormat(testFileId, newFormat);

        // Assert
        assertThat(result.getFormat()).isEqualTo(newFormat);
        verify(persistenceContext).registerUpdated(eq(testFileId), any(AudiobookFile.class));
        verify(persistenceContext).commit();
    }

    // Statistics tests
    @Test
    void shouldGetFormatStatistics() {
        // Arrange
        when(audiobookFileRepository.countByFormat(FileFormat.MP3)).thenReturn(10L);
        when(audiobookFileRepository.countByFormat(FileFormat.OGG)).thenReturn(8L);
        when(audiobookFileRepository.countByFormat(FileFormat.WAV)).thenReturn(5L);
        when(audiobookFileRepository.countByFormat(FileFormat.M4B)).thenReturn(3L);
        when(audiobookFileRepository.countByFormat(FileFormat.AAC)).thenReturn(4L);
        when(audiobookFileRepository.countByFormat(FileFormat.FLAC)).thenReturn(2L);

        // Act
        Map<FileFormat, Long> result = audiobookFileService.getFormatStatistics();

        // Assert
        assertThat(result).hasSize(6);
        assertThat(result.get(FileFormat.MP3)).isEqualTo(10L);
        assertThat(result.get(FileFormat.OGG)).isEqualTo(8L);
        assertThat(result.get(FileFormat.WAV)).isEqualTo(5L);
        assertThat(result.get(FileFormat.M4B)).isEqualTo(3L);
        assertThat(result.get(FileFormat.AAC)).isEqualTo(4L);
        assertThat(result.get(FileFormat.FLAC)).isEqualTo(2L);
    }

    @Test
    void shouldFindFilesByAudiobookIdOrderBySize() {
        // Arrange
        List<AudiobookFile> files = Arrays.asList(testFile);
        when(audiobookFileRepository.findByAudiobookIdOrderBySize(testAudiobookId, true)).thenReturn(files);

        // Act
        List<AudiobookFile> result = audiobookFileService.findByAudiobookIdOrderBySize(testAudiobookId, true);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testFile);
    }

    @Test
    void shouldFindPotentialDuplicates() {
        // Arrange
        List<AudiobookFile> duplicates = Arrays.asList(testFile);
        when(audiobookFileRepository.findPotentialDuplicates(testAudiobookId)).thenReturn(duplicates);

        // Act
        List<AudiobookFile> result = audiobookFileService.findPotentialDuplicates(testAudiobookId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testFile);
    }

    // Validation tests
    @Test
    void shouldThrowExceptionForNullAudiobookId() {
        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.findByAudiobookId(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ідентифікатор аудіокниги не може бути null");
    }

    @Test
    void shouldThrowExceptionForNullFormat() {
        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.findByFormat(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Формат файлу не може бути null");
    }

    @Test
    void shouldThrowExceptionForEmptyFilePath() {
        // Act & Assert
        assertThatThrownBy(() -> audiobookFileService.existsByAudiobookIdAndFilePath(testAudiobookId, ""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Шлях до файлу не може бути порожнім");
    }
}
