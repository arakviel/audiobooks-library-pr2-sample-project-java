package com.arakviel.application.validation;

import com.arakviel.application.exception.MultiFieldValidationException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Клас-помічник для валідації з підтримкою збору множинних помилок.
 */
public class ValidationHelper {

    private final MultiFieldValidationException validationException;

    // Регулярні вирази для валідації
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    /**
     * Конструктор створює новий екземпляр з порожнім списком помилок.
     */
    public ValidationHelper() {
        this.validationException = new MultiFieldValidationException();
    }

    /**
     * Перевіряє, що значення не null.
     *
     * @param fieldName назва поля
     * @param value     значення для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper notNull(String fieldName, Object value) {
        if (value == null) {
            validationException.addFieldError(fieldName, "не може бути null");
        }
        return this;
    }

    /**
     * Перевіряє, що рядок не null і не порожній.
     *
     * @param fieldName назва поля
     * @param value     рядок для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper notEmpty(String fieldName, String value) {
        if (value == null) {
            validationException.addFieldError(fieldName, "не може бути null");
        } else if (value.trim().isEmpty()) {
            validationException.addFieldError(fieldName, "не може бути порожнім");
        }
        return this;
    }

    /**
     * Перевіряє, що рядок не null і не порожній, навіть якщо передано null.
     * Цей метод завжди додає помилку для порожніх рядків.
     *
     * @param fieldName назва поля
     * @param value     рядок для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper notNullOrEmpty(String fieldName, String value) {
        if (value == null) {
            validationException.addFieldError(fieldName, "не може бути null");
        } else if (value.trim().isEmpty()) {
            validationException.addFieldError(fieldName, "не може бути порожнім");
        }
        return this;
    }

    /**
     * Перевіряє довжину рядка.
     *
     * @param fieldName назва поля
     * @param value     рядок для перевірки
     * @param minLength мінімальна довжина
     * @param maxLength максимальна довжина
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper length(String fieldName, String value, int minLength, int maxLength) {
        if (value != null) {
            int length = value.trim().length();
            if (length < minLength) {
                validationException.addFieldError(fieldName, 
                    String.format("повинно містити принаймні %d символів", minLength));
            }
            if (length > maxLength) {
                validationException.addFieldError(fieldName, 
                    String.format("не може містити більше %d символів", maxLength));
            }
        }
        return this;
    }

    /**
     * Перевіряє, що колекція не null і не порожня.
     *
     * @param fieldName  назва поля
     * @param collection колекція для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper notEmpty(String fieldName, Collection<?> collection) {
        if (collection == null) {
            validationException.addFieldError(fieldName, "не може бути null");
        } else if (collection.isEmpty()) {
            validationException.addFieldError(fieldName, "не може бути порожньою");
        }
        return this;
    }

    /**
     * Перевіряє формат email.
     *
     * @param fieldName назва поля
     * @param email     email для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper validEmail(String fieldName, String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            validationException.addFieldError(fieldName, "має неправильний формат email");
        }
        return this;
    }

    /**
     * Перевіряє формат username.
     *
     * @param fieldName назва поля
     * @param username  username для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper validUsername(String fieldName, String username) {
        if (username != null && !USERNAME_PATTERN.matcher(username).matches()) {
            validationException.addFieldError(fieldName, 
                "може містити тільки літери, цифри та підкреслення (3-20 символів)");
        }
        return this;
    }

    /**
     * Перевіряє діапазон чисел.
     *
     * @param fieldName назва поля
     * @param value     значення для перевірки
     * @param min       мінімальне значення (включно)
     * @param max       максимальне значення (включно)
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper range(String fieldName, Number value, Number min, Number max) {
        if (value != null) {
            double val = value.doubleValue();
            double minVal = min.doubleValue();
            double maxVal = max.doubleValue();
            
            if (val < minVal) {
                validationException.addFieldError(fieldName, 
                    String.format("повинно бути не менше %s", min));
            }
            if (val > maxVal) {
                validationException.addFieldError(fieldName, 
                    String.format("повинно бути не більше %s", max));
            }
        }
        return this;
    }

    /**
     * Перевіряє, що значення більше нуля.
     *
     * @param fieldName назва поля
     * @param value     значення для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper positive(String fieldName, Number value) {
        if (value != null && value.doubleValue() <= 0) {
            validationException.addFieldError(fieldName, "повинно бути більше нуля");
        }
        return this;
    }

    /**
     * Перевіряє, що значення не від'ємне.
     *
     * @param fieldName назва поля
     * @param value     значення для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper nonNegative(String fieldName, Number value) {
        if (value != null && value.doubleValue() < 0) {
            validationException.addFieldError(fieldName, "не може бути від'ємним");
        }
        return this;
    }

    /**
     * Перевіряє діапазон років.
     *
     * @param fieldName назва поля
     * @param year      рік для перевірки
     * @param minYear   мінімальний рік
     * @param maxYear   максимальний рік
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper yearRange(String fieldName, Integer year, int minYear, int maxYear) {
        if (year != null) {
            if (year < minYear) {
                validationException.addFieldError(fieldName, 
                    String.format("не може бути раніше %d року", minYear));
            }
            if (year > maxYear) {
                validationException.addFieldError(fieldName, 
                    String.format("не може бути пізніше %d року", maxYear));
            }
        }
        return this;
    }

    /**
     * Перевіряє, що дата не в майбутньому.
     *
     * @param fieldName назва поля
     * @param dateTime  дата для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper notInFuture(String fieldName, LocalDateTime dateTime) {
        if (dateTime != null && dateTime.isAfter(LocalDateTime.now())) {
            validationException.addFieldError(fieldName, "не може бути в майбутньому");
        }
        return this;
    }

    /**
     * Перевіряє, що UUID не null і має правильний формат.
     *
     * @param fieldName назва поля
     * @param uuid      UUID для перевірки
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper validUuid(String fieldName, UUID uuid) {
        if (uuid == null) {
            validationException.addFieldError(fieldName, "не може бути null");
        }
        return this;
    }

    /**
     * Додає кастомну помилку валідації.
     *
     * @param fieldName    назва поля
     * @param errorMessage повідомлення про помилку
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper addError(String fieldName, String errorMessage) {
        validationException.addFieldError(fieldName, errorMessage);
        return this;
    }

    /**
     * Додає кастомну помилку валідації за умовою.
     *
     * @param condition    умова для додавання помилки
     * @param fieldName    назва поля
     * @param errorMessage повідомлення про помилку
     * @return this для ланцюжкових викликів
     */
    public ValidationHelper addErrorIf(boolean condition, String fieldName, String errorMessage) {
        if (condition) {
            validationException.addFieldError(fieldName, errorMessage);
        }
        return this;
    }

    /**
     * Перевіряє, чи є помилки валідації.
     *
     * @return true, якщо є помилки
     */
    public boolean hasErrors() {
        return validationException.hasErrors();
    }

    /**
     * Повертає виняток з усіма зібраними помилками.
     *
     * @return виняток валідації
     */
    public MultiFieldValidationException getValidationException() {
        return validationException;
    }

    /**
     * Викидає виняток, якщо є помилки валідації.
     *
     * @throws MultiFieldValidationException якщо є помилки валідації
     */
    public void throwIfHasErrors() {
        validationException.throwIfHasErrors();
    }
}
