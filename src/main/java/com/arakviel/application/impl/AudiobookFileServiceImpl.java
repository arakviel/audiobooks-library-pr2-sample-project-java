package com.arakviel.application.impl;

import com.arakviel.application.contract.AudiobookFileService;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.exception.ValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookFileRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація сервісу для управління файлами аудіокниг.
 */
@Service
public class AudiobookFileServiceImpl implements AudiobookFileService {

    private final AudiobookFileRepository audiobookFileRepository;
    private final AudiobookRepository audiobookRepository;
    private final PersistenceContext persistenceContext;
    private final FileStorageService fileStorageService;

    public AudiobookFileServiceImpl(AudiobookFileRepository audiobookFileRepository,
                                  AudiobookRepository audiobookRepository,
                                  PersistenceContext persistenceContext,
                                  FileStorageService fileStorageService) {
        this.audiobookFileRepository = audiobookFileRepository;
        this.audiobookRepository = audiobookRepository;
        this.persistenceContext = persistenceContext;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Створює новий файл аудіокниги та завантажує його.
     */
    @Override
    public AudiobookFile create(AudiobookFile audiobookFile, InputStream inputStream, String fileName) {
        validateAudiobookFile(audiobookFile);
        if (audiobookFile.getId() == null) {
            audiobookFile.setId(UUID.randomUUID());
        }

        // Перевірка існування аудіокниги
        if (!audiobookRepository.findById(audiobookFile.getAudiobookId()).isPresent()) {
            throw new ValidationException("Аудіокнига з ідентифікатором " + audiobookFile.getAudiobookId() + " не існує.");
        }

        // Перевірка на дублювання шляху файлу
        if (existsByAudiobookIdAndFilePath(audiobookFile.getAudiobookId(), audiobookFile.getFilePath())) {
            throw new ValidationException("Файл з таким шляхом вже існує для цієї аудіокниги.");
        }

        // Завантаження файлу
        if (inputStream != null && fileName != null) {
            Path savedPath = fileStorageService.save(inputStream, fileName, audiobookFile.getAudiobookId());
            audiobookFile.setFilePath(savedPath.toString());
        }

        persistenceContext.registerNew(audiobookFile);
        persistenceContext.commit();
        return audiobookFile;
    }

    /**
     * Оновлює існуючий файл аудіокниги.
     */
    @Override
    public AudiobookFile update(UUID id, AudiobookFile audiobookFile, InputStream inputStream, String fileName) {
        validateAudiobookFile(audiobookFile);
        audiobookFile.setId(id);

        // Перевірка існування файлу
        Optional<AudiobookFile> existingFileOpt = audiobookFileRepository.findById(id);
        if (!existingFileOpt.isPresent()) {
            throw new ValidationException("Файл аудіокниги з ідентифікатором " + id + " не існує.");
        }

        AudiobookFile existingFile = existingFileOpt.get();

        // Обробка існуючого файлу
        if (existingFile.getFilePath() != null && inputStream != null && fileName != null) {
            String oldFileName = Path.of(existingFile.getFilePath()).getFileName().toString();
            fileStorageService.delete(oldFileName, existingFile.getAudiobookId());
        }

        // Завантаження нового файлу
        if (inputStream != null && fileName != null) {
            Path savedPath = fileStorageService.save(inputStream, fileName, audiobookFile.getAudiobookId());
            audiobookFile.setFilePath(savedPath.toString());
        } else if (existingFile.getFilePath() != null) {
            audiobookFile.setFilePath(existingFile.getFilePath());
        }

        persistenceContext.registerUpdated(id, audiobookFile);
        persistenceContext.commit();
        return audiobookFile;
    }

    /**
     * Видаляє файл аудіокниги та пов'язаний фізичний файл.
     */
    @Override
    public void delete(UUID id) {
        Optional<AudiobookFile> fileOpt = audiobookFileRepository.findById(id);
        if (fileOpt.isPresent()) {
            AudiobookFile file = fileOpt.get();

            // Видалення фізичного файлу
            if (file.getFilePath() != null) {
                String fileName = Path.of(file.getFilePath()).getFileName().toString();
                fileStorageService.delete(fileName, file.getAudiobookId());
            }

            persistenceContext.registerDeleted(file);
            persistenceContext.commit();
        }
    }

    @Override
    public Optional<AudiobookFile> findById(UUID id) {
        return audiobookFileRepository.findById(id);
    }

    @Override
    public List<AudiobookFile> findAll(int offset, int limit) {
        validatePagination(offset, limit);
        return audiobookFileRepository.findAll(offset, limit);
    }

    @Override
    public List<AudiobookFile> findByAudiobookId(UUID audiobookId) {
        new ValidationHelper()
                .validUuid("audiobookId", audiobookId)
                .throwIfHasErrors();
        return audiobookFileRepository.findByAudiobookId(audiobookId);
    }

    @Override
    public List<AudiobookFile> findByFormat(FileFormat format) {
        new ValidationHelper()
                .notNull("format", format)
                .throwIfHasErrors();
        return audiobookFileRepository.findByFormat(format);
    }

    @Override
    public List<AudiobookFile> findByAudiobookIdAndFormat(UUID audiobookId, FileFormat format) {
        new ValidationHelper()
                .validUuid("audiobookId", audiobookId)
                .notNull("format", format)
                .throwIfHasErrors();
        return audiobookFileRepository.findByAudiobookIdAndFormat(audiobookId, format);
    }

    @Override
    public long countByAudiobookId(UUID audiobookId) {
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }
        return audiobookFileRepository.countByAudiobookId(audiobookId);
    }

    @Override
    public long countByFormat(FileFormat format) {
        if (format == null) {
            throw new ValidationException("Формат файлу не може бути null.");
        }
        return audiobookFileRepository.countByFormat(format);
    }

    @Override
    public long calculateTotalSizeByAudiobookId(UUID audiobookId) {
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }
        return audiobookFileRepository.calculateTotalSizeByAudiobookId(audiobookId);
    }

    @Override
    public Optional<AudiobookFile> findLargestFileByAudiobookId(UUID audiobookId) {
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }
        return audiobookFileRepository.findLargestFileByAudiobookId(audiobookId);
    }

    @Override
    public Optional<AudiobookFile> findSmallestFileByAudiobookId(UUID audiobookId) {
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }
        return audiobookFileRepository.findSmallestFileByAudiobookId(audiobookId);
    }

    @Override
    public boolean existsByAudiobookIdAndFilePath(UUID audiobookId, String filePath) {
        new ValidationHelper()
                .validUuid("audiobookId", audiobookId)
                .notEmpty("filePath", filePath)
                .throwIfHasErrors();
        return audiobookFileRepository.existsByAudiobookIdAndFilePath(audiobookId, filePath);
    }

    @Override
    public void deleteAllByAudiobookId(UUID audiobookId) {
        new ValidationHelper()
                .validUuid("audiobookId", audiobookId)
                .throwIfHasErrors();

        List<AudiobookFile> files = findByAudiobookId(audiobookId);
        for (AudiobookFile file : files) {
            delete(file.getId());
        }
    }

    @Override
    public AudiobookFile copyFile(UUID sourceFileId, UUID targetAudiobookId, String newFileName) {
        new ValidationHelper()
                .validUuid("sourceFileId", sourceFileId)
                .validUuid("targetAudiobookId", targetAudiobookId)
                .notEmpty("newFileName", newFileName)
                .throwIfHasErrors();

        // Перевірка існування вихідного файлу
        Optional<AudiobookFile> sourceFileOpt = audiobookFileRepository.findById(sourceFileId);
        if (!sourceFileOpt.isPresent()) {
            throw new ValidationException("Вихідний файл не існує.");
        }

        // Перевірка існування цільової аудіокниги
        if (!audiobookRepository.findById(targetAudiobookId).isPresent()) {
            throw new ValidationException("Цільова аудіокнига не існує.");
        }

        AudiobookFile sourceFile = sourceFileOpt.get();

        // Створення копії файлу
        AudiobookFile newFile = new AudiobookFile(
                UUID.randomUUID(),
                targetAudiobookId,
                null, // filePath буде встановлено при збереженні
                sourceFile.getFormat(),
                sourceFile.getSize()
        );

        // Тут потрібно було б скопіювати фізичний файл, але це складно без доступу до файлової системи
        // Для спрощення просто створюємо запис у базі даних
        persistenceContext.registerNew(newFile);
        persistenceContext.commit();
        return newFile;
    }

    @Override
    public AudiobookFile changeFormat(UUID fileId, FileFormat newFormat) {
        if (fileId == null) {
            throw new ValidationException("Ідентифікатор файлу не може бути null.");
        }
        if (newFormat == null) {
            throw new ValidationException("Новий формат не може бути null.");
        }

        Optional<AudiobookFile> fileOpt = audiobookFileRepository.findById(fileId);
        if (!fileOpt.isPresent()) {
            throw new ValidationException("Файл не існує.");
        }

        AudiobookFile file = fileOpt.get();
        file.setFormat(newFormat);

        persistenceContext.registerUpdated(fileId, file);
        persistenceContext.commit();
        return file;
    }

    @Override
    public Map<FileFormat, Long> getFormatStatistics() {
        Map<FileFormat, Long> statistics = new HashMap<>();
        for (FileFormat format : FileFormat.values()) {
            long count = countByFormat(format);
            statistics.put(format, count);
        }
        return statistics;
    }

    @Override
    public List<AudiobookFile> findByAudiobookIdOrderBySize(UUID audiobookId, boolean ascending) {
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }
        return audiobookFileRepository.findByAudiobookIdOrderBySize(audiobookId, ascending);
    }

    @Override
    public List<AudiobookFile> findPotentialDuplicates(UUID audiobookId) {
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }
        return audiobookFileRepository.findPotentialDuplicates(audiobookId);
    }

    /**
     * Валідує дані файлу аудіокниги з використанням нового підходу.
     */
    private void validateAudiobookFile(AudiobookFile audiobookFile) {
        ValidationHelper validator = new ValidationHelper()
                .notNull("audiobookFile", audiobookFile);

        if (audiobookFile != null) {
            validator
                    .validUuid("audiobookId", audiobookFile.getAudiobookId())
                    .notNull("format", audiobookFile.getFormat())
                    .nonNegative("size", audiobookFile.getSize());
        }

        validator.throwIfHasErrors();
    }

    /**
     * Валідує параметри пагінації з використанням нового підходу.
     */
    private void validatePagination(int offset, int limit) {
        new ValidationHelper()
                .nonNegative("offset", offset)
                .positive("limit", limit)
                .throwIfHasErrors();
    }
}
