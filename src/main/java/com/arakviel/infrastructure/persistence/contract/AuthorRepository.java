package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Інтерфейс репозиторію для специфічних операцій з авторами.
 */
public interface AuthorRepository extends Repository<Author, UUID> {

    /**
     * Пошук автора за ім’ям та прізвищем.
     *
     * @param firstName ім’я автора
     * @param lastName  прізвище автора
     * @return список авторів
     */
    List<Author> findByName(String firstName, String lastName);

    /**
     * Пошук аудіокниг за ідентифікатором автора.
     *
     * @param authorId ідентифікатор автора
     * @return список аудіокниг
     */
    List<Audiobook> findAudiobooksByAuthorId(UUID authorId);

    /**
     * Пошук авторів за частковою відповідністю імені або прізвища.
     *
     * @param partialName часткове ім’я або прізвище
     * @return список авторів
     */
    List<Author> findByPartialName(String partialName);

    /**
     * Підрахунок аудіокниг для автора.
     *
     * @param authorId ідентифікатор автора
     * @return кількість аудіокниг
     */
    long countAudiobooksByAuthorId(UUID authorId);
}