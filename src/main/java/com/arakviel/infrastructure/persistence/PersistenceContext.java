package com.arakviel.infrastructure.persistence;

import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реалізація патерну Unit of Work для управління транзакціями та змінами сутностей.
 * Відстежує створені, оновлені та видалені сутності, застосовуючи зміни в одній транзакції.
 */
public class PersistenceContext {

    private final ConnectionPool connectionPool;
    private Connection connection;
    private final Map<Class<?>, Repository<?, ?>> repositories;
    private final List<Object> newEntities;
    private final Map<Object, Object> updatedEntities; // Map<Id, Entity>
    private final List<Object> deletedEntities;

    /**
     * Конструктор для створення контексту з пулом з'єднань.
     *
     * @param connectionPool пул з'єднань для управління з'єднаннями
     */
    public PersistenceContext(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.repositories = new HashMap<>();
        this.newEntities = new ArrayList<>();
        this.updatedEntities = new HashMap<>();
        this.deletedEntities = new ArrayList<>();
        initializeConnection();
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
     */
    public void commit() {
        try {
            // Збереження нових сутностей
            for (Object entity : newEntities) {
                Repository<Object, Object> repository = getRepository(entity.getClass());
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
                Object id = extractId(entity);
                repository.delete(id);
            }

            // Коміт транзакції
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DatabaseAccessException("Помилка відкатування транзакції", rollbackEx);
            }
            throw new DatabaseAccessException("Помилка виконання транзакції", e);
        } finally {
            clear();
            closeConnection();
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
     * Ініціалізація з'єднання з пулом.
     */
    private void initializeConnection() {
        try {
            this.connection = connectionPool.getConnection();
            this.connection.setAutoCommit(false); // Вимикаємо автокоміт для транзакцій
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка ініціалізації з'єднання", e);
        }
    }

    /**
     * Закриття з'єднання (повернення в пул).
     */
    private void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close(); // Повертає в пул завдяки Proxy
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка закриття з'єднання", e);
        }
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

    /**
     * TODO: перенести це в репозиторій.
     * <p>
     * Витягнення ідентифікатора з сутності через рефлексію.
     *
     * @param entity сутність
     * @return ідентифікатор
     */
    private Object extractId(Object entity) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Не вдалося отримати ідентифікатор для " + entity.getClass().getSimpleName(), e);
        }
    }
}