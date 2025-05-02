package com.arakviel.infrastructure.persistence;

import com.arakviel.domain.enums.FileFormat;
import com.arakviel.infrastructure.persistence.exception.DatabaseAccessException;
import com.arakviel.infrastructure.persistence.exception.EntityMappingException;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * Абстрактний клас для загальних операцій з базою даних.
 *
 * @param <T>  тип сутності
 * @param <ID> тип ідентифікатора сутності
 */
public abstract class GenericRepository<T, ID> implements Repository<T, ID> {

    protected final ConnectionPool connectionPool;
    protected final Class<T> entityClass;
    protected final String tableName;

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     * @param entityClass    клас сутності
     * @param tableName      назва таблиці в базі даних
     */
    protected GenericRepository(ConnectionPool connectionPool, Class<T> entityClass, String tableName) {
        this.connectionPool = connectionPool;
        this.entityClass = entityClass;
        this.tableName = tableName;
    }


    /**
     * Пошук сутності за ідентифікатором.
     *
     * @param id ідентифікатор сутності
     * @return Optional із знайденою сутністю або порожній, якщо не знайдено
     */
    @Override
    public Optional<T> findById(ID id) {
        return findByField("id", id).stream().findFirst();
    }

    /**
     * Пошук сутностей за значенням поля.
     *
     * @param fieldName назва поля
     * @param value     значення поля
     * @return список знайдених сутностей
     */
    @Override
    public List<T> findByField(String fieldName, Object value) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, fieldName);
        return executeQuery(sql, stmt -> stmt.setObject(1, value));
    }

    /**
     * Пошук усіх сутностей із кастомним SQL-запитом, фільтрацією, сортуванням і пагінацією.
     *
     * @param filter      фільтр для вибірки та пошуку (може бути null)
     * @param sortBy      поле для сортування (може бути null)
     * @param isAscending напрямок сортування (true - за зростанням)
     * @param offset      зміщення для пагінації
     * @param limit       ліміт кількості записів
     * @param baseSql     базовий SQL-запит (наприклад, із JOIN)
     * @return список знайдених сутностей
     */
    @Override
    public List<T> findAll(Filter filter, String sortBy, boolean isAscending, int offset, int limit, String baseSql) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add(baseSql);
        List<Object> parameters = new ArrayList<>();

        if (filter != null) {
            StringJoiner whereClause = new StringJoiner(" AND ", " WHERE ", "");
            filter.apply(whereClause, parameters);
            sql.add(whereClause.toString());
        }
        if (sortBy != null && !sortBy.isEmpty()) {
            sql.add("ORDER BY " + sortBy + (isAscending ? " ASC" : " DESC"));
        }
        sql.add("LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        return executeQuery(sql.toString(), stmt -> setParameters(stmt, parameters));
    }

    /**
     * Пошук усіх сутностей з фільтрацією, пошуком, сортуванням і пагінацією.
     *
     * @param filter      фільтр для вибірки та пошуку (може бути null)
     * @param sortBy      поле для сортування (може бути null)
     * @param isAscending напрямок сортування (true - за зростанням)
     * @param offset      зміщення для пагінації
     * @param limit       ліміт кількості записів
     * @return список знайдених сутностей
     */
    @Override
    public List<T> findAll(Filter filter, String sortBy, boolean isAscending, int offset, int limit) {
        return findAll(filter, sortBy, isAscending, offset, limit, String.format("SELECT * FROM %s", tableName));
    }

    /**
     * Пошук усіх сутностей без фільтрації, пошуку та сортування.
     *
     * @return список усіх сутностей
     */
    @Override
    public List<T> findAll() {
        String sql = String.format("SELECT * FROM %s", tableName);
        return executeQuery(sql, stmt -> {
        });
    }

    /**
     * Пошук усіх сутностей з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  ліміт кількості записів
     * @return список знайдених сутностей
     */
    @Override
    public List<T> findAll(int offset, int limit) {
        String sql = String.format("SELECT * FROM %s LIMIT ? OFFSET ?", tableName);
        return executeQuery(sql, stmt -> {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
        });
    }

    /**
     * Підрахунок кількості сутностей, що відповідають фільтру або умовам пошуку.
     *
     * @param filter фільтр для вибірки та пошуку (може бути null)
     * @return кількість сутностей
     */
    @Override
    public long count(Filter filter) {
        return count(filter, tableName);
    }

    /**
     * Перевантажений метод count для роботи з іншою таблицею.
     *
     * @param filter    фільтр для вибірки
     * @param tableName назва таблиці
     * @return кількість записів
     */
    protected long count(Filter filter, String tableName) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add(String.format("SELECT COUNT(*) FROM %s", tableName));
        List<Object> parameters = new ArrayList<>();

        if (filter != null) {
            StringJoiner whereClause = new StringJoiner(" AND ", " WHERE ", "");
            filter.apply(whereClause, parameters);
            sql.add(whereClause.toString());
        }

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка підрахунку записів у таблиці " + tableName, e);
        }
    }

    /**
     * Підрахунок усіх сутностей.
     *
     * @return кількість усіх сутностей
     */
    @Override
    public long count() {
        String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка підрахунку записів", e);
        }
    }

    /**
     * Групування сутностей за агрегаційною функцією.
     *
     * @param aggregation  агрегаційна функція
     * @param resultMapper функція для зіставлення результатів
     * @param <R>          тип результату
     * @return список результатів групування
     */
    @Override
    public <R> List<R> groupBy(Aggregation aggregation, Function<ResultSet, R> resultMapper) {
        StringJoiner selectClause = new StringJoiner(", ", "SELECT ", "");
        StringJoiner groupByClause = new StringJoiner(", ", " GROUP BY ", "");
        aggregation.apply(selectClause, groupByClause);
        String sql = String.format("%s FROM %s%s", selectClause, tableName, groupByClause);

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<R> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(resultMapper.apply(resultSet));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка групування сутностей", e);
        }
    }

    /**
     * Збереження нової сутності.
     *
     * @param entity сутність для збереження
     * @return збережена сутність
     */
    @Override
    public T save(T entity) {
        String sql = buildInsertSql(entity);
        List<Object> values = extractEntityValues(entity);
        executeUpdate(sql, values);
        return entity;
    }

    /**
     * Збереження кількох сутностей у пакетному режимі.
     *
     * @param entities список сутностей для збереження
     * @return список збережених сутностей
     */
    @Override
    public List<T> saveAll(List<T> entities) {
        if (entities.isEmpty()) {
            return entities;
        }

        String sql = buildInsertSql(entities.getFirst());
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (T entity : entities) {
                List<Object> values = extractEntityValues(entity);
                setParameters(statement, values);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка пакетного збереження сутностей", e);
        }

        return entities;
    }

    /**
     * Оновлення сутності.
     *
     * @param id     ідентифікатор сутності
     * @param entity сутність з новими даними
     * @return оновлена сутність
     */
    @Override
    public T update(ID id, T entity) {
        String sql = buildUpdateSql();
        List<Object> values = extractEntityValues(entity, false);
        values.add(id); // Add id for WHERE clause
        executeUpdate(sql, values);
        return entity;
    }

    /**
     * Оновлення кількох сутностей у пакетному режимі.
     *
     * @param entities мапа ідентифікаторів та відповідних сутностей
     * @return мапа оновлених сутностей
     */
    @Override
    public Map<ID, T> updateAll(Map<ID, T> entities) {
        if (entities.isEmpty()) {
            return entities;
        }

        String sql = buildUpdateSql();
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<ID, T> entry : entities.entrySet()) {
                List<Object> values = extractEntityValues(entry.getValue());
                values.add(entry.getKey());
                setParameters(statement, values);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка пакетного оновлення сутностей", e);
        }

        return entities;
    }

    /**
     * Видалення сутності за ідентифікатором.
     *
     * @param id ідентифікатор сутності
     */
    @Override
    public void delete(ID id) {
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        executeUpdate(sql, List.of(id));
    }

    /**
     * Видалення кількох сутностей за ідентифікаторами у пакетному режимі.
     *
     * @param ids список ідентифікаторів
     */
    @Override
    public void deleteAll(List<ID> ids) {
        if (ids.isEmpty()) {
            return;
        }

        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ID id : ids) {
                statement.setObject(1, id);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка пакетного видалення сутностей", e);
        }
    }

    /**
     * Виконує SQL-запит і повертає список сутностей.
     *
     * @param sql             SQL-запит
     * @param parameterSetter функція для встановлення параметрів
     * @return список сутностей
     */
    protected List<T> executeQuery(String sql, ParameterSetter parameterSetter) {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            parameterSetter.setParameters(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> entities = new ArrayList<>();
                while (resultSet.next()) {
                    entities.add(mapResultSetToEntity(resultSet));
                }
                return entities;
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка виконання запиту: " + sql, e);
        }
    }

    /**
     * Виконує SQL-запит і повертає список об'єктів із зіставленням результатів.
     *
     * @param sql             SQL-запит
     * @param parameterSetter функція для встановлення параметрів
     * @param mapper          функція для зіставлення ResultSet
     * @param <R>             тип результату
     * @return список об'єктів
     */
    protected <R> List<R> executeQuery(String sql, ParameterSetter parameterSetter, RowMapper<R> mapper) {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            parameterSetter.setParameters(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<R> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapper.map(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка виконання запиту: " + sql, e);
        }
    }

    /**
     * Виконує SQL-запит для оновлення або вставки.
     *
     * @param sql        SQL-запит
     * @param parameters параметри запиту
     */
    protected void executeUpdate(String sql, List<Object> parameters) {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, parameters);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка виконання оновлення: " + sql, e);
        }
    }

    /**
     * Встановлює параметри для PreparedStatement.
     *
     * @param statement  підготовлений запит
     * @param parameters список параметрів
     * @throws SQLException якщо виникає помилка при встановленні параметрів
     */
    protected void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
    }

    /**
     * Будує SQL-запит для вставки сутності.
     *
     * @param entity сутність
     * @return SQL-запит для вставки
     */
    protected String buildInsertSql(T entity) {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        for (Field field : entityClass.getDeclaredFields()) {
            columns.add(camelCaseToSnakeCase(field.getName()));
            placeholders.add("?");
        }
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }

    /**
     * Будує SQL-запит для оновлення сутності.
     *
     * @return SQL-запит для оновлення
     */
    protected String buildUpdateSql() {
        StringJoiner setClause = new StringJoiner(", ");
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getName().equals("id")) continue;
            setClause.add(camelCaseToSnakeCase(field.getName()) + " = ?");
        }
        return String.format("UPDATE %s SET %s WHERE id = ?", tableName, setClause);
    }

    /**
     * Витягує значення полів сутності для SQL-запиту.
     *
     * @param entity    сутність
     * @param includeId чи включати поле id
     * @return список значень полів
     */
    protected List<Object> extractEntityValues(T entity, boolean includeId) {
        List<Object> values = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (!includeId && field.getName().equals("id")) {
                continue; // Skip id field if includeId is false
            }
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    if (field.getType().isEnum()) {
                        // Конвертуємо енам у рядок
                        value = ((Enum<?>) value).name();
                    } else if (field.getType() == LocalDateTime.class) {
                        // Конвертуємо LocalDateTime у Timestamp
                        value = Timestamp.valueOf((LocalDateTime) value);
                    }
                }
                values.add(value);
            } catch (IllegalAccessException e) {
                throw new EntityMappingException("Помилка доступу до поля: " + field.getName(), e);
            }
        }
        return values;
    }

    /**
     * Витягує значення полів сутності для SQL-запиту (включає id).
     *
     * @param entity сутність
     * @return список значень полів
     */
    protected List<Object> extractEntityValues(T entity) {
        return extractEntityValues(entity, true); // Default to including id
    }

    /**
     * Зіставлення ResultSet із сутністю.
     *
     * @param rs результат запиту
     * @return зіставлена сутність
     * @throws SQLException якщо виникає помилка при роботі з ResultSet
     */
    protected T mapResultSetToEntity(ResultSet rs) throws SQLException {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = camelCaseToSnakeCase(field.getName());
                Object value = rs.getObject(columnName);
                if (value != null) {
                    field.set(entity, convertValue(value, field.getType()));
                }
            }
            return entity;
        } catch (Exception e) {
            throw new EntityMappingException("Помилка зіставлення ResultSet із сутністю", e);
        }
    }

    /**
     * Конвертація значення з бази даних у тип поля сутності.
     *
     * @param value      значення з бази даних
     * @param targetType тип поля сутності
     * @return сконвертоване значення
     */
    protected Object convertValue(Object value, Class<?> targetType) {
        if (value == null && !targetType.isPrimitive()) {
            return null;
        }
        return switch (targetType.getName()) {
            case "java.lang.String" -> value.toString();
            case "java.util.UUID" -> value instanceof String ? UUID.fromString((String) value) : value;
            case "java.lang.Integer", "int" ->
                    value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
            case "java.time.LocalDateTime" -> value instanceof Timestamp ? ((Timestamp) value).toLocalDateTime() : null;
            case "com.arakviel.domain.enums.FileFormat" ->
                    value instanceof String ? FileFormat.valueOf((String) value) : value;
            default -> value;
        };
    }

    /**
     * Перетворення camelCase у snake_case.
     *
     * @param input вхідний рядок
     * @return рядок у форматі snake_case
     */
    protected static String camelCaseToSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    /**
     * Перетворення snake_case у camelCase.
     *
     * @param input вхідний рядок
     * @return рядок у форматі camelCase
     */
    protected static String snakeCaseToCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        boolean toUpperCase = false;
        for (char ch : input.toCharArray()) {
            if (ch == '_') {
                toUpperCase = true;
            } else {
                result.append(toUpperCase ? Character.toUpperCase(ch) : ch);
                toUpperCase = false;
            }
        }
        return result.toString();
    }

    /**
     * Витягнення ідентифікатора з сутності через рефлексію.
     *
     * @param entity сутність
     * @return ідентифікатор
     */
    public Object extractId(Object entity) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Не вдалося отримати ідентифікатор для " + entity.getClass().getSimpleName(), e);
        }
    }

    /**
     * Функціональний інтерфейс для встановлення параметрів PreparedStatement.
     */
    @FunctionalInterface
    protected interface ParameterSetter {
        void setParameters(PreparedStatement statement) throws SQLException;
    }
}