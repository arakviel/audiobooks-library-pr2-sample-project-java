package com.arakviel.infrastructure.persistence.contract;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.infrastructure.persistence.Repository;

import java.util.List;
import java.util.Optional;
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

    /**
     * Пошук усіх аудіокниг у колекції користувача (зв’язок багато-до-багатьох).
     *
     * @param collectionId ідентифікатор колекції
     * @return список аудіокниг
     */
    List<Audiobook> findByCollectionId(UUID collectionId);

    /**
     * Пошук аудіокниг за роком випуску.
     *
     * @param year рік випуску
     * @return список аудіокниг
     */
    List<Audiobook> findByReleaseYear(int year);

    /**
     * Пошук аудіокниг за діапазоном тривалості.
     *
     * @param minDuration мінімальна тривалість (у секундах)
     * @param maxDuration максимальна тривалість (у секундах)
     * @return список аудіокниг
     */
    List<Audiobook> findByDurationRange(int minDuration, int maxDuration);

    /**
     * Підрахунок кількості аудіокниг для автора.
     *
     * @param authorId ідентифікатор автора
     * @return кількість аудіокниг
     */
    long countByAuthorId(UUID authorId);

    /**
     * Підрахунок кількості аудіокниг для жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return кількість аудіокниг
     */
    long countByGenreId(UUID genreId);

    /**
     * Знаходить аудіокниги за назвою.
     *
     * @param title назва аудіокниги
     * @return список аудіокниг
     */
    List<Audiobook> findByTitle(String title);

    /**
     * Знаходить аудіокниги за частковою відповідністю назви.
     *
     * @param partialTitle часткова назва аудіокниги
     * @return список аудіокниг
     */
    List<Audiobook> findByPartialTitle(String partialTitle);

    /**
     * Знаходить аудіокниги за роком публікації.
     *
     * @param year рік публікації
     * @return список аудіокниг
     */
    List<Audiobook> findByPublicationYear(int year);

    /**
     * Знаходить найпопулярніші аудіокниги (за кількістю прослуховувань).
     *
     * @param limit кількість записів для отримання
     * @return список популярних аудіокниг
     */
    List<Audiobook> findMostPopular(int limit);

    /**
     * Знаходить нещодавно додані аудіокниги.
     *
     * @param limit кількість записів для отримання
     * @return список нещодавно доданих аудіокниг
     */
    List<Audiobook> findRecentlyAdded(int limit);

    /**
     * Обчислює загальну тривалість всіх аудіокниг.
     *
     * @return загальна тривалість у секундах
     */
    long calculateTotalDuration();

    /**
     * Обчислює середню тривалість аудіокниг.
     *
     * @return середня тривалість у секундах
     */
    double calculateAverageDuration();

    /**
     * Знаходить найдовшу аудіокнигу.
     *
     * @return Optional з найдовшою аудіокнигою, якщо знайдено
     */
    Optional<Audiobook> findLongest();

    /**
     * Знаходить найкоротшу аудіокнигу.
     *
     * @return Optional з найкоротшою аудіокнигою, якщо знайдено
     */
    Optional<Audiobook> findShortest();

    /**
     * Перевіряє, чи існує аудіокнига з такою назвою та автором.
     *
     * @param title    назва аудіокниги
     * @param authorId ідентифікатор автора
     * @return true, якщо аудіокнига існує
     */
    boolean existsByTitleAndAuthorId(String title, UUID authorId);

    /**
     * Знаходить схожі аудіокниги за жанром та автором.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @param limit       кількість записів для отримання
     * @return список схожих аудіокниг
     */
    List<Audiobook> findSimilar(UUID audiobookId, int limit);
}