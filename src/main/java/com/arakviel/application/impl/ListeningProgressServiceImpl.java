package com.arakviel.application.impl;

import com.arakviel.application.contract.ListeningProgressService;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.exception.ValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація сервісу для управління прогресом прослуховування аудіокниг.
 */
@Service
public class ListeningProgressServiceImpl implements ListeningProgressService {

    private final ListeningProgressRepository listeningProgressRepository;
    private final AudiobookRepository audiobookRepository;
    private final UserRepository userRepository;
    private final PersistenceContext persistenceContext;

    public ListeningProgressServiceImpl(ListeningProgressRepository listeningProgressRepository,
                                      AudiobookRepository audiobookRepository,
                                      UserRepository userRepository,
                                      PersistenceContext persistenceContext) {
        this.listeningProgressRepository = listeningProgressRepository;
        this.audiobookRepository = audiobookRepository;
        this.userRepository = userRepository;
        this.persistenceContext = persistenceContext;
    }

    /**
     * Створює новий запис прогресу прослуховування.
     */
    @Override
    public ListeningProgress create(ListeningProgress progress) {
        validateListeningProgress(progress);
        if (progress.getId() == null) {
            progress.setId(UUID.randomUUID());
        }
        if (progress.getLastListened() == null) {
            progress.setLastListened(LocalDateTime.now());
        }

        ValidationHelper validator = new ValidationHelper();

        // Перевірка існування користувача
        if (!userRepository.findById(progress.getUserId()).isPresent()) {
            validator.addError("userId", "користувач з таким ідентифікатором не існує");
        }

        // Перевірка існування аудіокниги
        if (!audiobookRepository.findById(progress.getAudiobookId()).isPresent()) {
            validator.addError("audiobookId", "аудіокнига з таким ідентифікатором не існує");
        }

        // Перевірка на дублювання запису прогресу
        Optional<ListeningProgress> existingProgress = findByUserIdAndAudiobookId(
                progress.getUserId(), progress.getAudiobookId());
        if (existingProgress.isPresent()) {
            validator.addError("progress", "запис прогресу для цього користувача та аудіокниги вже існує");
        }

        validator.throwIfHasErrors();

        persistenceContext.registerNew(progress);
        persistenceContext.commit();
        return progress;
    }

    /**
     * Оновлює існуючий запис прогресу прослуховування.
     */
    @Override
    public ListeningProgress update(UUID id, ListeningProgress progress) {
        validateListeningProgress(progress);
        progress.setId(id);

        // Перевірка існування запису прогресу
        if (!listeningProgressRepository.findById(id).isPresent()) {
            new ValidationHelper()
                    .addError("id", "запис прогресу з таким ідентифікатором не існує")
                    .throwIfHasErrors();
        }

        progress.setLastListened(LocalDateTime.now());

        persistenceContext.registerUpdated(id, progress);
        persistenceContext.commit();
        return progress;
    }

    /**
     * Видаляє запис прогресу прослуховування.
     */
    @Override
    public void delete(UUID id) {
        Optional<ListeningProgress> progressOpt = listeningProgressRepository.findById(id);
        if (progressOpt.isPresent()) {
            ListeningProgress progress = progressOpt.get();
            persistenceContext.registerDeleted(progress);
            persistenceContext.commit();
        }
    }

    @Override
    public Optional<ListeningProgress> findById(UUID id) {
        return listeningProgressRepository.findById(id);
    }

    @Override
    public List<ListeningProgress> findAll(int offset, int limit) {
        validatePagination(offset, limit);
        return listeningProgressRepository.findAll(offset, limit);
    }

    @Override
    public List<ListeningProgress> findByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return listeningProgressRepository.findByUserId(userId);
    }

    @Override
    public List<ListeningProgress> findByAudiobookId(UUID audiobookId) {
        new ValidationHelper()
                .validUuid("audiobookId", audiobookId)
                .throwIfHasErrors();
        return listeningProgressRepository.findByAudiobookId(audiobookId);
    }

    @Override
    public Optional<ListeningProgress> findByUserIdAndAudiobookId(UUID userId, UUID audiobookId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .validUuid("audiobookId", audiobookId)
                .throwIfHasErrors();
        return listeningProgressRepository.findByUserIdAndAudiobookId(userId, audiobookId);
    }

    @Override
    public ListeningProgress updateProgress(UUID userId, UUID audiobookId, int position) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .validUuid("audiobookId", audiobookId)
                .nonNegative("position", position)
                .throwIfHasErrors();

        Optional<ListeningProgress> existingProgressOpt = findByUserIdAndAudiobookId(userId, audiobookId);
        
        if (existingProgressOpt.isPresent()) {
            // Оновлюємо існуючий запис
            ListeningProgress existingProgress = existingProgressOpt.get();
            existingProgress.setPosition(position);
            existingProgress.setLastListened(LocalDateTime.now());
            
            persistenceContext.registerUpdated(existingProgress.getId(), existingProgress);
            persistenceContext.commit();
            return existingProgress;
        } else {
            // Створюємо новий запис
            ListeningProgress newProgress = new ListeningProgress(
                    UUID.randomUUID(), userId, audiobookId, position, LocalDateTime.now());
            return create(newProgress);
        }
    }

    @Override
    public ListeningProgress markAsCompleted(UUID userId, UUID audiobookId) {
        ValidationHelper validator = new ValidationHelper()
                .validUuid("userId", userId)
                .validUuid("audiobookId", audiobookId);

        // Отримуємо тривалість аудіокниги
        Optional<Audiobook> audiobookOpt = audiobookRepository.findById(audiobookId);
        if (!audiobookOpt.isPresent()) {
            validator.addError("audiobookId", "аудіокнига з таким ідентифікатором не існує");
        }

        validator.throwIfHasErrors();

        Audiobook audiobook = audiobookOpt.get();
        return updateProgress(userId, audiobookId, audiobook.getDuration());
    }

    @Override
    public ListeningProgress resetProgress(UUID userId, UUID audiobookId) {
        return updateProgress(userId, audiobookId, 0);
    }

    @Override
    public List<ListeningProgress> findRecentlyListened(UUID userId, int limit) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .positive("limit", limit)
                .throwIfHasErrors();
        return listeningProgressRepository.findRecentlyListened(userId, limit);
    }

    @Override
    public List<ListeningProgress> findCompletedByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return listeningProgressRepository.findCompletedByUserId(userId);
    }

    @Override
    public List<ListeningProgress> findInProgressByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return listeningProgressRepository.findInProgressByUserId(userId);
    }

    @Override
    public long countByUserId(UUID userId) {
        if (userId == null) {
            throw new ValidationException("Ідентифікатор користувача не може бути null.");
        }
        return listeningProgressRepository.countByUserId(userId);
    }

    @Override
    public long countByAudiobookId(UUID audiobookId) {
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }
        return listeningProgressRepository.countByAudiobookId(audiobookId);
    }

    @Override
    public long countCompletedByUserId(UUID userId) {
        if (userId == null) {
            throw new ValidationException("Ідентифікатор користувача не може бути null.");
        }
        return listeningProgressRepository.countCompletedByUserId(userId);
    }

    @Override
    public long countInProgressByUserId(UUID userId) {
        if (userId == null) {
            throw new ValidationException("Ідентифікатор користувача не може бути null.");
        }
        return listeningProgressRepository.countInProgressByUserId(userId);
    }

    @Override
    public double calculateProgressPercentage(UUID userId, UUID audiobookId) {
        if (userId == null) {
            throw new ValidationException("Ідентифікатор користувача не може бути null.");
        }
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }

        Optional<ListeningProgress> progressOpt = findByUserIdAndAudiobookId(userId, audiobookId);
        if (!progressOpt.isPresent()) {
            return 0.0;
        }

        Optional<Audiobook> audiobookOpt = audiobookRepository.findById(audiobookId);
        if (!audiobookOpt.isPresent()) {
            throw new ValidationException("Аудіокнига з ідентифікатором " + audiobookId + " не існує.");
        }

        ListeningProgress progress = progressOpt.get();
        Audiobook audiobook = audiobookOpt.get();

        if (audiobook.getDuration() == 0) {
            return 0.0;
        }

        double percentage = (double) progress.getPosition() / audiobook.getDuration() * 100;
        return Math.min(100.0, Math.max(0.0, percentage));
    }

    @Override
    public List<ListeningProgress> findUpdatedSince(UUID userId, LocalDateTime since) {
        if (userId == null) {
            throw new ValidationException("Ідентифікатор користувача не може бути null.");
        }
        if (since == null) {
            throw new ValidationException("Дата не може бути null.");
        }
        return listeningProgressRepository.findUpdatedSince(userId, since);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        if (userId == null) {
            throw new ValidationException("Ідентифікатор користувача не може бути null.");
        }
        listeningProgressRepository.deleteAllByUserId(userId);
    }

    @Override
    public void deleteAllByAudiobookId(UUID audiobookId) {
        if (audiobookId == null) {
            throw new ValidationException("Ідентифікатор аудіокниги не може бути null.");
        }
        listeningProgressRepository.deleteAllByAudiobookId(audiobookId);
    }

    /**
     * Валідує дані запису прогресу з використанням нового підходу.
     */
    private void validateListeningProgress(ListeningProgress progress) {
        ValidationHelper validator = new ValidationHelper()
                .notNull("progress", progress);

        if (progress != null) {
            validator
                    .validUuid("userId", progress.getUserId())
                    .validUuid("audiobookId", progress.getAudiobookId())
                    .nonNegative("position", progress.getPosition());
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
