package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Genre;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.GenreRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з жанрами.
 */
@Repository
public class GenreRepositoryImpl extends GenericRepository<Genre, UUID> implements GenreRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public GenreRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Genre.class, "genres");
    }

    /**
     * Пошук жанру за назвою.
     *
     * @param name назва жанру
     * @return список жанрів
     */
    @Override
    public List<Genre> findByName(String name) {
        return findByField("name", name);
    }

    /**
     * Пошук аудіокниг за ідентифікатором жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findAudiobooksByGenreId(UUID genreId) {
        String baseSql = "SELECT * FROM audiobooks WHERE genre_id = ?";
        return executeQuery(baseSql, stmt -> stmt.setObject(1, genreId), this::mapResultSetToAudiobook);
    }

    /**
     * Пошук жанрів за ідентифікатором аудіокниги.
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список жанрів
     */
    @Override
    public List<Genre> findByAudiobookId(UUID audiobookId) {
        String baseSql = "SELECT g.* FROM genres g JOIN audiobooks a ON g.id = a.genre_id WHERE a.id = ?";
        return executeQuery(baseSql, stmt -> stmt.setObject(1, audiobookId), this::mapResultSetToGenre);
    }

    /**
     * Пошук жанрів за частковою відповідністю назви.
     *
     * @param partialName часткова назва жанру
     * @return список жанрів
     */
    @Override
    public List<Genre> findByPartialName(String partialName) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("name ILIKE ?");
                    params.add("%" + partialName + "%");
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Підрахунок аудіокниг для жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return кількість аудіокниг
     */
    @Override
    public long countAudiobooksByGenreId(UUID genreId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("genre_id = ?");
            params.add(genreId);
        };
        return count(filter, "audiobooks");
    }

    /**
     * Перевірка існування жанру за назвою.
     *
     * @param name назва жанру
     * @return true, якщо жанр існує
     */
    @Override
    public boolean existsByName(String name) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("name = ?");
            params.add(name);
        };
        return count(filter) > 0;
    }

    /**
     * Зіставлення ResultSet у аудіокнигу.
     *
     * @param rs результат запиту
     * @return аудіокнига
     */
    private Audiobook mapResultSetToAudiobook(ResultSet rs) {
        try {
            Audiobook audiobook = new Audiobook();
            audiobook.setId(rs.getObject("id", UUID.class));
            audiobook.setAuthorId(rs.getObject("author_id", UUID.class));
            audiobook.setGenreId(rs.getObject("genre_id", UUID.class));
            audiobook.setTitle(rs.getString("title"));
            audiobook.setDuration(rs.getInt("duration"));
            audiobook.setReleaseYear(rs.getInt("release_year"));
            audiobook.setDescription(rs.getString("description"));
            audiobook.setCoverImagePath(rs.getString("cover_image_path"));
            return audiobook;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet із аудіокнигою", e);
        }
    }

    /**
     * Зіставлення ResultSet у жанр.
     *
     * @param rs результат запиту
     * @return жанр
     */
    private Genre mapResultSetToGenre(ResultSet rs) {
        try {
            Genre genre = new Genre();
            genre.setId(rs.getObject("id", UUID.class));
            genre.setName(rs.getString("name"));
            genre.setDescription(rs.getString("description"));
            return genre;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet із жанром", e);
        }
    }
}