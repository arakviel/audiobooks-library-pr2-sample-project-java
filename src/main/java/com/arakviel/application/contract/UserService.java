package com.arakviel.application.contract;

import com.arakviel.application.exception.ValidationException;
import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.file.exception.FileStorageException;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Інтерфейс для управління сутностями користувачів, включаючи операції з аватарами та пов'язаними даними.
 */
public interface UserService {

    /**
     * Створює нового користувача та, за потреби, завантажує аватар.
     *
     * @param user       користувач для створення
     * @param avatar     потік даних аватара, може бути null
     * @param avatarName ім'я файлу аватара, може бути null
     * @return створений користувач
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо порушено бізнес-правила (наприклад, дублювання username або email)
     */
    User create(User user, InputStream avatar, String avatarName);

    /**
     * Оновлює існуючого користувача та, за потреби, оновлює аватар.
     *
     * @param id         ідентифікатор користувача для оновлення
     * @param user       оновлені дані користувача
     * @param avatar     потік даних нового аватара, може бути null
     * @param avatarName ім'я файлу нового аватара, може бути null
     * @return оновлений користувач
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     * @throws ValidationException     якщо порушено бізнес-правила
     */
    User update(UUID id, User user, InputStream avatar, String avatarName);

    /**
     * Видаляє користувача та пов'язаний аватар.
     * Також видаляє всі пов'язані дані (колекції, прогрес прослуховування).
     *
     * @param id ідентифікатор користувача для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException    якщо виникає помилка при роботі з файлами
     */
    void delete(UUID id);

    /**
     * Знаходить користувача за ідентифікатором.
     *
     * @param id ідентифікатор користувача
     * @return Optional з користувачем, якщо знайдено
     */
    Optional<User> findById(UUID id);

    /**
     * Знаходить всіх користувачів з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  кількість записів для отримання
     * @return список користувачів
     */
    List<User> findAll(int offset, int limit);

    /**
     * Знаходить користувача за username.
     *
     * @param username ім'я користувача
     * @return список користувачів (зазвичай один або порожній)
     */
    List<User> findByUsername(String username);

    /**
     * Знаходить користувача за email.
     *
     * @param email електронна пошта користувача
     * @return список користувачів (зазвичай один або порожній)
     */
    List<User> findByEmail(String email);

    /**
     * Перевіряє, чи існує користувач з таким username.
     *
     * @param username ім'я користувача
     * @return true, якщо користувач існує
     */
    boolean existsByUsername(String username);

    /**
     * Перевіряє, чи існує користувач з таким email.
     *
     * @param email електронна пошта
     * @return true, якщо користувач існує
     */
    boolean existsByEmail(String email);

    /**
     * Знаходить колекції користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    List<Collection> findCollectionsByUserId(UUID userId);

    /**
     * Знаходить прогрес прослуховування користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список записів прогресу
     */
    List<ListeningProgress> findListeningProgressByUserId(UUID userId);

    /**
     * Підраховує кількість колекцій користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість колекцій
     */
    long countCollectionsByUserId(UUID userId);

    /**
     * Підраховує кількість записів прогресу прослуховування користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість записів прогресу
     */
    long countListeningProgressByUserId(UUID userId);

    /**
     * Змінює пароль користувача.
     *
     * @param userId      ідентифікатор користувача
     * @param oldPassword старий пароль
     * @param newPassword новий пароль
     * @throws ValidationException якщо старий пароль неправильний
     */
    void changePassword(UUID userId, String oldPassword, String newPassword);

    /**
     * Аутентифікація користувача.
     *
     * @param username ім'я користувача
     * @param password пароль
     * @return Optional з користувачем, якщо аутентифікація успішна
     */
    Optional<User> authenticate(String username, String password);
}
