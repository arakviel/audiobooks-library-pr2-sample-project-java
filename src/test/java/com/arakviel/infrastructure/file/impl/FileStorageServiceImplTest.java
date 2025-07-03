package com.arakviel.infrastructure.file.impl;

import com.arakviel.infrastructure.file.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тести для {@link FileStorageServiceImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl fileStorageService;
    private UUID testEntityId;
    private String validFileName;
    private String invalidFileName;
    private InputStream testInputStream;

    @BeforeEach
    void setUp() {
        String[] allowedExtensions = {"jpg", "png", "gif", "mp3", "wav"};
        long maxFileSize = 5 * 1024 * 1024; // 5 MB
        
        fileStorageService = new FileStorageServiceImpl(
                tempDir.toString(), 
                allowedExtensions, 
                maxFileSize
        );
        
        testEntityId = UUID.randomUUID();
        validFileName = "test-image.jpg";
        invalidFileName = "test-file.txt";
        testInputStream = new ByteArrayInputStream("test content".getBytes());
    }

    // Constructor and initialization tests
    @Test
    void shouldInitializeStorageDirectoryWhenConstructorCalled() {
        // Arrange & Act
        Path storageRoot = tempDir.resolve("new-storage");
        String[] extensions = {"jpg", "png"};
        
        FileStorageServiceImpl service = new FileStorageServiceImpl(
                storageRoot.toString(), 
                extensions, 
                1024
        );
        
        // Assert
        assertThat(Files.exists(storageRoot)).isTrue();
        assertThat(Files.isDirectory(storageRoot)).isTrue();
    }

    // Save method tests
    @Test
    void shouldSaveFileSuccessfullyWhenValidInputProvided() throws IOException {
        // Arrange
        String content = "test file content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        
        // Act
        Path savedPath = fileStorageService.save(inputStream, validFileName, testEntityId);
        
        // Assert
        assertThat(Files.exists(savedPath)).isTrue();
        assertThat(savedPath.getFileName().toString()).isEqualTo(validFileName);
        assertThat(savedPath.getParent().getFileName().toString()).isEqualTo(testEntityId.toString());
        
        String savedContent = Files.readString(savedPath);
        assertThat(savedContent).isEqualTo(content);
    }

    @Test
    void shouldReplaceExistingFileWhenSavingWithSameName() throws IOException {
        // Arrange
        String originalContent = "original content";
        String newContent = "new content";
        InputStream originalStream = new ByteArrayInputStream(originalContent.getBytes());
        InputStream newStream = new ByteArrayInputStream(newContent.getBytes());
        
        // Act
        Path firstSave = fileStorageService.save(originalStream, validFileName, testEntityId);
        Path secondSave = fileStorageService.save(newStream, validFileName, testEntityId);
        
        // Assert
        assertThat(firstSave).isEqualTo(secondSave);
        String savedContent = Files.readString(secondSave);
        assertThat(savedContent).isEqualTo(newContent);
    }

    @Test
    void shouldThrowExceptionWhenSavingFileWithInvalidExtension() {
        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.save(testInputStream, invalidFileName, testEntityId))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Непідтримуваний формат файлу: txt");
    }

    @Test
    void shouldThrowExceptionWhenSavingFileWithNullFileName() {
        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.save(testInputStream, null, testEntityId))
                .isInstanceOf(FileStorageException.class);
    }

    @Test
    void shouldThrowExceptionWhenSavingFileWithEmptyFileName() {
        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.save(testInputStream, "", testEntityId))
                .isInstanceOf(FileStorageException.class);
    }

    @Test
    void shouldThrowExceptionWhenSavingFileWithInvalidCharacters() {
        // Arrange
        String maliciousFileName = "../../../etc/passwd.jpg";

        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.save(testInputStream, maliciousFileName, testEntityId))
                .isInstanceOf(FileStorageException.class);
    }

    // GetFilePath method tests
    @Test
    void shouldReturnFilePathWhenFileExists() throws IOException {
        // Arrange
        fileStorageService.save(testInputStream, validFileName, testEntityId);
        
        // Act
        Path filePath = fileStorageService.getFilePath(validFileName, testEntityId);
        
        // Assert
        assertThat(Files.exists(filePath)).isTrue();
        assertThat(filePath.getFileName().toString()).isEqualTo(validFileName);
    }

    @Test
    void shouldThrowExceptionWhenGettingPathForNonExistentFile() {
        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.getFilePath(validFileName, testEntityId))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Файл не знайдено");
    }

    @Test
    void shouldThrowExceptionWhenGettingPathWithInvalidFileName() {
        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.getFilePath(invalidFileName, testEntityId))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Непідтримуваний формат файлу");
    }

    // Delete method tests
    @Test
    void shouldDeleteFileSuccessfullyWhenFileExists() throws IOException {
        // Arrange
        Path savedPath = fileStorageService.save(testInputStream, validFileName, testEntityId);
        assertThat(Files.exists(savedPath)).isTrue();
        
        // Act
        fileStorageService.delete(validFileName, testEntityId);
        
        // Assert
        assertThat(Files.exists(savedPath)).isFalse();
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentFile() {
        // Act & Assert - should not throw exception
        fileStorageService.delete(validFileName, testEntityId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingWithInvalidFileName() {
        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.delete(invalidFileName, testEntityId))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Непідтримуваний формат файлу");
    }

    // Exists method tests
    @Test
    void shouldReturnTrueWhenFileExists() throws IOException {
        // Arrange
        fileStorageService.save(testInputStream, validFileName, testEntityId);
        
        // Act
        boolean exists = fileStorageService.exists(validFileName, testEntityId);
        
        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFileDoesNotExist() {
        // Act
        boolean exists = fileStorageService.exists(validFileName, testEntityId);
        
        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenCheckingExistenceWithInvalidFileName() {
        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.exists(invalidFileName, testEntityId))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Непідтримуваний формат файлу");
    }

    // File name validation tests
    @Test
    void shouldThrowExceptionForFileNameWithBackslashes() {
        // Arrange
        String fileNameWithBackslash = "folder\\file.jpg";

        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.save(testInputStream, fileNameWithBackslash, testEntityId))
                .isInstanceOf(FileStorageException.class);
    }

    @Test
    void shouldThrowExceptionForFileNameWithForwardSlashes() {
        // Arrange
        String fileNameWithSlash = "folder/file.jpg";

        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.save(testInputStream, fileNameWithSlash, testEntityId))
                .isInstanceOf(FileStorageException.class);
    }

    @Test
    void shouldAcceptValidFileExtensions() throws IOException {
        // Arrange
        String[] validFiles = {"image.jpg", "photo.png", "animation.gif", "audio.mp3", "sound.wav"};
        
        // Act & Assert
        for (String fileName : validFiles) {
            InputStream stream = new ByteArrayInputStream("content".getBytes());
            UUID entityId = UUID.randomUUID();
            
            Path savedPath = fileStorageService.save(stream, fileName, entityId);
            assertThat(Files.exists(savedPath)).isTrue();
        }
    }

    @Test
    void shouldHandleCaseInsensitiveExtensions() throws IOException {
        // Arrange
        String upperCaseFileName = "image.JPG";
        InputStream stream = new ByteArrayInputStream("content".getBytes());

        // Act
        Path savedPath = fileStorageService.save(stream, upperCaseFileName, testEntityId);

        // Assert
        assertThat(Files.exists(savedPath)).isTrue();
    }

    // Entity directory creation tests
    @Test
    void shouldCreateEntityDirectoryWhenSavingFirstFile() throws IOException {
        // Arrange
        UUID newEntityId = UUID.randomUUID();
        Path expectedEntityDir = tempDir.resolve(newEntityId.toString());

        // Act
        fileStorageService.save(testInputStream, validFileName, newEntityId);

        // Assert
        assertThat(Files.exists(expectedEntityDir)).isTrue();
        assertThat(Files.isDirectory(expectedEntityDir)).isTrue();
    }

    @Test
    void shouldReuseExistingEntityDirectoryWhenSavingMultipleFiles() throws IOException {
        // Arrange
        String fileName1 = "file1.jpg";
        String fileName2 = "file2.png";
        InputStream stream1 = new ByteArrayInputStream("content1".getBytes());
        InputStream stream2 = new ByteArrayInputStream("content2".getBytes());

        // Act
        Path savedPath1 = fileStorageService.save(stream1, fileName1, testEntityId);
        Path savedPath2 = fileStorageService.save(stream2, fileName2, testEntityId);

        // Assert
        assertThat(savedPath1.getParent()).isEqualTo(savedPath2.getParent());
        assertThat(Files.exists(savedPath1)).isTrue();
        assertThat(Files.exists(savedPath2)).isTrue();
    }

    // Edge cases and error handling
    @Test
    void shouldThrowExceptionForFileWithoutExtension() {
        // Arrange
        String fileNameWithoutExtension = "filename";

        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.save(testInputStream, fileNameWithoutExtension, testEntityId))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Непідтримуваний формат файлу");
    }

    @Test
    void shouldThrowExceptionForFileWithOnlyDot() {
        // Arrange
        String fileNameWithOnlyDot = "filename.";

        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.save(testInputStream, fileNameWithOnlyDot, testEntityId))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Непідтримуваний формат файлу");
    }

    @Test
    void shouldAcceptFileStartingWithDotButWithValidExtension() throws IOException {
        // Arrange
        String hiddenFileName = ".hidden.jpg";
        InputStream stream = new ByteArrayInputStream("content".getBytes());

        // Act
        Path savedPath = fileStorageService.save(stream, hiddenFileName, testEntityId);

        // Assert
        assertThat(Files.exists(savedPath)).isTrue();
        assertThat(savedPath.getFileName().toString()).isEqualTo(hiddenFileName);
    }

    @Test
    void shouldHandleMultipleDotsInFileName() throws IOException {
        // Arrange
        String fileNameWithMultipleDots = "my.file.name.jpg";
        InputStream stream = new ByteArrayInputStream("content".getBytes());

        // Act
        Path savedPath = fileStorageService.save(stream, fileNameWithMultipleDots, testEntityId);

        // Assert
        assertThat(Files.exists(savedPath)).isTrue();
        assertThat(savedPath.getFileName().toString()).isEqualTo(fileNameWithMultipleDots);
    }

    // Integration tests
    @Test
    void shouldPerformCompleteFileLifecycleOperations() throws IOException {
        // Arrange
        String content = "test file lifecycle content";
        InputStream stream = new ByteArrayInputStream(content.getBytes());

        // Act & Assert - Save
        Path savedPath = fileStorageService.save(stream, validFileName, testEntityId);
        assertThat(Files.exists(savedPath)).isTrue();

        // Act & Assert - Exists
        boolean existsAfterSave = fileStorageService.exists(validFileName, testEntityId);
        assertThat(existsAfterSave).isTrue();

        // Act & Assert - Get path
        Path retrievedPath = fileStorageService.getFilePath(validFileName, testEntityId);
        assertThat(retrievedPath).isEqualTo(savedPath);

        // Act & Assert - Delete
        fileStorageService.delete(validFileName, testEntityId);
        boolean existsAfterDelete = fileStorageService.exists(validFileName, testEntityId);
        assertThat(existsAfterDelete).isFalse();
    }

    @Test
    void shouldHandleMultipleEntitiesWithSameFileName() throws IOException {
        // Arrange
        UUID entityId1 = UUID.randomUUID();
        UUID entityId2 = UUID.randomUUID();
        String content1 = "content for entity 1";
        String content2 = "content for entity 2";
        InputStream stream1 = new ByteArrayInputStream(content1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(content2.getBytes());

        // Act
        Path savedPath1 = fileStorageService.save(stream1, validFileName, entityId1);
        Path savedPath2 = fileStorageService.save(stream2, validFileName, entityId2);

        // Assert
        assertThat(Files.exists(savedPath1)).isTrue();
        assertThat(Files.exists(savedPath2)).isTrue();
        assertThat(savedPath1).isNotEqualTo(savedPath2);

        String savedContent1 = Files.readString(savedPath1);
        String savedContent2 = Files.readString(savedPath2);
        assertThat(savedContent1).isEqualTo(content1);
        assertThat(savedContent2).isEqualTo(content2);
    }
}
