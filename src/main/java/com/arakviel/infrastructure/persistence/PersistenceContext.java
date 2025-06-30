package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.entities.*;
import com.arakviel.infrastructure.persistence.contract.*;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реалізація патерну Unit of Work для управління транзакціями та змінами сутностей.
 * Відстежує створені, оновлені та видалені сутності, застосовуючи зміни в одній транзакції.
 *
 * Нова архітектура:
 * - З'єднання створюється тільки під час commit()
 * - Автоматичне закриття з'єднань через try-with-resources
 * - Thread-safe операції
 * - Proper lifecycle management
 */
@Component
public class PersistenceContext implements AutoCloseable {

    private final ConnectionPool connectionPool;
    private final AudiobookRepository audiobookRepository;
    private final AudiobookFileRepository audiobookFileRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final CollectionRepository collectionRepository;
    private final ListeningProgressRepository listeningProgressRepository;
    private final UserRepository userRepository;

    // Видаляємо connection як поле класу - тепер воно створюється тільки під час commit
    private final Map<Class<?>, Repository<?, ?>> repositories;
    private final List<Object> newEntities;
    private final Map<Object, Object> updatedEntities; // Map<Id, Entity>
    private final List<Object> deletedEntities;

    // Додаємо флаг для відстеження стану
    private volatile boolean isActive = true;

    /**
     * Конструктор для створення контексту з пулом з'єднань.
     *
     * @param connectionPool пул з'єднань для управління з'єднаннями
     */
    public PersistenceContext(ConnectionPool connectionPool,
                              AudiobookRepository audiobookRepository,
                              AudiobookFileRepository audiobookFileRepository,
                              AuthorRepository authorRepository,
                              GenreRepository genreRepository,
                              CollectionRepository collectionRepository,
                              ListeningProgressRepository listeningProgressRepository,
                              UserRepository userRepository) {
        this.connectionPool = connectionPool;
        this.audiobookRepository = audiobookRepository;
        this.audiobookFileRepository = audiobookFileRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.collectionRepository = collectionRepository;
        this.listeningProgressRepository = listeningProgressRepository;
        this.userRepository = userRepository;

        this.repositories = new HashMap<>();
        this.newEntities = new ArrayList<>();
        this.updatedEntities = new HashMap<>();
        this.deletedEntities = new ArrayList<>();

        // Ініціалізуємо репозиторії в конструкторі замість @PostConstruct
        this.registerRepository(Audiobook.class, audiobookRepository);
        this.registerRepository(AudiobookFile.class, audiobookFileRepository);
        this.registerRepository(Author.class, authorRepository);
        this.registerRepository(Genre.class, genreRepository);
        this.registerRepository(Collection.class, collectionRepository);
        this.registerRepository(ListeningProgress.class, listeningProgressRepository);
        this.registerRepository(User.class, userRepository);
    }

    /**
     * Реєстрація репозиторію для певного типу сутності.
     *
     * @param entityClass клас сутності
     * @param repository  репозиторій для роботи з сутністю
     */
    public <T, ID> void registerRepository(Class<T> entityClass, Repository<T, ID> repository) {
        repositories.put(entityClass, repository);
    }

    /**
     * Реєстрація нової сутності для збереження.
     *
     * @param entity сутність для створення
     */
    public void registerNew(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сутність не може бути null");
        }
        newEntities.add(entity);
    }

    /**
     * Реєстрація сутності для оновлення.
     *
     * @param id     ідентифікатор сутності
     * @param entity сутність з новими даними
     */
    public void registerUpdated(Object id, Object entity) {
        if (id == null || entity == null) {
            throw new IllegalArgumentException("Ідентифікатор або сутність не можуть бути null");
        }
        updatedEntities.put(id, entity);
    }

    /**
     * Реєстрація сутності для видалення.
     *
     * @param entity сутність для видалення
     */
    public void registerDeleted(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сутність не може бути null");
        }
        deletedEntities.add(entity);
    }

    /**
     * Застосування всіх зареєстрованих змін у транзакції.
     * Нова архітектура: з'єднання створюється тільки під час commit() та автоматично закривається.
     */
    public void commit() {
        if (!isActive) {
            throw new IllegalStateException("PersistenceContext is not active");
        }

        // Якщо немає змін, нічого не робимо
        if (newEntities.isEmpty() && updatedEntities.isEmpty() && deletedEntities.isEmpty()) {
            return;
        }

        // Використовуємо try-with-resources для автоматичного закриття з'єднання
        try (Connection connection = connectionPool.getConnection()) {
            connection.setAutoCommit(false); // Вимикаємо автокоміт для транзакції

            try {
                // Збереження нових сутностей
                for (Object entity : newEntities) {
                    Repository<Object, Object> repository = getRepository(entity.getClass());
                    System.out.println("Saving entity: " + entity); // Логування
                    repository.save(entity);
                }

                // Оновлення сутностей
                for (Map.Entry<Object, Object> entry : updatedEntities.entrySet()) {
                    Repository<Object, Object> repository = getRepository(entry.getValue().getClass());
                    repository.update(entry.getKey(), entry.getValue());
                }

                // Видалення сутностей
                for (Object entity : deletedEntities) {
                    Repository<Object, Object> repository = getRepository(entity.getClass());
                    Object id = repository.extractId(entity);
                    repository.delete(id);
                }

                // Коміт транзакції
                connection.commit();

            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw new DatabaseAccessException("Помилка виконання транзакції", e);
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка отримання з'єднання", e);
        } finally {
            // Очищуємо контекст після commit
            clear();
        }
    }

    /**
     * Очищення списків змінених сутностей.
     */
    private void clear() {
        newEntities.clear();
        updatedEntities.clear();
        deletedEntities.clear();
    }

    /**
     * Публічний метод для очищення контексту.
     * Використовується в тестах для забезпечення чистого стану.
     */
    public void clearAndClose() {
        clear();
        // Більше не потрібно закривати з'єднання, оскільки воно не зберігається як поле
    }

    /**
     * Ініціалізація нового з'єднання для тестів.
     * Тепер це просто очищення контексту.
     */
    public void initNewConnection() {
        clear();
        // З'єднання створюється автоматично під час наступного commit()
    }

    /**
     * Реалізація AutoCloseable для автоматичного закриття ресурсів.
     */
    @Override
    public void close() {
        isActive = false;
        clear();
        // З'єднання автоматично закриваються через try-with-resources в commit()
    }

    /**
     * Перевірка, чи активний контекст.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Активація контексту (для повторного використання).
     */
    public void activate() {
        isActive = true;
    }

    /**
     * Отримання репозиторію для певного типу сутності.
     *
     * @param entityClass клас сутності
     * @return відповідний репозиторій
     */
    @SuppressWarnings("unchecked")
    private <T, ID> Repository<T, ID> getRepository(Class<?> entityClass) {
        Repository<T, ID> repository = (Repository<T, ID>) repositories.get(entityClass);
        if (repository == null) {
            throw new IllegalStateException("Репозиторій для " + entityClass.getSimpleName() + " не зареєстровано");
        }
        return repository;
    }
}