package com.arakviel.presentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Тести для головного додатку JavaFX.
 */
@ExtendWith(ApplicationExtension.class)
class MainApplicationTest extends ApplicationTest {

    @Test
    void testApplicationStartup() {
        // Перевіряємо, що додаток запускається без помилок
        assertNotNull(MainApplication.getSpringContext());
    }

    @Test
    void testPrimaryStage() {
        // Перевіряємо, що головне вікно створюється
        assertNotNull(MainApplication.getPrimaryStage());
    }
}
