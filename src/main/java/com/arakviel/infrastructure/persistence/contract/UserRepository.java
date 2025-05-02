package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Інтерфейс репозиторію для специфічних операцій з користувачами.
 */
public interface UserRepository extends Repository<User, UUID> {

    /**
     * Пошук користувача за ім’ям користувача.
     *
     * @param username ім’я користувача
     * @return список користувачів
     */
    List<User> findByUsername(String username);

    /**
     * Пошук користувача за електронною поштою.
     *
     * @param email електронна пошта
     * @return список користувачів
     */
    List<User> findByEmail(String email);

    /**
     * Пошук колекцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    List<Collection> findCollectionsByUserId(UUID userId);

    /**
     * Пошук прогресу прослуховування за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список записів прогресу
     */
    List<ListeningProgress> findListeningProgressByUserId(UUID userId);

    /**
     * Пошук користувачів за частковою відповідністю імені.
     *
     * @param partialUsername часткове ім’я користувача
     * @return список користувачів
     */
    List<User> findByPartialUsername(String partialUsername);

    /**
     * Підрахунок колекцій користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість колекцій
     */
    long countCollectionsByUserId(UUID userId);

    /**
     * Підрахунок записів прогресу прослуховування.
     *
     * @param userId ідентифікатор користувача
     * @return кількість записів прогресу
     */
    long countListeningProgressByUserId(UUID userId);

    /**
     * Перевірка існування користувача за ім’ям.
     *
     * @param username ім’я користувача
     * @return true, якщо користувач існує
     */
    boolean existsByUsername(String username);

    /**
     * Перевірка існування користувача за електронною поштою.
     *
     * @param email електронна пошта
     * @return true, якщо користувач існує
     */
    boolean existsByEmail(String email);
}