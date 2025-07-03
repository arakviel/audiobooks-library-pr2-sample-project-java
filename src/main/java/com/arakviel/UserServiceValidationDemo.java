package com.arakviel;

import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Демонстрація нової системи валідації в UserService.
 */
public class UserServiceValidationDemo {

    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦІЯ ВАЛІДАЦІЇ USERSERVICE ===\n");

        // 1. Валідація створення користувача
        System.out.println("1. Валідація створення користувача:");
        demonstrateUserCreationValidation();

        // 2. Валідація оновлення користувача
        System.out.println("\n2. Валідація оновлення користувача:");
        demonstrateUserUpdateValidation();

        // 3. Валідація зміни пароля
        System.out.println("\n3. Валідація зміни пароля:");
        demonstratePasswordChangeValidation();

        // 4. Валідація автентифікації
        System.out.println("\n4. Валідація автентифікації:");
        demonstrateAuthenticationValidation();

        // 5. Валідація пошуку
        System.out.println("\n5. Валідація пошуку:");
        demonstrateSearchValidation();

        // 6. Комплексна валідація реєстрації
        System.out.println("\n6. Комплексна валідація реєстрації:");
        demonstrateComprehensiveRegistrationValidation();

        System.out.println("\n=== ДЕМОНСТРАЦІЯ ЗАВЕРШЕНА ===");
    }

    private static void demonstrateUserCreationValidation() {
        System.out.println("Спроба створити користувача з множинними помилками:");

        // Користувач з багатьма помилками
        User invalidUser = new User(
                UUID.randomUUID(),
                "ab", // занадто короткий username
                null, // null password
                "not-an-email", // неправильний email
                null
        );

        try {
            validateUserForCreation(invalidUser);
        } catch (MultiFieldValidationException e) {
            System.out.printf("Знайдено %d помилок у %d полях:%n", 
                e.getErrorCount(), e.getFieldsWithErrors().size());
            
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("  %s: %s%n", field, String.join(", ", errors));
            });
        }
    }

    private static void demonstrateUserUpdateValidation() {
        System.out.println("Спроба оновити неіснуючого користувача з дублікатами:");

        try {
            ValidationHelper validator = new ValidationHelper();
            
            // Імітація перевірки існування користувача
            boolean userExists = false;
            if (!userExists) {
                validator.addError("id", "користувач з таким ідентифікатором не існує");
            }
            
            // Імітація перевірки дублікатів
            boolean usernameExists = true;
            boolean emailExists = true;
            
            if (usernameExists) {
                validator.addError("username", "користувач з таким ім'ям уже існує");
            }
            if (emailExists) {
                validator.addError("email", "користувач з таким email уже існує");
            }
            
            validator.throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Помилки оновлення (%d помилок):%n", e.getErrorCount());
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("  %s: %s%n", field, String.join(", ", errors));
            });
        }
    }

    private static void demonstratePasswordChangeValidation() {
        System.out.println("Спроба змінити пароль з помилками:");

        try {
            new ValidationHelper()
                    .validUuid("userId", null) // null userId
                    .notEmpty("oldPassword", (String) "") // порожній старий пароль
                    .notEmpty("newPassword", (String) null) // null новий пароль
                    .addError("userId", "користувач не знайдений") // імітація
                    .addError("oldPassword", "неправильний старий пароль") // імітація
                    .throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Помилки зміни пароля (%d помилок):%n", e.getErrorCount());
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("  %s: %s%n", field, String.join(", ", errors));
            });
        }
    }

    private static void demonstrateAuthenticationValidation() {
        System.out.println("Спроба автентифікації з порожніми даними:");

        try {
            new ValidationHelper()
                    .notEmpty("username", (String) "")
                    .notEmpty("password", (String) null)
                    .throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Помилки автентифікації (%d помилок):%n", e.getErrorCount());
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("  %s: %s%n", field, String.join(", ", errors));
            });
        }
    }

    private static void demonstrateSearchValidation() {
        System.out.println("Спроба пошуку з неправильними параметрами:");

        try {
            // Імітація валідації пошуку
            new ValidationHelper()
                    .notEmpty("username", (String) null) // пошук за username
                    .notEmpty("email", (String) "") // пошук за email
                    .validUuid("userId", null) // пошук за userId
                    .throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Помилки пошуку (%d помилок):%n", e.getErrorCount());
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("  %s: %s%n", field, String.join(", ", errors));
            });
        }
    }

    private static void demonstrateComprehensiveRegistrationValidation() {
        System.out.println("Комплексна валідація реєстрації з бізнес-правилами:");

        try {
            String username = "test";
            String password = "123";
            String email = "test@";
            String confirmPassword = "456";
            int age = 15;
            
            // Імітація перевірок існування
            boolean usernameExists = true;
            boolean emailExists = true;
            
            ValidationHelper validator = new ValidationHelper()
                    // Базова валідація
                    .notEmpty("username", (String) username)
                    .validUsername("username", username)
                    .notEmpty("password", (String) password)
                    .notEmpty("email", (String) email)
                    .validEmail("email", email)
                    .notEmpty("confirmPassword", (String) confirmPassword)
                    
                    // Бізнес-правила
                    .addErrorIf(usernameExists, "username", "вже зайнятий іншим користувачем")
                    .addErrorIf(emailExists, "email", "вже зареєстрований в системі")
                    .addErrorIf(!password.equals(confirmPassword), "confirmPassword", "паролі не співпадають")
                    .addErrorIf(password.length() < 8, "password", "повинен містити принаймні 8 символів")
                    .addErrorIf(password.matches("\\d+"), "password", "не може складатися тільки з цифр")
                    .addErrorIf(!password.matches(".*[A-Z].*"), "password", "повинен містити велику літеру")
                    .addErrorIf(!password.matches(".*[a-z].*"), "password", "повинен містити малу літеру")
                    .addErrorIf(age < 18, "age", "повинен бути не менше 18 років")
                    .addErrorIf(age > 120, "age", "не може бути більше 120 років");
            
            validator.throwIfHasErrors();

        } catch (MultiFieldValidationException e) {
            System.out.printf("Помилки реєстрації (%d помилок у %d полях):%n", 
                e.getErrorCount(), e.getFieldsWithErrors().size());
            
            e.getAllFieldErrors().forEach((field, errors) -> {
                System.out.printf("  %s:%n", field);
                errors.forEach(error -> System.out.printf("    - %s%n", error));
            });
            
            System.out.printf("%nДетальне повідомлення: %s%n", e.getMessage());
        }
    }

    // Допоміжний метод для валідації користувача
    private static void validateUserForCreation(User user) {
        ValidationHelper validator = new ValidationHelper()
                .notNull("user", user);
        
        if (user != null) {
            validator
                    .notEmpty("username", user.getUsername())
                    .validUsername("username", user.getUsername())
                    .notEmpty("passwordHash", user.getPasswordHash())
                    .notEmpty("email", user.getEmail())
                    .validEmail("email", user.getEmail());
            
            // Імітація перевірки дублікатів
            if ("existinguser".equals(user.getUsername())) {
                validator.addError("username", "користувач з таким ім'ям уже існує");
            }
            if ("existing@email.com".equals(user.getEmail())) {
                validator.addError("email", "користувач з таким email уже існує");
            }
        }
        
        validator.throwIfHasErrors();
    }
}
