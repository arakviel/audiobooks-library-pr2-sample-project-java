package com.arakviel.infrastructure.persistence.util;

import java.sql.Connection;

/**
 * Утримувач з'єднання для забезпечення використання одного з'єднання в межах транзакції.
 * Використовує ThreadLocal для зберігання з'єднання для поточного потоку.
 */
public class ConnectionHolder {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    /**
     * Встановлює з'єднання для поточного потоку.
     *
     * @param connection з'єднання з базою даних
     */
    public static void setConnection(Connection connection) {
        connectionHolder.set(connection);
    }

    /**
     * Отримує з'єднання для поточного потоку.
     *
     * @return з'єднання з базою даних або null, якщо не встановлено
     */
    public static Connection getConnection() {
        return connectionHolder.get();
    }

    /**
     * Очищає з'єднання для поточного потоку.
     */
    public static void clearConnection() {
        connectionHolder.remove();
    }
}
