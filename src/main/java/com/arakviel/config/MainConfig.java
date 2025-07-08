package com.arakviel.config;

import com.arakviel.application.config.ApplicationConfig;
import com.arakviel.infrastructure.InfrastructureConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Головна конфігурація додатку, що об'єднує всі шари архітектури.
 *
 * Включає:
 * - Infrastructure шар (репозиторії, база даних)
 * - Application шар (сервіси, бізнес-логіка)
 * - Presentation шар (JavaFX контролери, UI)
 * - Domain шар (сутності, енуми)
 *
 * Ця конфігурація є центральною точкою для всього додатку
 * і забезпечує правильну ініціалізацію всіх компонентів.
 */
@Configuration
@ComponentScan(basePackages = {
    "com.arakviel.presentation",
    "com.arakviel.application",
    "com.arakviel.infrastructure",
    "com.arakviel.domain"
})
@Import({ApplicationConfig.class, InfrastructureConfig.class})
public class MainConfig {

    /**
     * Головна конфігурація всіх шарів додатку.
     *
     * Особливості:
     * - Централізоване управління залежностями
     * - Автоматичне сканування компонентів у всіх пакетах
     * - Інтеграція з усіма шарами архітектури
     * - Підтримка Dependency Injection
     */

}
