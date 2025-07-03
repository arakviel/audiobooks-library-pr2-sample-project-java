package com.arakviel.application.exception;

import java.util.*;

/**
 * Виняток для валідації з підтримкою множинних помилок по полях.
 * Зберігає словник, де ключ - назва поля, значення - список помилок для цього поля.
 */
public class MultiFieldValidationException extends RuntimeException {

    private final Map<String, List<String>> fieldErrors;

    /**
     * Конструктор з порожнім словником помилок.
     */
    public MultiFieldValidationException() {
        super("Validation failed for multiple fields");
        this.fieldErrors = new HashMap<>();
    }

    /**
     * Конструктор з готовим словником помилок.
     *
     * @param fieldErrors словник помилок по полях
     */
    public MultiFieldValidationException(Map<String, List<String>> fieldErrors) {
        super(buildMessage(fieldErrors));
        this.fieldErrors = new HashMap<>(fieldErrors);
    }

    /**
     * Конструктор з повідомленням та словником помилок.
     *
     * @param message     загальне повідомлення про помилку
     * @param fieldErrors словник помилок по полях
     */
    public MultiFieldValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message);
        this.fieldErrors = new HashMap<>(fieldErrors);
    }

    /**
     * Додає помилку для конкретного поля.
     *
     * @param fieldName    назва поля
     * @param errorMessage повідомлення про помилку
     */
    public void addFieldError(String fieldName, String errorMessage) {
        fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
    }

    /**
     * Додає кілька помилок для конкретного поля.
     *
     * @param fieldName     назва поля
     * @param errorMessages список повідомлень про помилки
     */
    public void addFieldErrors(String fieldName, List<String> errorMessages) {
        fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).addAll(errorMessages);
    }

    /**
     * Перевіряє, чи є помилки для конкретного поля.
     *
     * @param fieldName назва поля
     * @return true, якщо є помилки для цього поля
     */
    public boolean hasFieldErrors(String fieldName) {
        return fieldErrors.containsKey(fieldName) && !fieldErrors.get(fieldName).isEmpty();
    }

    /**
     * Повертає список помилок для конкретного поля.
     *
     * @param fieldName назва поля
     * @return список помилок або порожній список, якщо помилок немає
     */
    public List<String> getFieldErrors(String fieldName) {
        return fieldErrors.getOrDefault(fieldName, Collections.emptyList());
    }

    /**
     * Повертає всі помилки по полях.
     *
     * @return незмінний словник помилок
     */
    public Map<String, List<String>> getAllFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    /**
     * Перевіряє, чи є взагалі помилки.
     *
     * @return true, якщо є хоча б одна помилка
     */
    public boolean hasErrors() {
        return !fieldErrors.isEmpty() && fieldErrors.values().stream().anyMatch(list -> !list.isEmpty());
    }

    /**
     * Повертає загальну кількість помилок.
     *
     * @return кількість помилок
     */
    public int getErrorCount() {
        return fieldErrors.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Повертає список всіх полів з помилками.
     *
     * @return набір назв полів з помилками
     */
    public Set<String> getFieldsWithErrors() {
        return Collections.unmodifiableSet(fieldErrors.keySet());
    }

    /**
     * Очищає всі помилки.
     */
    public void clearErrors() {
        fieldErrors.clear();
    }

    /**
     * Викидає виняток, якщо є помилки.
     *
     * @throws MultiFieldValidationException якщо є помилки валідації
     */
    public void throwIfHasErrors() {
        if (hasErrors()) {
            throw new MultiFieldValidationException(buildMessage(this.fieldErrors), this.fieldErrors);
        }
    }

    /**
     * Створює повідомлення на основі словника помилок.
     *
     * @param fieldErrors словник помилок
     * @return форматоване повідомлення
     */
    private static String buildMessage(Map<String, List<String>> fieldErrors) {
        if (fieldErrors.isEmpty()) {
            return "Validation failed";
        }

        StringBuilder message = new StringBuilder("Validation failed for fields: ");
        fieldErrors.forEach((field, errors) -> {
            message.append(field).append(" (").append(String.join(", ", errors)).append("); ");
        });

        return message.toString();
    }

    @Override
    public String toString() {
        return "MultiFieldValidationException{" +
                "fieldErrors=" + fieldErrors +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
