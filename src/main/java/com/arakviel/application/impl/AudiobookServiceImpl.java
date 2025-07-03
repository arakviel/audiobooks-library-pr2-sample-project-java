package com.arakviel.application.impl;

import com.arakviel.application.contract.AudiobookService;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.exception.ValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Genre;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.file.exception.FileStorageException;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookFileRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.contract.AuthorRepository;
import com.arakviel.infrastructure.persistence.contract.GenreRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація сервісу для управління сутностями аудіокниг, включаючи операції з файлами.
 */
@Service
public class AudiobookServiceImpl implements AudiobookService {

    private final AudiobookRepository audiobookRepository;
    private final AudiobookFileRepository audiobookFileRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PersistenceContext persistenceContext;
    private final FileStorageService fileStorageService;

    /**
     * Конструктор для ін'єкції залежностей.
     *
     * @param audiobookRepository     репозиторій аудіокниг
     * @param audiobookFileRepository репозиторій файлів аудіокниг
     * @param authorRepository        репозиторій авторів
     * @param genreRepository         репозиторій жанрів
     * @param persistenceContext      контекст для управління транзакціями
     * @param fileStorageService      сервіс для роботи з файлами
     */
    public AudiobookServiceImpl(
            AudiobookRepository audiobookRepository,
            AudiobookFileRepository audiobookFileRepository,
            AuthorRepository authorRepository,
            GenreRepository genreRepository,
            PersistenceContext persistenceContext,
            FileStorageService fileStorageService) {
        this.audiobookRepository = audiobookRepository;
        this.audiobookFileRepository = audiobookFileRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.persistenceContext = persistenceContext;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Створює нову аудіокнигу та, за потреби, завантажує обкладинку.
     *
     * @param audiobook      аудіокнига для створення
     * @param coverImage     потік даних обкладинки, може бути null
     * @param coverImageName ім'я файлу обкладинки, може бути null
     * @return створена аудіокнига
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    @Override
    public Audiobook create(Audiobook audiobook, InputStream coverImage, String coverImageName) {
        if (audiobook.getId() == null) {
            audiobook.setId(UUID.randomUUID());
        }

        // Обробка завантаження обкладинки
        if (coverImage != null && coverImageName != null) {
            Path coverImagePath = fileStorageService.save(coverImage, coverImageName, audiobook.getId());
            audiobook.setCoverImagePath(coverImagePath.toString());
        }

        persistenceContext.registerNew(audiobook);
        persistenceContext.commit();
        return audiobook;
    }

    /**
     * Оновлює існуючу аудіокнигу та, за потреби, оновлює обкладинку.
     *
     * @param id             ідентифікатор аудіокниги для оновлення
     * @param audiobook      оновлені дані аудіокниги
     * @param coverImage     потік даних нової обкладинки, може бути null
     * @param coverImageName ім'я файлу нової обкладинки, може бути null
     * @return оновлена аудіокнига
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    @Override
    public Audiobook update(UUID id, Audiobook audiobook, InputStream coverImage, String coverImageName) {
        audiobook.setId(id);

        // Обробка існуючої обкладинки
        if (audiobook.getCoverImagePath() != null && coverImage != null && coverImageName != null) {
            fileStorageService.delete(audiobook.getCoverImagePath(), id);
        }

        // Обробка нової обкладинки
        if (coverImage != null && coverImageName != null) {
            Path coverImagePath = fileStorageService.save(coverImage, coverImageName, id);
            audiobook.setCoverImagePath(coverImagePath.toString());
        }

        persistenceContext.registerUpdated(id, audiobook);
        persistenceContext.commit();
        return audiobook;
    }

    /**
     * Видаляє аудіокнигу та всі пов'язані файли.
     *
     * @param id ідентифікатор аудіокниги для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    @Override
    public void delete(UUID id) {
        Optional<Audiobook> audiobookOpt = audiobookRepository.findById(id);
        if (audiobookOpt.isPresent()) {
            Audiobook audiobook = audiobookOpt.get();

            // Видалення обкладинки
            if (audiobook.getCoverImagePath() != null) {
                fileStorageService.delete(audiobook.getCoverImagePath(), id);
            }

            // Видалення пов'язаних аудіофайлів
            List<AudiobookFile> files = audiobookFileRepository.findByAudiobookId(id);
            for (AudiobookFile file : files) {
                fileStorageService.delete(file.getFilePath(), id);
                persistenceContext.registerDeleted(file);
            }

            persistenceContext.registerDeleted(audiobook);
            persistenceContext.commit();
        }
    }

    /**
     * Завантажує аудіофайл для певної аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param inputStream потік даних аудіофайлу
     * @param fileName    ім'я аудіофайлу
     * @param format      формат файлу
     * @param size        розмір файлу в байтах
     * @return створений аудіофайл
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    @Override
    public AudiobookFile uploadAudiobookFile(UUID audiobookId, InputStream inputStream, String fileName, FileFormat format, int size) {
        AudiobookFile audiobookFile = new AudiobookFile(
                UUID.randomUUID(), audiobookId, null, format, size);

        Path filePath = fileStorageService.save(inputStream, fileName, audiobookId);
        audiobookFile.setFilePath(filePath.toString());

        persistenceContext.registerNew(audiobookFile);
        persistenceContext.commit();
        return audiobookFile;
    }

    /**
     * Знаходить аудіокнигу за ідентифікатором.
     *
     * @param id ідентифікатор аудіокниги
     * @return Optional з аудіокнигою, якщо знайдено
     */
    @Override
    public Optional<Audiobook> findById(UUID id) {
        return audiobookRepository.findById(id);
    }

    /**
     * Знаходить всі аудіокниги з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findAll(int offset, int limit) {
        return audiobookRepository.findAll(offset, limit);
    }

    /**
     * Знаходить всі файли, пов'язані з аудіокнигою.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список аудіофайлів
     */
    @Override
    public List<AudiobookFile> findFilesByAudiobookId(UUID audiobookId) {
        return audiobookFileRepository.findByAudiobookId(audiobookId);
    }

    /**
     * Видаляє певний аудіофайл аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param fileId      ідентифікатор файлу для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    @Override
    public void deleteAudiobookFile(UUID audiobookId, UUID fileId) {
        Optional<AudiobookFile> fileOpt = audiobookFileRepository.findById(fileId);
        if (fileOpt.isPresent()) {
            AudiobookFile file = fileOpt.get();
            if (file.getAudiobookId().equals(audiobookId)) {
                fileStorageService.delete(file.getFilePath(), audiobookId);
                persistenceContext.registerDeleted(file);
                persistenceContext.commit();
            }
        }
    }

    @Override
    public List<Audiobook> findByTitle(String title) {
        new ValidationHelper()
                .notEmpty("title", title)
                .throwIfHasErrors();
        return audiobookRepository.findByTitle(title);
    }

    @Override
    public List<Audiobook> findByPartialTitle(String partialTitle) {
        new ValidationHelper()
                .notEmpty("partialTitle", partialTitle)
                .throwIfHasErrors();
        return audiobookRepository.findByPartialTitle(partialTitle);
    }

    @Override
    public List<Audiobook> findByAuthorId(UUID authorId) {
        new ValidationHelper()
                .validUuid("authorId", authorId)
                .throwIfHasErrors();
        return audiobookRepository.findByAuthorId(authorId);
    }

    @Override
    public List<Audiobook> findByGenreId(UUID genreId) {
        new ValidationHelper()
                .validUuid("genreId", genreId)
                .throwIfHasErrors();
        return audiobookRepository.findByGenreId(genreId);
    }

    @Override
    public List<Audiobook> findByPublicationYear(int year) {
        new ValidationHelper()
                .nonNegative("year", year)
                .throwIfHasErrors();
        return audiobookRepository.findByPublicationYear(year);
    }

    @Override
    public List<Audiobook> findByDurationRange(int minDuration, int maxDuration) {
        new ValidationHelper()
                .nonNegative("minDuration", minDuration)
                .nonNegative("maxDuration", maxDuration)
                .addErrorIf(maxDuration < minDuration, "maxDuration",
                    "не може бути меншою за мінімальну тривалість")
                .throwIfHasErrors();
        return audiobookRepository.findByDurationRange(minDuration, maxDuration);
    }

    @Override
    public List<Audiobook> findMostPopular(int limit) {
        new ValidationHelper()
                .positive("limit", limit)
                .throwIfHasErrors();
        return audiobookRepository.findMostPopular(limit);
    }

    @Override
    public List<Audiobook> findRecentlyAdded(int limit) {
        new ValidationHelper()
                .positive("limit", limit)
                .throwIfHasErrors();
        return audiobookRepository.findRecentlyAdded(limit);
    }

    @Override
    public long count() {
        return audiobookRepository.count();
    }

    @Override
    public long countByAuthorId(UUID authorId) {
        new ValidationHelper()
                .validUuid("authorId", authorId)
                .throwIfHasErrors();
        return audiobookRepository.countByAuthorId(authorId);
    }

    @Override
    public long countByGenreId(UUID genreId) {
        new ValidationHelper()
                .validUuid("genreId", genreId)
                .throwIfHasErrors();
        return audiobookRepository.countByGenreId(genreId);
    }

    @Override
    public long calculateTotalDuration() {
        return audiobookRepository.calculateTotalDuration();
    }

    @Override
    public double calculateAverageDuration() {
        return audiobookRepository.calculateAverageDuration();
    }

    @Override
    public Optional<Audiobook> findLongest() {
        return audiobookRepository.findLongest();
    }

    @Override
    public Optional<Audiobook> findShortest() {
        return audiobookRepository.findShortest();
    }

    @Override
    public boolean existsByTitleAndAuthorId(String title, UUID authorId) {
        new ValidationHelper()
                .notEmpty("title", title)
                .validUuid("authorId", authorId)
                .throwIfHasErrors();
        return audiobookRepository.existsByTitleAndAuthorId(title, authorId);
    }

    @Override
    public List<Audiobook> findSimilar(UUID audiobookId, int limit) {
        new ValidationHelper()
                .validUuid("audiobookId", audiobookId)
                .positive("limit", limit)
                .throwIfHasErrors();
        return audiobookRepository.findSimilar(audiobookId, limit);
    }

    @Override
    public Map<Genre, Long> getGenreStatistics() {
        Map<Genre, Long> statistics = new HashMap<>();
        List<Genre> genres = genreRepository.findAll(0, Integer.MAX_VALUE);
        for (Genre genre : genres) {
            long count = countByGenreId(genre.getId());
            statistics.put(genre, count);
        }
        return statistics;
    }

    @Override
    public Map<Author, Long> getAuthorStatistics() {
        Map<Author, Long> statistics = new HashMap<>();
        List<Author> authors = authorRepository.findAll(0, Integer.MAX_VALUE);
        for (Author author : authors) {
            long count = countByAuthorId(author.getId());
            statistics.put(author, count);
        }
        return statistics;
    }
}