package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Genre;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.AudiobookFileRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
class AudiobookFileRepositoryTest {

    private final AudiobookFileRepository audiobookFileRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;
    private final PersistenceContext persistenceContext;

    @Autowired
    public AudiobookFileRepositoryTest(
            AudiobookFileRepository audiobookFileRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool,
            PersistenceContext persistenceContext) {
        this.audiobookFileRepository = audiobookFileRepository;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
        this.persistenceContext = persistenceContext;
    }

    @BeforeEach
    void setUp() {
        persistenceContext.clearAndClose(); // Clear context
        persistenceInitializer.init(false); // Initialize without DML
        persistenceInitializer.clearData(); // Clear all data for isolation
        persistenceContext.activate(); // Activate context for this test
    }

    @AfterEach
    void tearDown() {
        persistenceContext.clearAndClose(); // Clear context after each test
    }

    @AfterAll
    void closeResources() {
        connectionPool.shutdown();
    }

    @Test
    void shouldSaveAndRetrieveAudiobookFileByIdWhenPersisted() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        
        Author author = new Author(authorId, "File", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "File Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "File Book", 7200, 2023, "Description", null);
        AudiobookFile file = new AudiobookFile(fileId, audiobookId, "/audio/file_book.mp3", FileFormat.MP3, 150000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(file);

        // Act
        persistenceContext.commit();
        Optional<AudiobookFile> foundFile = audiobookFileRepository.findById(fileId);

        // Assert
        assertThat(foundFile).isPresent();
        assertThat(foundFile.get())
                .extracting(AudiobookFile::getFilePath, AudiobookFile::getFormat)
                .containsExactly("/audio/file_book.mp3", FileFormat.MP3);
    }

    @Test
    void shouldFindFilesByAudiobookIdWhenFilesExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "Multi File", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Multi File Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Multi File Book", 14400, 2023, "Description", null);
        AudiobookFile file1 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/chapter1.mp3", FileFormat.MP3, 120000000);
        AudiobookFile file2 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/chapter2.flac", FileFormat.FLAC, 250000000);
        AudiobookFile file3 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/chapter3.wav", FileFormat.WAV, 180000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(file1);
        persistenceContext.registerNew(file2);
        persistenceContext.registerNew(file3);
        persistenceContext.commit();

        // Act
        List<AudiobookFile> files = audiobookFileRepository.findByAudiobookId(audiobookId);

        // Assert
        assertThat(files).hasSize(3);
        assertThat(files)
                .extracting(AudiobookFile::getFilePath)
                .containsExactlyInAnyOrder("/audio/chapter1.mp3", "/audio/chapter2.flac", "/audio/chapter3.wav");
    }

    @Test
    void shouldReturnEmptyListWhenNoFilesForAudiobookId() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "No Files", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "No Files Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "No Files Book", 3600, 2023, "Description", null);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.commit();

        // Act
        List<AudiobookFile> files = audiobookFileRepository.findByAudiobookId(audiobookId);

        // Assert
        assertThat(files).isEmpty();
    }

    @Test
    void shouldFindFilesByFormatWhenFilesExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobook1Id = UUID.randomUUID();
        UUID audiobook2Id = UUID.randomUUID();
        
        Author author = new Author(authorId, "Format", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Format Genre", "Description");
        Audiobook audiobook1 = new Audiobook(audiobook1Id, authorId, genreId, "Format Book 1", 5400, 2023, "Description", null);
        Audiobook audiobook2 = new Audiobook(audiobook2Id, authorId, genreId, "Format Book 2", 7200, 2023, "Description", null);
        AudiobookFile mp3File1 = new AudiobookFile(UUID.randomUUID(), audiobook1Id, "/audio/book1.mp3", FileFormat.MP3, 100000000);
        AudiobookFile mp3File2 = new AudiobookFile(UUID.randomUUID(), audiobook2Id, "/audio/book2.mp3", FileFormat.MP3, 120000000);
        AudiobookFile flacFile = new AudiobookFile(UUID.randomUUID(), audiobook1Id, "/audio/book1.flac", FileFormat.FLAC, 200000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook1);
        persistenceContext.registerNew(audiobook2);
        persistenceContext.registerNew(mp3File1);
        persistenceContext.registerNew(mp3File2);
        persistenceContext.registerNew(flacFile);
        persistenceContext.commit();

        // Act
        List<AudiobookFile> mp3Files = audiobookFileRepository.findByFormat(FileFormat.MP3);

        // Assert
        assertThat(mp3Files).hasSize(2);
        assertThat(mp3Files)
                .extracting(AudiobookFile::getFormat)
                .containsOnly(FileFormat.MP3);
    }

    @Test
    void shouldReturnEmptyListWhenNoFilesForFormat() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "Different Format", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Different Format Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Different Format Book", 3600, 2023, "Description", null);
        AudiobookFile file = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/book.mp3", FileFormat.MP3, 100000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(file);
        persistenceContext.commit();

        // Act
        List<AudiobookFile> oggFiles = audiobookFileRepository.findByFormat(FileFormat.OGG);

        // Assert
        assertThat(oggFiles).isEmpty();
    }

    @Test
    void shouldCountFilesByAudiobookIdWhenFilesExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "Count Files", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Count Files Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Count Files Book", 21600, 2023, "Description", null);
        AudiobookFile file1 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/part1.mp3", FileFormat.MP3, 150000000);
        AudiobookFile file2 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/part2.mp3", FileFormat.MP3, 160000000);
        AudiobookFile file3 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/part3.mp3", FileFormat.MP3, 170000000);
        AudiobookFile file4 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/part4.mp3", FileFormat.MP3, 180000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(file1);
        persistenceContext.registerNew(file2);
        persistenceContext.registerNew(file3);
        persistenceContext.registerNew(file4);
        persistenceContext.commit();

        // Act
        long count = audiobookFileRepository.countByAudiobookId(audiobookId);

        // Assert
        assertThat(count).isEqualTo(4);
    }

    @Test
    void shouldReturnZeroWhenNoFilesForAudiobookId() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "No Count Files", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "No Count Files Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "No Count Files Book", 3600, 2023, "Description", null);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.commit();

        // Act
        long count = audiobookFileRepository.countByAudiobookId(audiobookId);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldFindFilesBySizeRangeWhenFilesExist() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "Size Range", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Size Range Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Size Range Book", 10800, 2023, "Description", null);
        AudiobookFile smallFile = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/small.mp3", FileFormat.MP3, 50000000);
        AudiobookFile mediumFile = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/medium.mp3", FileFormat.MP3, 150000000);
        AudiobookFile largeFile = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/large.mp3", FileFormat.MP3, 300000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(smallFile);
        persistenceContext.registerNew(mediumFile);
        persistenceContext.registerNew(largeFile);
        persistenceContext.commit();

        // Act
        List<AudiobookFile> filesInRange = audiobookFileRepository.findBySizeRange(100000000, 200000000);

        // Assert
        assertThat(filesInRange).hasSize(1);
        assertThat(filesInRange.getFirst().getFilePath()).isEqualTo("/audio/medium.mp3");
    }

    @Test
    void shouldReturnEmptyListWhenNoFilesInSizeRange() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "No Size Range", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "No Size Range Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "No Size Range Book", 3600, 2023, "Description", null);
        AudiobookFile file = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/file.mp3", FileFormat.MP3, 50000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(file);
        persistenceContext.commit();

        // Act
        List<AudiobookFile> filesInRange = audiobookFileRepository.findBySizeRange(100000000, 200000000);

        // Assert
        assertThat(filesInRange).isEmpty();
    }

    @Test
    void shouldUpdateFilePathWhenModifiedAndPersisted() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        
        Author author = new Author(authorId, "Update File", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Update File Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Update File Book", 5400, 2023, "Description", null);
        AudiobookFile file = new AudiobookFile(fileId, audiobookId, "/audio/old_path.mp3", FileFormat.MP3, 120000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(file);
        persistenceContext.commit();

        // Act
        file.setFilePath("/audio/new_path.mp3");
        persistenceContext.registerUpdated(file.getId(), file);
        persistenceContext.commit();

        AudiobookFile updatedFile = audiobookFileRepository.findById(fileId).orElse(null);

        // Assert
        assertThat(updatedFile).isNotNull();
        assertThat(updatedFile.getFilePath()).isEqualTo("/audio/new_path.mp3");
    }

    @Test
    void shouldDeleteFileAndVerifyAbsence() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        
        Author author = new Author(authorId, "Delete File", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Delete File Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Delete File Book", 3600, 2023, "Description", null);
        AudiobookFile file = new AudiobookFile(fileId, audiobookId, "/audio/delete_me.mp3", FileFormat.MP3, 100000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(file);
        persistenceContext.commit();

        // Act
        persistenceContext.registerDeleted(file);
        persistenceContext.commit();

        Optional<AudiobookFile> deletedFile = audiobookFileRepository.findById(fileId);

        // Assert
        assertThat(deletedFile).isEmpty();
    }

    @Test
    void shouldSaveMultipleFilesAndRetrieveAll() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID audiobookId = UUID.randomUUID();
        
        Author author = new Author(authorId, "Multiple Files", "Author", "Biography", null);
        Genre genre = new Genre(genreId, "Multiple Files Genre", "Description");
        Audiobook audiobook = new Audiobook(audiobookId, authorId, genreId, "Multiple Files Book", 18000, 2023, "Description", null);
        AudiobookFile file1 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/file1.mp3", FileFormat.MP3, 140000000);
        AudiobookFile file2 = new AudiobookFile(UUID.randomUUID(), audiobookId, "/audio/file2.flac", FileFormat.FLAC, 280000000);
        
        persistenceContext.registerNew(author);
        persistenceContext.registerNew(genre);
        persistenceContext.registerNew(audiobook);
        persistenceContext.registerNew(file1);
        persistenceContext.registerNew(file2);
        persistenceContext.commit();

        // Act
        List<AudiobookFile> allFiles = audiobookFileRepository.findAll();

        // Assert
        assertThat(allFiles).hasSize(2);
        assertThat(allFiles)
                .extracting(AudiobookFile::getFormat)
                .containsExactlyInAnyOrder(FileFormat.MP3, FileFormat.FLAC);
    }
}
