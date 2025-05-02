package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.AuthorRepository;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з авторами.
 */
@Repository
public class AuthorRepositoryImpl extends GenericRepository<Author, UUID> implements AuthorRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public AuthorRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Author.class, "authors");
    }

    /**
     * Пошук автора за ім’ям та прізвищем.
     *
     * @param firstName ім’я автора
     * @param lastName  прізвище автора
     * @return список авторів
     */
    @Override
    public List<Author> findByName(String firstName, String lastName) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("first_name = ?");
                    whereClause.add("last_name = ?");
                    params.add(firstName);
                    params.add(lastName);
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Пошук аудіокниг за ідентифікатором автора.
     *
     * @param authorId ідентифікатор автора
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findAudiobooksByAuthorId(UUID authorId) {
        String baseSql = "SELECT * FROM audiobooks WHERE author_id = ?";
        return executeQuery(baseSql, stmt -> stmt.setObject(1, authorId), this::mapResultSetToAudiobook);
    }

    /**
     * Пошук авторів за частковою відповідністю імені або прізвища.
     *
     * @param partialName часткове ім’я або прізвище
     * @return список авторів
     */
    @Override
    public List<Author> findByPartialName(String partialName) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("(first_name ILIKE ? OR last_name ILIKE ?)");
                    params.add("%" + partialName + "%");
                    params.add("%" + partialName + "%");
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Підрахунок аудіокниг для автора.
     *
     * @param authorId ідентифікатор автора
     * @return кількість аудіокниг
     */
    @Override
    public long countAudiobooksByAuthorId(UUID authorId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("author_id = ?");
            params.add(authorId);
        };
        return count(filter, "audiobooks");
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
}