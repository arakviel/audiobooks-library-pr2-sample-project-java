package com.arakviel;

import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.*;
import com.arakviel.domain.enums.FileFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Комплексна демонстрація нової системи валідації з множинними помилками.
 * Показує, як валідація працює в різних сценаріях та сервісах.
 */
public class ComprehensiveValidationDemo {

    public static void main(String[] args) {
        System.out.println("=== КОМПЛЕКСНА ДЕМОНСТРАЦІЯ НОВОЇ СИСТЕМИ ВАЛІДАЦІЇ ===\n");

        // 1. Базові можливості ValidationHelper
        System.out.println("1. Базові можливості ValidationHelper:");
        demonstrateBasicValidation();

        // 2. Валідація доменних об'єктів
        System.out.println("\n2. Валідація доменних об'єктів:");
        demonstrateDomainObjectValidation();

        // 3. Складна бізнес-логіка валідації
        System.out.println("\n3. Складна бізнес-логіка валідації:");
        demonstrateComplexBusinessValidation();

        // 4. Порівняння зі старою системою
        System.out.println("\n4. Порівняння зі старою системою:");
        demonstrateOldVsNewValidation();

        // 5. Обробка помилок
        System.out.println("\n5. Обробка помилок:");
        demonstrateErrorHandling();

        System.out.println("\n=== ДЕМОНСТРАЦІЯ ЗАВЕРШЕНА ===");
    }

    private static void demonstrateBasicValidation() {
        System.out.println("Тестування різних типів валідації:");

        try {
            new ValidationHelper()
                    .notNull("object", null)
                    .notEmpty("string", "")
                    .validEmail("email", "invalid-email")
                    .validUsername("username", "ab") // занадто короткий
                    .positive("number", -5)
                    .range("percentage", 150, 0, 100)
                    .yearRange("year", 1800, 1900, 2025)
                    .throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Знайдено %d помилок у %d полях:%n", 
                e.getErrorCount(), e.getFieldsWithErrors().size());
            
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("  %s: %s%n", field, String.join(", ", errors));
            });
        }
    }

    private static void demonstrateDomainObjectValidation() {
        // Користувач з множинними помилками
        System.out.println("Валідація користувача:");
        User invalidUser = new User(
                UUID.randomUUID(),
                "ab", // занадто короткий
                null, // null password
                "not-email", // неправильний email
                null
        );
        validateUser(invalidUser);

        // Колекція з помилками
        System.out.println("\nВалідація колекції:");
        Collection invalidCollection = new Collection(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "", // порожня назва
                LocalDateTime.now().plusDays(1) // майбутня дата
        );
        validateCollection(invalidCollection);

        // Файл аудіокниги з помилками
        System.out.println("\nВалідація файлу аудіокниги:");
        AudiobookFile invalidFile = new AudiobookFile(
                UUID.randomUUID(),
                null, // null audiobookId
                "", // порожній шлях
                null, // null format
                -100 // від'ємний розмір
        );
        validateAudiobookFile(invalidFile);
    }

    private static void demonstrateComplexBusinessValidation() {
        System.out.println("Складна бізнес-логіка валідації:");

        try {
            // Імітація складної валідації реєстрації користувача
            String username = "test";
            String password = "123";
            String email = "test@test";
            int age = 15;
            
            // Перевірки існування (імітація)
            boolean usernameExists = true;
            boolean emailExists = true;
            
            new ValidationHelper()
                    .notEmpty("username", username)
                    .length("username", username, 3, 20)
                    .validUsername("username", username)
                    .addErrorIf(usernameExists, "username", "вже зайнятий")
                    
                    .notEmpty("password", password)
                    .length("password", password, 8, 50)
                    .addErrorIf(password.matches("\\d+"), "password", "не може складатися тільки з цифр")
                    .addErrorIf(!password.matches(".*[A-Z].*"), "password", "повинен містити велику літеру")
                    
                    .notEmpty("email", email)
                    .validEmail("email", email)
                    .addErrorIf(emailExists, "email", "вже зареєстрований")
                    
                    .range("age", age, 18, 120)
                    .throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Помилки реєстрації (%d помилок):%n", e.getErrorCount());
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("  %s:%n", field);
                errors.forEach(error -> System.out.printf("    - %s%n", error));
            });
        }
    }

    private static void demonstrateOldVsNewValidation() {
        System.out.println("Стара система (по одній помилці):");
        try {
            // Імітація старої системи
            String value = null;
            if (value == null) {
                throw new RuntimeException("Поле не може бути null");
            }
        } catch (Exception e) {
            System.out.printf("  Помилка: %s%n", e.getMessage());
            System.out.println("  (Користувач бачить тільки першу помилку)");
        }

        System.out.println("\nНова система (всі помилки одразу):");
        try {
            new ValidationHelper()
                    .notNull("field1", null)
                    .notEmpty("field2", "")
                    .positive("field3", -1)
                    .throwIfHasErrors();
        } catch (MultiFieldValidationException e) {
            System.out.printf("  Всього помилок: %d%n", e.getErrorCount());
            System.out.println("  (Користувач бачить всі помилки одразу)");
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("    %s: %s%n", field, String.join(", ", errors));
            });
        }
    }

    private static void demonstrateErrorHandling() {
        System.out.println("Різні способи обробки помилок:");

        // 1. Перевірка наявності помилок
        ValidationHelper validator = new ValidationHelper()
                .notNull("field", null)
                .positive("number", -1);

        if (validator.hasErrors()) {
            System.out.println("1. Є помилки валідації:");
            MultiFieldValidationException exception = validator.getValidationException();
            System.out.printf("   Кількість помилок: %d%n", exception.getErrorCount());
            System.out.printf("   Поля з помилками: %s%n", exception.getFieldsWithErrors());
        }

        // 2. Перевірка конкретного поля
        try {
            new ValidationHelper()
                    .notEmpty("username", "")
                    .validEmail("email", "invalid")
                    .throwIfHasErrors();
        } catch (MultiFieldValidationException e) {
            System.out.println("\n2. Перевірка конкретних полів:");
            if (e.hasFieldErrors("username")) {
                System.out.printf("   Username помилки: %s%n", e.getFieldErrors("username"));
            }
            if (e.hasFieldErrors("email")) {
                System.out.printf("   Email помилки: %s%n", e.getFieldErrors("email"));
            }
        }

        // 3. Детальна інформація про виняток
        try {
            new ValidationHelper()
                    .notNull("data", null)
                    .throwIfHasErrors();
        } catch (MultiFieldValidationException e) {
            System.out.println("\n3. Детальна інформація:");
            System.out.printf("   Повідомлення: %s%n", e.getMessage());
            System.out.printf("   toString(): %s%n", e.toString());
        }
    }

    // Допоміжні методи валідації
    private static void validateUser(User user) {
        try {
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
        } catch (MultiFieldValidationException e) {
            System.out.printf("  Помилки користувача (%d): %s%n", 
                e.getErrorCount(), 
                e.getAllFieldErrors().entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                    .reduce((a, b) -> a + "; " + b).orElse(""));
        }
    }

    private static void validateCollection(Collection collection) {
        try {
            ValidationHelper validator = new ValidationHelper()
                    .notNull("collection", collection);
            
            if (collection != null) {
                validator
                        .notEmpty("name", collection.getName())
                        .length("name", collection.getName(), 1, 100)
                        .notInFuture("createdAt", collection.getCreatedAt());
            }
            
            validator.throwIfHasErrors();
        } catch (MultiFieldValidationException e) {
            System.out.printf("  Помилки колекції (%d): %s%n", 
                e.getErrorCount(),
                e.getAllFieldErrors().keySet().toString());
        }
    }

    private static void validateAudiobookFile(AudiobookFile file) {
        try {
            ValidationHelper validator = new ValidationHelper()
                    .notNull("file", file);
            
            if (file != null) {
                validator
                        .validUuid("audiobookId", file.getAudiobookId())
                        .notEmpty("filePath", file.getFilePath())
                        .notNull("format", file.getFormat())
                        .nonNegative("size", file.getSize());
            }
            
            validator.throwIfHasErrors();
        } catch (MultiFieldValidationException e) {
            System.out.printf("  Помилки файлу (%d): %s%n", 
                e.getErrorCount(),
                e.getAllFieldErrors().keySet().toString());
        }
    }
}
