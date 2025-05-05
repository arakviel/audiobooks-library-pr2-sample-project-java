package com.arakviel.application.impl;

import com.arakviel.application.contract.AuthorService;
import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.file.exception.FileStorageException;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.AuthorRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація сервісу для управління сутностями авторів, включаючи операції з файлами фотографій та пов'язаними аудіокнигами.
 */
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final PersistenceContext persistenceContext;
    private final FileStorageService fileStorageService;

    /**
     * Конструктор для ін'єкції залежностей.
     *
     * @param authorRepository   репозиторій авторів
     * @param persistenceContext контекст для управління транзакціями
     * @param fileStorageService сервіс для роботи з файлами
     */
    public AuthorServiceImpl(
            AuthorRepository authorRepository,
            PersistenceContext persistenceContext,
            FileStorageService fileStorageService) {
        this.authorRepository = authorRepository;
        this.persistenceContext = persistenceContext;
        this.fileStorageService = fileStorageService;
    }

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
    @Override
    public Author create(Author author, InputStream photo, String photoName) {
        validateAuthor(author);
        if (author.getId() == null) {
            author.setId(UUID.randomUUID());
        }

        // Перевірка на дублювання автора
        List<Author> existingAuthors = authorRepository.findByName(author.getFirstName(), author.getLastName());
        if (!existingAuthors.isEmpty()) {
            throw new ValidationException("Автор з таким ім'ям та прізвищем уже існує.");
        }

        // Обробка завантаження фотографії
        if (photo != null && photoName != null) {
            Path photoPath = fileStorageService.save(photo, photoName, author.getId());
            author.setImagePath(photoPath.toString());
        }

        persistenceContext.registerNew(author);
        persistenceContext.commit();
        return author;
    }

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
    @Override
    public Author update(UUID id, Author author, InputStream photo, String photoName) {
        validateAuthor(author);
        author.setId(id);

        // Перевірка існування автора
        if (!authorRepository.findById(id).isPresent()) {
            throw new ValidationException("Автор з ідентифікатором " + id + " не існує.");
        }

        // Обробка існуючої фотографії
        if (author.getImagePath() != null && photo != null && photoName != null) {
            fileStorageService.delete(author.getImagePath(), id);
        }

        // Обробка нової фотографії
        if (photo != null && photoName != null) {
            Path photoPath = fileStorageService.save(photo, photoName, id);
            author.setImagePath(photoPath.toString());
        }

        persistenceContext.registerUpdated(id, author);
        persistenceContext.commit();
        return author;
    }

    /**
     * Видаляє автора та пов'язану фотографію, якщо автор не пов'язаний з аудіокнигами.
     *
     * @param id ідентифікатор автора для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо автор пов'язаний з аудіокнигами
     */
    @Override
    public void delete(UUID id) {
        Optional<Author> authorOpt = authorRepository.findById(id);
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();

            // Перевірка, чи автор пов'язаний з аудіокнигами
            if (countAudiobooksByAuthorId(id) > 0) {
                throw new ValidationException("Неможливо видалити автора, оскільки він пов'язаний з аудіокнигами.");
            }

            // Видалення фотографії
            if (author.getImagePath() != null) {
                fileStorageService.delete(author.getImagePath(), id);
            }

            persistenceContext.registerDeleted(author);
            persistenceContext.commit();
        }
    }

    /**
     * Знаходить автора за ідентифікатором.
     *
     * @param id ідентифікатор автора
     * @return Optional з автором, якщо знайдено
     */
    @Override
    public Optional<Author> findById(UUID id) {
        return authorRepository.findById(id);
    }

    /**
     * Знаходить всіх авторів з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список авторів
     */
    @Override
    public List<Author> findAll(int offset, int limit) {
        return authorRepository.findAll(offset, limit);
    }

    /**
     * Знаходить авторів за ім'ям та прізвищем.
     *
     * @param firstName ім'я автора
     * @param lastName  прізвище автора
     * @return список авторів
     */
    @Override
    public List<Author> findByName(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            throw new ValidationException("Ім'я та прізвище не можуть бути null.");
        }
        return authorRepository.findByName(firstName, lastName);
    }

    /**
     * Знаходить авторів за частковою відповідністю імені або прізвища.
     *
     * @param partialName часткове ім'я або прізвище
     * @return список авторів
     */
    @Override
    public List<Author> findByPartialName(String partialName) {
        if (partialName == null || partialName.trim().isEmpty()) {
            throw new ValidationException("Часткове ім'я не може бути null або порожнім.");
        }
        return authorRepository.findByPartialName(partialName);
    }

    /**
     * Знаходить аудіокниги, пов'язані з автором.
     *
     * @param authorId ідентифікатор автора
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findAudiobooksByAuthorId(UUID authorId) {
        if (authorId == null) {
            throw new ValidationException("Ідентифікатор автора не може бути null.");
        }
        return authorRepository.findAudiobooksByAuthorId(authorId);
    }

    /**
     * Підраховує кількість аудіокниг, пов'язаних з автором.
     *
     * @param authorId ідентифікатор автора
     * @return кількість аудіокниг
     */
    @Override
    public long countAudiobooksByAuthorId(UUID authorId) {
        if (authorId == null) {
            throw new ValidationException("Ідентифікатор автора не може бути null.");
        }
        return authorRepository.countAudiobooksByAuthorId(authorId);
    }

    /**
     * Валідує дані автора перед створенням або оновленням.
     *
     * @param author автор для валідації
     * @throws ValidationException якщо порушено бізнес-правила
     */
    private void validateAuthor(Author author) {
        if (author == null) {
            throw new ValidationException("Автор не може бути null.");
        }
        if (author.getFirstName() == null || author.getFirstName().trim().isEmpty()) {
            throw new ValidationException("Ім'я автора не може бути null або порожнім.");
        }
        if (author.getLastName() == null || author.getLastName().trim().isEmpty()) {
            throw new ValidationException("Прізвище автора не може бути null або порожнім.");
        }
    }
}