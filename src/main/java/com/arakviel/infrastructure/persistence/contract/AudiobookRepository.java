package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Інтерфейс репозиторію для специфічних операцій з аудіокнигами.
 */
public interface AudiobookRepository extends Repository<Audiobook, UUID> {

    /**
     * Пошук аудіокниг за ідентифікатором автора.
     *
     * @param authorId ідентифікатор автора
     * @return список аудіокниг
     */
    List<Audiobook> findByAuthorId(UUID authorId);

    /**
     * Пошук аудіокниг за ідентифікатором жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return список аудіокниг
     */
    List<Audiobook> findByGenreId(UUID genreId);

    /**
     * Отримання всіх файлів аудіокниги за її ідентифікатором (зв’язок один-до-багатьох).
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список файлів аудіокниги
     */
    List<AudiobookFile> findFilesByAudiobookId(UUID audiobookId);

    // TODO: мені не потрібний findByCollectionId.

    /**
     * Пошук усіх аудіокниг у колекції користувача (зв’язок багато-до-багатьох).
     *
     * @param collectionId ідентифікатор колекції
     * @return список аудіокниг
     */
    List<Audiobook> findByCollectionId(UUID collectionId);
}