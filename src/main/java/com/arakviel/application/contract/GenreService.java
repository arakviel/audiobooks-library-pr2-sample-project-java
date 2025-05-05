package com.arakviel.application.contract;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Інтерфейс для управління сутностями жанрів, включаючи операції з пов'язаними аудіокнигами.
 */
public interface GenreService {

    /**
     * Створює новий жанр.
     *
     * @param genre жанр для створення
     * @return створений жанр
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила (наприклад, дублювання жанру)
     */
    Genre create(Genre genre);

    /**
     * Оновлює існуючий жанр.
     *
     * @param id    ідентифікатор жанру для оновлення
     * @param genre оновлені дані жанру
     * @return оновлений жанр
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    Genre update(UUID id, Genre genre);

    /**
     * Видаляє жанр, якщо він не пов'язаний з аудіокнигами.
     *
     * @param id ідентифікатор жанру для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws ValidationException     якщо жанр пов'язаний з аудіокнигами
     */
    void delete(UUID id);

    /**
     * Знаходить жанр за ідентифікатором.
     *
     * @param id ідентифікатор жанру
     * @return Optional з жанром, якщо знайдено
     */
    Optional<Genre> findById(UUID id);

    /**
     * Знаходить всі жанри з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список жанрів
     */
    List<Genre> findAll(int offset, int limit);

    /**
     * Знаходить жанри за назвою.
     *
     * @param name назва жанру
     * @return список жанрів
     */
    List<Genre> findByName(String name);

    /**
     * Знаходить жанри за частковою відповідністю назви.
     *
     * @param partialName часткова назва жанру
     * @return список жанрів
     */
    List<Genre> findByPartialName(String partialName);

    /**
     * Знаходить аудіокниги, пов'язані з жанром.
     *
     * @param genreId ідентифікатор жанру
     * @return список аудіокниг
     */
    List<Audiobook> findAudiobooksByGenreId(UUID genreId);

    /**
     * Знаходить жанри, пов'язані з аудіокнигою.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список жанрів
     */
    List<Genre> findByAudiobookId(UUID audiobookId);

    /**
     * Підраховує кількість аудіокниг, пов'язаних з жанром.
     *
     * @param genreId ідентифікатор жанру
     * @return кількість аудіокниг
     */
    long countAudiobooksByGenreId(UUID genreId);

    /**
     * Перевіряє існування жанру за назвою.
     *
     * @param name назва жанру
     * @return true, якщо жанр існує
     */
    boolean existsByName(String name);
}