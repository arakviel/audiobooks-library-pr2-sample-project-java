package com.arakviel.application.contract;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.infrastructure.file.exception.FileStorageException;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Інтерфейс для управління сутностями авторів, включаючи операції з файлами фотографій та пов'язаними аудіокнигами.
 */
public interface AuthorService {

    /**
     * Створює нового автора та, за потреби, завантажує фотографію.
     *
     * @param author    автор для створення
     * @param photo     потік даних фотографії, може бути null
     * @param photoName ім'я файлу фотографії, може бути null
     * @return створений автор
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо порушено бізнес-правила (наприклад, дублювання автора)
     */
    Author create(Author author, InputStream photo, String photoName);

    /**
     * Оновлює існуючого автора та, за потреби, оновлює фотографію.
     *
     * @param id        ідентифікатор автора для оновлення
     * @param author    оновлені дані автора
     * @param photo     потік даних нової фотографії, може бути null
     * @param photoName ім'я файлу нової фотографії, може бути null
     * @return оновлений автор
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    Author update(UUID id, Author author, InputStream photo, String photoName);

    /**
     * Видаляє автора та пов'язану фотографію, якщо автор не пов'язаний з аудіокнигами.
     *
     * @param id ідентифікатор автора для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо автор пов'язаний з аудіокнигами
     */
    void delete(UUID id);

    /**
     * Знаходить автора за ідентифікатором.
     *
     * @param id ідентифікатор автора
     * @return Optional з автором, якщо знайдено
     */
    Optional<Author> findById(UUID id);

    /**
     * Знаходить всіх авторів з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список авторів
     */
    List<Author> findAll(int offset, int limit);

    /**
     * Знаходить авторів за ім'ям та прізвищем.
     *
     * @param firstName ім'я автора
     * @param lastName  прізвище автора
     * @return список авторів
     */
    List<Author> findByName(String firstName, String lastName);

    /**
     * Знаходить авторів за частковою відповідністю імені або прізвища.
     *
     * @param partialName часткове ім'я або прізвище
     * @return список авторів
     */
    List<Author> findByPartialName(String partialName);

    /**
     * Знаходить аудіокниги, пов'язані з автором.
     *
     * @param authorId ідентифікатор автора
     * @return список аудіокниг
     */
    List<Audiobook> findAudiobooksByAuthorId(UUID authorId);

    /**
     * Підраховує кількість аудіокниг, пов'язаних з автором.
     *
     * @param authorId ідентифікатор автора
     * @return кількість аудіокниг
     */
    long countAudiobooksByAuthorId(UUID authorId);
}