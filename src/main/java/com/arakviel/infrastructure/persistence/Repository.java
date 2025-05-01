package com.arakviel.infrastructure.persistence;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * Інтерфейс для загальних операцій з репозиторієм.
 *
 * @param <T>  тип сутності
 * @param <ID> тип ідентифікатора сутності
 */
public interface Repository<T, ID> {

    /**
     * Функціональний інтерфейс для визначення умов фільтрації та пошуку.
     */
    @FunctionalInterface
    interface Filter {
        void apply(StringJoiner whereClause, List<Object> parameters);
    }

    /**
     * Функціональний інтерфейс для визначення агрегаційних функцій.
     */
    @FunctionalInterface
    interface Aggregation {
        void apply(StringJoiner selectClause, StringJoiner groupByClause);
    }

    /**
     * Функціональний інтерфейс для зіставлення ResultSet у об'єкти.
     *
     * @param <R> тип результату
     */
    @FunctionalInterface
    interface RowMapper<R> {
        R map(ResultSet rs);
    }

    /**
     * Пошук сутності за ідентифікатором.
     *
     * @param id ідентифікатор сутності
     * @return Optional із знайденою сутністю або порожній, якщо не знайдено
     */
    Optional<T> findById(ID id);

    /**
     * Пошук сутностей за значенням поля.
     *
     * @param fieldName назва поля
     * @param value     значення поля
     * @return список знайдених сутностей
     */
    List<T> findByField(String fieldName, Object value);

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
    List<T> findAll(Filter filter, String sortBy, boolean isAscending, int offset, int limit, String baseSql);

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
    List<T> findAll(Filter filter, String sortBy, boolean isAscending, int offset, int limit);

    /**
     * Пошук усіх сутностей з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit  ліміт кількості записів
     * @return список знайдених сутностей
     */
    List<T> findAll(int offset, int limit);

    /**
     * Пошук усіх сутностей без фільтрації, пошуку та сортування.
     *
     * @return список усіх сутностей
     */
    List<T> findAll();

    /**
     * Підрахунок кількості сутностей, що відповідають фільтру або умовам пошуку.
     *
     * @param filter фільтр для вибірки та пошуку (може бути null)
     * @return кількість сутностей
     */
    long count(Filter filter);

    /**
     * Підрахунок усіх сутностей.
     *
     * @return кількість усіх сутностей
     */
    long count();

    /**
     * Групування сутностей за агрегаційною функцією.
     *
     * @param aggregation  агрегаційна функція
     * @param resultMapper функція для зіставлення результатів
     * @param <R>          тип результату
     * @return список результатів групування
     */
    <R> List<R> groupBy(Aggregation aggregation, Function<ResultSet, R> resultMapper);

    /**
     * Збереження нової сутності.
     *
     * @param entity сутність для збереження
     * @return збережена сутність
     */
    T save(T entity);

    /**
     * Збереження кількох сутностей у пакетному режимі.
     *
     * @param entities список сутностей для збереження
     * @return список збережених сутностей
     */
    List<T> saveAll(List<T> entities);

    /**
     * Оновлення сутності.
     *
     * @param id     ідентифікатор сутності
     * @param entity сутність з новими даними
     * @return оновлена сутність
     */
    T update(ID id, T entity);

    /**
     * Оновлення кількох сутностей у пакетному режимі.
     *
     * @param entities мапа ідентифікаторів та відповідних сутностей
     * @return мапа оновлених сутностей
     */
    Map<ID, T> updateAll(Map<ID, T> entities);

    /**
     * Видалення сутності за ідентифікатором.
     *
     * @param id ідентифікатор сутності
     */
    void delete(ID id);

    /**
     * Видалення кількох сутностей за ідентифікаторами у пакетному режимі.
     *
     * @param ids список ідентифікаторів
     */
    void deleteAll(List<ID> ids);
}