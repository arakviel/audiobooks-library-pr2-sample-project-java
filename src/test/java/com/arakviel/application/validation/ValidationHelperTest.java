package com.arakviel.application.validation;

import com.arakviel.application.exception.MultiFieldValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тести для ValidationHelper та MultiFieldValidationException.
 */
class ValidationHelperTest {

    @Test
    void shouldPassValidationWhenAllFieldsAreValid() {
        // Arrange & Act & Assert - не повинно викидати виняток
        new ValidationHelper()
                .notNull("field1", "value")
                .notEmpty("field2", "non-empty")
                .length("field3", "test", 1, 10)
                .validEmail("email", "test@example.com")
                .validUsername("username", "testuser123")
                .positive("number", 5)
                .throwIfHasErrors();
    }

    @Test
    void shouldCollectMultipleValidationErrors() {
        // Arrange
        ValidationHelper validator = new ValidationHelper();

        // Act
        validator
                .notNull("field1", null)
                .notEmpty("field2", "")
                .length("field3", "ab", 5, 10)
                .validEmail("email", "invalid-email")
                .positive("number", -5);

        // Assert
        assertThat(validator.hasErrors()).isTrue();
        
        MultiFieldValidationException exception = validator.getValidationException();
        Map<String, List<String>> errors = exception.getAllFieldErrors();
        
        assertThat(errors).hasSize(5);
        assertThat(errors.get("field1")).contains("не може бути null");
        assertThat(errors.get("field2")).contains("не може бути порожнім");
        assertThat(errors.get("field3")).contains("повинно містити принаймні 5 символів");
        assertThat(errors.get("email")).contains("має неправильний формат email");
        assertThat(errors.get("number")).contains("повинно бути більше нуля");
    }

    @Test
    void shouldThrowExceptionWithAllErrors() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> {
            new ValidationHelper()
                    .notNull("field1", null)
                    .notEmpty("field2", "")
                    .positive("number", -1)
                    .throwIfHasErrors();
        })
        .isInstanceOf(MultiFieldValidationException.class)
        .satisfies(exception -> {
            MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
            assertThat(validationException.getErrorCount()).isEqualTo(3);
            assertThat(validationException.getFieldsWithErrors()).containsExactlyInAnyOrder("field1", "field2", "number");
        });
    }

    @Test
    void shouldValidateEmailFormats() {
        // Valid emails
        new ValidationHelper()
                .validEmail("email1", "test@example.com")
                .validEmail("email2", "user.name+tag@domain.co.uk")
                .validEmail("email3", "user123@test-domain.org")
                .throwIfHasErrors();

        // Invalid emails
        ValidationHelper validator = new ValidationHelper();
        validator
                .validEmail("email1", "invalid")
                .validEmail("email2", "@domain.com")
                .validEmail("email3", "user@")
                .validEmail("email4", "user@domain");

        assertThat(validator.hasErrors()).isTrue();
        assertThat(validator.getValidationException().getErrorCount()).isEqualTo(4);
    }

    @Test
    void shouldValidateUsernameFormats() {
        // Valid usernames
        new ValidationHelper()
                .validUsername("username1", "user123")
                .validUsername("username2", "test_user")
                .validUsername("username3", "User_Name_123")
                .throwIfHasErrors();

        // Invalid usernames
        ValidationHelper validator = new ValidationHelper();
        validator
                .validUsername("username1", "ab") // too short
                .validUsername("username2", "user-name") // contains dash
                .validUsername("username3", "user name") // contains space
                .validUsername("username4", "a".repeat(25)); // too long

        assertThat(validator.hasErrors()).isTrue();
        assertThat(validator.getValidationException().getErrorCount()).isEqualTo(4);
    }

    @Test
    void shouldValidateRanges() {
        // Valid ranges
        new ValidationHelper()
                .range("number1", 5, 1, 10)
                .range("number2", 1.5, 1.0, 2.0)
                .range("number3", 100L, 50L, 150L)
                .throwIfHasErrors();

        // Invalid ranges
        ValidationHelper validator = new ValidationHelper();
        validator
                .range("number1", 0, 1, 10) // below min
                .range("number2", 15, 1, 10); // above max

        assertThat(validator.hasErrors()).isTrue();
        assertThat(validator.getValidationException().getErrorCount()).isEqualTo(2);
    }

    @Test
    void shouldValidateYearRanges() {
        int currentYear = LocalDateTime.now().getYear();
        
        // Valid years
        new ValidationHelper()
                .yearRange("year1", 2000, 1900, currentYear + 1)
                .yearRange("year2", currentYear, 1900, currentYear + 1)
                .throwIfHasErrors();

        // Invalid years
        ValidationHelper validator = new ValidationHelper();
        validator
                .yearRange("year1", 1800, 1900, currentYear + 1) // too early
                .yearRange("year2", currentYear + 5, 1900, currentYear + 1); // too late

        assertThat(validator.hasErrors()).isTrue();
        assertThat(validator.getValidationException().getErrorCount()).isEqualTo(2);
    }

    @Test
    void shouldValidateDateNotInFuture() {
        LocalDateTime now = LocalDateTime.now();
        
        // Valid dates
        new ValidationHelper()
                .notInFuture("date1", now.minusDays(1))
                .notInFuture("date2", now)
                .throwIfHasErrors();

        // Invalid date
        ValidationHelper validator = new ValidationHelper();
        validator.notInFuture("date", now.plusDays(1));

        assertThat(validator.hasErrors()).isTrue();
        assertThat(validator.getValidationException().getFieldErrors("date"))
                .contains("не може бути в майбутньому");
    }

    @Test
    void shouldValidateUUIDs() {
        UUID validUuid = UUID.randomUUID();
        
        // Valid UUID
        new ValidationHelper()
                .validUuid("uuid", validUuid)
                .throwIfHasErrors();

        // Invalid UUID
        ValidationHelper validator = new ValidationHelper();
        validator.validUuid("uuid", null);

        assertThat(validator.hasErrors()).isTrue();
        assertThat(validator.getValidationException().getFieldErrors("uuid"))
                .contains("не може бути null");
    }

    @Test
    void shouldSupportCustomErrors() {
        ValidationHelper validator = new ValidationHelper();
        
        validator
                .addError("custom1", "Custom error message")
                .addErrorIf(true, "custom2", "Conditional error")
                .addErrorIf(false, "custom3", "Should not appear");

        assertThat(validator.hasErrors()).isTrue();
        Map<String, List<String>> errors = validator.getValidationException().getAllFieldErrors();
        
        assertThat(errors).hasSize(2);
        assertThat(errors.get("custom1")).contains("Custom error message");
        assertThat(errors.get("custom2")).contains("Conditional error");
        assertThat(errors).doesNotContainKey("custom3");
    }

    @Test
    void shouldSupportLengthValidation() {
        // Valid lengths
        new ValidationHelper()
                .length("field1", "test", 1, 10)
                .length("field2", "exact", 5, 5)
                .throwIfHasErrors();

        // Invalid lengths
        assertThatThrownBy(() -> {
            new ValidationHelper()
                    .length("field1", "ab", 5, 10) // too short
                    .length("field2", "this is way too long", 1, 10) // too long
                    .throwIfHasErrors();
        })
        .isInstanceOf(MultiFieldValidationException.class)
        .satisfies(exception -> {
            MultiFieldValidationException validationException = (MultiFieldValidationException) exception;
            assertThat(validationException.getErrorCount()).isEqualTo(2);
        });
    }

    @Test
    void shouldHandleNullValuesGracefully() {
        // Null values should not cause additional errors for optional validations
        new ValidationHelper()
                .validEmail("email", null) // should not add error
                .validUsername("username", null) // should not add error
                .range("number", null, 1, 10) // should not add error
                .throwIfHasErrors(); // should not throw
    }

    @Test
    void shouldProvideDetailedExceptionMessage() {
        // Act & Assert
        assertThatThrownBy(() -> {
            new ValidationHelper()
                    .notNull("field1", null)
                    .notEmpty("field2", "")
                    .throwIfHasErrors();
        })
        .isInstanceOf(MultiFieldValidationException.class)
        .satisfies(exception -> {
            String message = exception.getMessage();
            assertThat(message).contains("field1");
            assertThat(message).contains("field2");
            assertThat(message).contains("не може бути null");
            assertThat(message).contains("не може бути порожнім");
        });
    }
}
