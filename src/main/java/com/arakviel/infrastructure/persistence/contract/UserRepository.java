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
}