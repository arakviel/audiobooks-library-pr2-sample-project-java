package com.arakviel.application.impl;

import com.arakviel.application.contract.GenreService;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.exception.ValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.GenreRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація сервісу для управління сутностями жанрів, включаючи операції з пов'язаними аудіокнигами.
 */
@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final PersistenceContext persistenceContext;

    /**
     * Конструктор для ін'єкції залежностей.
     *
     * @param genreRepository    репозиторій жанрів
     * @param persistenceContext контекст для управління транзакціями
     */
    public GenreServiceImpl(
            GenreRepository genreRepository,
            PersistenceContext persistenceContext) {
        this.genreRepository = genreRepository;
        this.persistenceContext = persistenceContext;
    }

    /**
     * Створює новий жанр.
     *
     * @param genre жанр для створення
     * @return створений жанр
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила (наприклад, дублювання жанру)
     */
    @Override
    public Genre create(Genre genre) {
        validateGenre(genre);
        if (genre.getId() == null) {
            genre.setId(UUID.randomUUID());
        }

        // Перевірка на дублювання жанру
        if (genreRepository.existsByName(genre.getName())) {
            new ValidationHelper()
                    .addError("name", "жанр з такою назвою вже існує")
                    .throwIfHasErrors();
        }

        persistenceContext.registerNew(genre);
        persistenceContext.commit();
        return genre;
    }

    /**
     * Оновлює існуючий жанр.
     *
     * @param id    ідентифікатор жанру для оновлення
     * @param genre оновлені дані жанру
     * @return оновлений жанр
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    @Override
    public Genre update(UUID id, Genre genre) {
        validateGenre(genre);
        genre.setId(id);

        ValidationHelper validator = new ValidationHelper();

        // Перевірка існування жанру
        if (!genreRepository.findById(id).isPresent()) {
            validator.addError("id", "жанр з таким ідентифікатором не існує");
        }

        // Перевірка на дублювання назви при оновленні
        List<Genre> existingGenres = genreRepository.findByName(genre.getName());
        if (!existingGenres.isEmpty() && !existingGenres.get(0).getId().equals(id)) {
            validator.addError("name", "жанр з такою назвою вже існує");
        }

        validator.throwIfHasErrors();

        persistenceContext.registerUpdated(id, genre);
        persistenceContext.commit();
        return genre;
    }

    /**
     * Видаляє жанр, якщо він не пов'язаний з аудіокнигами.
     *
     * @param id ідентифікатор жанру для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо жанр пов'язаний з аудіокнигами
     */
    @Override
    public void delete(UUID id) {
        Optional<Genre> genreOpt = genreRepository.findById(id);
        if (genreOpt.isPresent()) {
            // Перевірка, чи жанр пов'язаний з аудіокнигами
            if (countAudiobooksByGenreId(id) > 0) {
                new ValidationHelper()
                        .addError("id", "неможливо видалити жанр, оскільки він пов'язаний з аудіокнигами")
                        .throwIfHasErrors();
            }

            persistenceContext.registerDeleted(genreOpt.get());
            persistenceContext.commit();
        }
    }

    /**
     * Знаходить жанр за ідентифікатором.
     *
     * @param id ідентифікатор жанру
     * @return Optional з жанром, якщо знайдено
     */
    @Override
    public Optional<Genre> findById(UUID id) {
        return genreRepository.findById(id);
    }

    /**
     * Знаходить всі жанри з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список жанрів
     */
    @Override
    public List<Genre> findAll(int offset, int limit) {
        return genreRepository.findAll(offset, limit);
    }

    /**
     * Знаходить жанри за назвою.
     *
     * @param name назва жанру
     * @return список жанрів
     */
    @Override
    public List<Genre> findByName(String name) {
        new ValidationHelper()
                .notEmpty("name", name)
                .throwIfHasErrors();
        return genreRepository.findByName(name);
    }

    /**
     * Знаходить жанри за частковою відповідністю назви.
     *
     * @param partialName часткова назва жанру
     * @return список жанрів
     */
    @Override
    public List<Genre> findByPartialName(String partialName) {
        new ValidationHelper()
                .notEmpty("partialName", partialName)
                .throwIfHasErrors();
        return genreRepository.findByPartialName(partialName);
    }

    /**
     * Знаходить аудіокниги, пов'язані з жанром.
     *
     * @param genreId ідентифікатор жанру
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findAudiobooksByGenreId(UUID genreId) {
        new ValidationHelper()
                .validUuid("genreId", genreId)
                .throwIfHasErrors();
        return genreRepository.findAudiobooksByGenreId(genreId);
    }

    /**
     * Знаходить жанри, пов'язані з аудіокнигою.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список жанрів
     */
    @Override
    public List<Genre> findByAudiobookId(UUID audiobookId) {
        new ValidationHelper()
                .validUuid("audiobookId", audiobookId)
                .throwIfHasErrors();
        return genreRepository.findByAudiobookId(audiobookId);
    }

    /**
     * Підраховує кількість аудіокниг, пов'язаних з жанром.
     *
     * @param genreId ідентифікатор жанру
     * @return кількість аудіокниг
     */
    @Override
    public long countAudiobooksByGenreId(UUID genreId) {
        new ValidationHelper()
                .validUuid("genreId", genreId)
                .throwIfHasErrors();
        return genreRepository.countAudiobooksByGenreId(genreId);
    }

    /**
     * Перевіряє існування жанру за назвою.
     *
     * @param name назва жанру
     * @return true, якщо жанр існує
     */
    @Override
    public boolean existsByName(String name) {
        new ValidationHelper()
                .notEmpty("name", name)
                .throwIfHasErrors();
        return genreRepository.existsByName(name);
    }

    /**
     * Валідує дані жанру перед створенням або оновленням з використанням нового підходу.
     *
     * @param genre жанр для валідації
     * @throws MultiFieldValidationException якщо порушено бізнес-правила
     */
    private void validateGenre(Genre genre) {
        ValidationHelper validator = new ValidationHelper()
                .notNull("genre", genre);

        if (genre != null) {
            validator.notEmpty("name", genre.getName());
        }

        validator.throwIfHasErrors();
    }
}