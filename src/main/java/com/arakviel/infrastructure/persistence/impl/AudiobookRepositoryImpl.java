package com.arakviel.infrastructure.persistence.impl;

import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.AudiobookFile;
import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.persistence.GenericRepository;
import com.arakviel.infrastructure.persistence.contract.AudiobookRepository;
import com.arakviel.infrastructure.persistence.exception.EntityMappingException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з аудіокнигами.
 */
@Repository
public class AudiobookRepositoryImpl extends GenericRepository<Audiobook, UUID> implements AudiobookRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public AudiobookRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Audiobook.class, "audiobooks");
    }

    /**
     * Пошук аудіокниг за ідентифікатором автора.
     *
     * @param authorId ідентифікатор автора
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findByAuthorId(UUID authorId) {
        return findByField("author_id", authorId);
    }

    /**
     * Пошук аудіокниг за ідентифікатором жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findByGenreId(UUID genreId) {
        return findByField("genre_id", genreId);
    }

    /**
     * Отримання всіх файлів аудіокниги за її ідентифікатором (зв’язок один-до-багатьох).
     *
     * @param audiobookId ідентифікатор аудіокниги
     * @return список файлів аудіокниги
     */
    @Override
    public List<AudiobookFile> findFilesByAudiobookId(UUID audiobookId) {
        String sql = "SELECT * FROM audiobook_files WHERE audiobook_id = ?";
        return executeQuery(sql, stmt -> stmt.setObject(1, audiobookId), this::mapResultSetToAudiobookFiles);
    }

    /**
     * Пошук усіх аудіокниг у колекції користувача (зв’язок багато-до-багатьох).
     *
     * @param collectionId ідентифікатор колекції
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findByCollectionId(UUID collectionId) {
        String baseSql = "SELECT a.* FROM audiobooks a JOIN collection_audiobooks ca ON a.id = ca.audiobook_id";
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("ca.collection_id = ?");
                    params.add(collectionId);
                },
                null, true, 0, Integer.MAX_VALUE, baseSql
        );
    }

    /**
     * Пошук аудіокниг за роком випуску.
     *
     * @param year рік випуску
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findByReleaseYear(int year) {
        return findByField("release_year", year);
    }

    /**
     * Пошук аудіокниг за діапазоном тривалості.
     *
     * @param minDuration мінімальна тривалість (у секундах)
     * @param maxDuration максимальна тривалість (у секундах)
     * @return список аудіокниг
     */
    @Override
    public List<Audiobook> findByDurationRange(int minDuration, int maxDuration) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("duration >= ?");
                    whereClause.add("duration <= ?");
                    params.add(minDuration);
                    params.add(maxDuration);
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Підрахунок кількості аудіокниг для автора.
     *
     * @param authorId ідентифікатор автора
     * @return кількість аудіокниг
     */
    @Override
    public long countByAuthorId(UUID authorId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("author_id = ?");
            params.add(authorId);
        };
        return count(filter);
    }

    /**
     * Підрахунок кількості аудіокниг для жанру.
     *
     * @param genreId ідентифікатор жанру
     * @return кількість аудіокниг
     */
    @Override
    public long countByGenreId(UUID genreId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("genre_id = ?");
            params.add(genreId);
        };
        return count(filter);
    }

    @Override
    public List<Audiobook> findByTitle(String title) {
        return findByField("title", title);
    }

    @Override
    public List<Audiobook> findByPartialTitle(String partialTitle) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("title ILIKE ?");
                    params.add("%" + partialTitle + "%");
                },
                null, true, 0, Integer.MAX_VALUE
        );
    }

    @Override
    public List<Audiobook> findByPublicationYear(int year) {
        return findByField("release_year", year);
    }

    @Override
    public List<Audiobook> findMostPopular(int limit) {
        // Простий підхід - сортування за назвою (в реальному проекті - за кількістю прослуховувань)
        return findAll(null, "title", true, 0, limit);
    }

    @Override
    public List<Audiobook> findRecentlyAdded(int limit) {
        // Простий підхід - сортування за роком випуску (в реальному проекті - за датою додавання)
        return findAll(null, "release_year", false, 0, limit);
    }

    @Override
    public long calculateTotalDuration() {
        // Простий підрахунок через суму - в реальному проекті краще використати SQL SUM
        List<Audiobook> audiobooks = findAll();
        return audiobooks.stream().mapToLong(Audiobook::getDuration).sum();
    }

    @Override
    public double calculateAverageDuration() {
        List<Audiobook> audiobooks = findAll();
        if (audiobooks.isEmpty()) {
            return 0.0;
        }
        return audiobooks.stream().mapToLong(Audiobook::getDuration).average().orElse(0.0);
    }

    @Override
    public java.util.Optional<Audiobook> findLongest() {
        List<Audiobook> audiobooks = findAll();
        return audiobooks.stream().max((a1, a2) -> Integer.compare(a1.getDuration(), a2.getDuration()));
    }

    @Override
    public java.util.Optional<Audiobook> findShortest() {
        List<Audiobook> audiobooks = findAll();
        return audiobooks.stream().min((a1, a2) -> Integer.compare(a1.getDuration(), a2.getDuration()));
    }

    @Override
    public boolean existsByTitleAndAuthorId(String title, UUID authorId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("title = ?");
            whereClause.add("author_id = ?");
            params.add(title);
            params.add(authorId);
        };
        return count(filter) > 0;
    }

    @Override
    public List<Audiobook> findSimilar(UUID audiobookId, int limit) {
        // Простий підхід - знаходимо аудіокниги того ж автора або жанру
        java.util.Optional<Audiobook> audiobookOpt = findById(audiobookId);
        if (!audiobookOpt.isPresent()) {
            return java.util.Collections.emptyList();
        }

        Audiobook audiobook = audiobookOpt.get();
        List<Audiobook> similar = new java.util.ArrayList<>();

        // Додаємо аудіокниги того ж автора
        similar.addAll(findByAuthorId(audiobook.getAuthorId()));

        // Додаємо аудіокниги того ж жанру
        similar.addAll(findByGenreId(audiobook.getGenreId()));

        // Видаляємо оригінальну аудіокнигу та дублікати
        return similar.stream()
                .filter(a -> !a.getId().equals(audiobookId))
                .distinct()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Зіставлення ResultSet у список файлів аудіокниги.
     *
     * @param rs результат запиту
     * @return список файлів аудіокниги
     */
    private AudiobookFile mapResultSetToAudiobookFiles(ResultSet rs) {
        try {
            AudiobookFile file = new AudiobookFile();
            file.setId(rs.getObject("id", UUID.class));
            file.setAudiobookId(rs.getObject("audiobook_id", UUID.class));
            file.setFilePath(rs.getString("file_path"));
            String format = rs.getString("format");
            file.setFormat(format != null ? FileFormat.valueOf(format.toUpperCase()) : null);
            file.setSize(rs.getInt("size"));
            return file;
        } catch (Exception e) {
            throw new EntityMappingException("Помилка зіставлення ResultSet із файлом аудіокниги", e);
        }
    }
}