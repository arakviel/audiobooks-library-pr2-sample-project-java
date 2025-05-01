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

    // TODO: мені треба List<Genre> по UUID audiobookId, а не findAudiobooksByGenreId.
}