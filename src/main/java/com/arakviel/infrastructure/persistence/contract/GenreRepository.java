package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Інтерфейс репозиторію для специфічних операцій з жанрами.
 */
public interface GenreRepository extends Repository<Genre, UUID> {

    /**
     * Пошук жанру за назвою.
     *
     * @param name назва жанру
     * @return список жанрів
     */
    List<Genre> findByName(String name);

    /**
     * Пошук аудіокниг за ідентифікатором жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return список аудіокниг
     */
    List<Audiobook> findAudiobooksByGenreId(UUID genreId);

    /**
     * Пошук жанрів за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список жанрів
     */
    List<Genre> findByAudiobookId(UUID audiobookId);

    /**
     * Пошук жанрів за частковою відповідністю назви.
     *
     * @param partialName часткова назва жанру
     * @return список жанрів
     */
    List<Genre> findByPartialName(String partialName);

    /**
     * Підрахунок аудіокниг для жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return кількість аудіокниг
     */
    long countAudiobooksByGenreId(UUID genreId);

    /**
     * Перевірка існування жанру за назвою.
     *
     * @param name назва жанру
     * @return true, якщо жанр існує
     */
    boolean existsByName(String name);
}