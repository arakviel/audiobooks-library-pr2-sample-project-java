package com.arakviel.application.impl;

import com.arakviel.application.contract.AudiobookService;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.file.exception.FileStorageException;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookFileRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація сервісу для управління сутностями аудіокниг, включаючи операції з файлами.
 */
@Service
public class AudiobookServiceImpl implements AudiobookService {

    private final AudiobookRepository audiobookRepository;
    private final AudiobookFileRepository audiobookFileRepository;
    private final PersistenceContext persistenceContext;
    private final FileStorageService fileStorageService;

    /**
     * Конструктор для ін'єкції залежностей.
     *
     * @param audiobookRepository     репозиторій аудіокниг
     * @param audiobookFileRepository репозиторій файлів аудіокниг
     * @param persistenceContext      контекст для управління транзакціями
     * @param fileStorageService      сервіс для роботи з файлами
     */
    public AudiobookServiceImpl(
            AudiobookRepository audiobookRepository,
            AudiobookFileRepository audiobookFileRepository,
            PersistenceContext persistenceContext,
            FileStorageService fileStorageService) {
        this.audiobookRepository = audiobookRepository;
        this.audiobookFileRepository = audiobookFileRepository;
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
}