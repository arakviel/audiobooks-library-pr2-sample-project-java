package com.arakviel;

import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Демонстрація нової системи валідації з множинними помилками.
 */
public class ValidationDemo {

    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦІЯ НОВОЇ СИСТЕМИ ВАЛІДАЦІЇ ===\n");

        // 1. Демонстрація базової валідації
        System.out.println("1. Базова валідація:");
        demonstrateBasicValidation();

        // 2. Демонстрація множинних помилок
        System.out.println("\n2. Множинні помилки валідації:");
        demonstrateMultipleErrors();

        // 3. Демонстрація валідації користувача
        System.out.println("\n3. Валідація користувача:");
        demonstrateUserValidation();

        // 4. Демонстрація валідації колекції
        System.out.println("\n4. Валідація колекції:");
        demonstrateCollectionValidation();

        // 5. Демонстрація кастомних помилок
        System.out.println("\n5. Кастомні помилки валідації:");
        demonstrateCustomValidation();

        System.out.println("\n=== ДЕМОНСТРАЦІЯ ЗАВЕРШЕНА ===");
    }

    private static void demonstrateBasicValidation() {
        try {
            // Успішна валідація
            new ValidationHelper()
                    .notNull("field1", "value")
                    .notEmpty("field2", "non-empty")
                    .validEmail("email", "test@example.com")
                    .positive("number", 5)
                    .throwIfHasErrors();
            
            System.out.println("✓ Успішна валідація пройшла без помилок");

            // Валідація з помилкою
            new ValidationHelper()
                    .notNull("field", null)
                    .throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("✗ Валідація не пройшла: %s%n", e.getMessage());
        }
    }

    private static void demonstrateMultipleErrors() {
        try {
            new ValidationHelper()
                    .notNull("field1", null)
                    .notEmpty("field2", "")
                    .validEmail("email", "invalid-email")
                    .positive("number", -5)
                    .range("year", 1800, 1900, 2025)
                    .throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Знайдено %d помилок у %d полях:%n", 
                e.getErrorCount(), e.getFieldsWithErrors().size());
            
            Map<String, List<String>> errors = e.getAllFieldErrors();
            errors.forEach((field, fieldErrors) -> {
                System.out.printf("  %s:%n", field);
                fieldErrors.forEach(error -> System.out.printf("    - %s%n", error));
            });
        }
    }

    private static void demonstrateUserValidation() {
        // Створюємо користувача з множинними помилками
        User invalidUser = new User(
                UUID.randomUUID(),
                "ab", // занадто короткий username
                null, // null password
                "not-an-email", // неправильний email
                null
        );

        try {
            validateUser(invalidUser);
        } catch (MultiFieldValidationException e) {
            System.out.printf("Помилки валідації користувача (%d помилок):%n", e.getErrorCount());
            
            Map<String, List<String>> errors = e.getAllFieldErrors();
            errors.forEach((field, fieldErrors) -> {
                System.out.printf("  %s: %s%n", field, String.join(", ", fieldErrors));
            });
        }
    }

    private static void demonstrateCollectionValidation() {
        // Створюємо колекцію з помилками
        Collection invalidCollection = new Collection(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "", // порожня назва
                LocalDateTime.now().plusDays(1) // дата в майбутньому
        );

        try {
            validateCollection(invalidCollection);
        } catch (MultiFieldValidationException e) {
            System.out.printf("Помилки валідації колекції (%d помилок):%n", e.getErrorCount());
            
            e.getAllFieldErrors().forEach((field, fieldErrors) -> {
                System.out.printf("  %s: %s%n", field, String.join(", ", fieldErrors));
            });
        }
    }

    private static void demonstrateCustomValidation() {
        try {
            ValidationHelper validator = new ValidationHelper();
            
            // Кастомна бізнес-логіка
            String password = "123";
            boolean isWeakPassword = password.length() < 8;
            boolean containsOnlyNumbers = password.matches("\\d+");
            
            validator
                    .notEmpty("password", password)
                    .addErrorIf(isWeakPassword, "password", "повинен містити принаймні 8 символів")
                    .addErrorIf(containsOnlyNumbers, "password", "не може складатися тільки з цифр");

            // Перевірка віку
            int age = 15;
            validator.addErrorIf(age < 18, "age", "повинен бути не менше 18 років");

            // Перевірка унікальності (імітація)
            boolean usernameExists = true;
            validator.addErrorIf(usernameExists, "username", "вже зайнятий іншим користувачем");

            validator.throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Кастомні помилки валідації (%d помилок):%n", e.getErrorCount());
            
            e.getAllFieldErrors().forEach((field, fieldErrors) -> {
                System.out.printf("  %s:%n", field);
                fieldErrors.forEach(error -> System.out.printf("    - %s%n", error));
            });
        }
    }

    // Приклад валідації користувача
    private static void validateUser(User user) {
        ValidationHelper validator = new ValidationHelper()
                .notNull("user", user);
        
        if (user != null) {
            validator
                    .notEmpty("username", user.getUsername())
                    .validUsername("username", user.getUsername())
                    .notEmpty("passwordHash", user.getPasswordHash())
                    .notEmpty("email", user.getEmail())
                    .validEmail("email", user.getEmail());
        }
        
        validator.throwIfHasErrors();
    }

    // Приклад валідації колекції
    private static void validateCollection(Collection collection) {
        ValidationHelper validator = new ValidationHelper()
                .notNull("collection", collection);
        
        if (collection != null) {
            validator
                    .notEmpty("name", collection.getName())
                    .length("name", collection.getName(), 1, 100)
                    .notInFuture("createdAt", collection.getCreatedAt());
        }
        
        validator.throwIfHasErrors();
    }
}
