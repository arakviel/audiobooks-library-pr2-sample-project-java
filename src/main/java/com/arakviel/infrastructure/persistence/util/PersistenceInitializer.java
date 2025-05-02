package com.arakviel.infrastructure.persistence.util;

import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ініціалізатор бази даних для виконання DDL та DML скриптів.
 */
@Component
public class PersistenceInitializer {

    private static final String DDL_SCRIPT_PATH = "db/ddl_h2.sql";
    private static final String DML_SCRIPT_PATH = "db/dml_h2.sql";
    private final ConnectionPool connectionPool;

    /**
     * Конструктор ініціалізатора.
     *
     * @param connectionPool пул з'єднань для управління з'єднаннями
     */
    public PersistenceInitializer(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * Ініціалізація бази даних: виконання DDL та DML скриптів.
     */
    public void init() {
        init(true);
    }

    /**
     * Ініціалізація бази даних: виконання DDL та DML скриптів.
     */
    public void init(boolean isRunDml) {
        try (Connection connection = connectionPool.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);

            // Спочатку видаляємо всі таблиці, якщо вони існують
            try {
                statement.execute("DROP ALL OBJECTS");
            } catch (SQLException e) {
                // Ігноруємо помилку, якщо таблиць немає
            }

            // Створюємо таблиці та заповнюємо їх даними
            statement.execute(getSQL(DDL_SCRIPT_PATH));
            if (isRunDml)
                statement.execute(getSQL(DML_SCRIPT_PATH));
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка ініціалізації бази даних", e);
        }
    }

    /**
     * Зчитування SQL-скрипту з ресурсів.
     *
     * @param resourcePath шлях до SQL-файлу в ресурсах
     * @return вміст SQL-скрипту
     */
    private String getSQL(String resourcePath) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                PersistenceInitializer.class.getClassLoader().getResourceAsStream(resourcePath))))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зчитування SQL-скрипту: " + resourcePath, e);
        }
    }
}