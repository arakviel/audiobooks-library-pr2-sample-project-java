package com.arakviel;

import com.arakviel.application.contract.CollectionService;
import com.arakviel.domain.entities.Collection;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Демонстрація функціоналу публічних колекцій.
 */
public class PublicCollectionDemo {

    private final CollectionService collectionService;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;

    public PublicCollectionDemo(CollectionService collectionService,
                               PersistenceInitializer persistenceInitializer,
                               ConnectionPool connectionPool) {
        this.collectionService = collectionService;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
    }

    /**
     * Демонструє роботу з публічними колекціями.
     */
    public void run() {
        System.out.println("=== ДЕМОНСТРАЦІЯ ПУБЛІЧНИХ КОЛЕКЦІЙ ===\n");

        // Ініціалізація бази даних
        persistenceInitializer.init();

        // 1. Створення публічних колекцій
        System.out.println("1. Створення публічних колекцій:");
        createPublicCollections();

        // 2. Пошук публічних колекцій
        System.out.println("\n2. Пошук публічних колекцій:");
        searchPublicCollections();

        // 3. Статистика публічних колекцій
        System.out.println("\n3. Статистика публічних колекцій:");
        showPublicCollectionStatistics();

        // 4. Популярні та нещодавні колекції
        System.out.println("\n4. Популярні та нещодавні публічні колекції:");
        showPopularAndRecentCollections();

        // 5. Перевірка типу колекції
        System.out.println("\n5. Перевірка типу колекції:");
        checkCollectionTypes();

        // Закриття пулу з'єднань
        connectionPool.shutdown();
        System.out.println("\n=== ДЕМОНСТРАЦІЯ ЗАВЕРШЕНА ===");
    }

    private void createPublicCollections() {
        try {
            // Створюємо кілька публічних колекцій
            String[] collectionNames = {
                "Українська класика",
                "Сучасна фантастика",
                "Детективи та трилери",
                "Дитячі казки",
                "Наукова література"
            };

            for (String name : collectionNames) {
                Collection publicCollection = new Collection(
                    UUID.randomUUID(),
                    null, // userId = null для публічної колекції
                    name,
                    LocalDateTime.now()
                );

                Collection created = collectionService.createPublicCollection(publicCollection);
                System.out.printf("✓ Створено публічну колекцію: '%s' (ID: %s)%n", 
                    created.getName(), created.getId());
            }

            // Спроба створити дублікат
            try {
                Collection duplicate = new Collection(
                    UUID.randomUUID(),
                    null,
                    "Українська класика", // Дублікат назви
                    LocalDateTime.now()
                );
                collectionService.createPublicCollection(duplicate);
            } catch (Exception e) {
                System.out.printf("✗ Помилка при створенні дубліката: %s%n", e.getMessage());
            }

        } catch (Exception e) {
            System.err.printf("Помилка при створенні публічних колекцій: %s%n", e.getMessage());
        }
    }

    private void searchPublicCollections() {
        try {
            // Пошук всіх публічних колекцій
            List<Collection> allPublic = collectionService.findPublicCollections(0, 10);
            System.out.printf("Знайдено %d публічних колекцій:%n", allPublic.size());
            allPublic.forEach(collection -> 
                System.out.printf("  - %s (створено: %s)%n", 
                    collection.getName(), collection.getCreatedAt()));

            // Пошук за точною назвою
            List<Collection> byName = collectionService.findPublicCollectionsByName("Сучасна фантастика");
            System.out.printf("%nПошук за назвою 'Сучасна фантастика': %d результатів%n", byName.size());

            // Пошук за частковою назвою
            List<Collection> byPartialName = collectionService.findPublicCollectionsByPartialName("класик");
            System.out.printf("Пошук за частковою назвою 'класик': %d результатів%n", byPartialName.size());
            byPartialName.forEach(collection -> 
                System.out.printf("  - %s%n", collection.getName()));

        } catch (Exception e) {
            System.err.printf("Помилка при пошуку публічних колекцій: %s%n", e.getMessage());
        }
    }

    private void showPublicCollectionStatistics() {
        try {
            // Загальна кількість публічних колекцій
            long totalPublic = collectionService.countPublicCollections();
            System.out.printf("Загальна кількість публічних колекцій: %d%n", totalPublic);

            // Перевірка існування колекцій
            boolean exists1 = collectionService.existsPublicCollectionByName("Українська класика");
            boolean exists2 = collectionService.existsPublicCollectionByName("Неіснуюча колекція");
            
            System.out.printf("Чи існує 'Українська класика': %s%n", exists1 ? "Так" : "Ні");
            System.out.printf("Чи існує 'Неіснуюча колекція': %s%n", exists2 ? "Так" : "Ні");

        } catch (Exception e) {
            System.err.printf("Помилка при отриманні статистики: %s%n", e.getMessage());
        }
    }

    private void showPopularAndRecentCollections() {
        try {
            // Найпопулярніші публічні колекції
            List<Collection> popular = collectionService.findMostPopularPublicCollections(3);
            System.out.printf("Топ-%d найпопулярніших публічних колекцій:%n", popular.size());
            popular.forEach(collection -> 
                System.out.printf("  - %s%n", collection.getName()));

            // Нещодавно створені публічні колекції
            List<Collection> recent = collectionService.findRecentlyCreatedPublicCollections(3);
            System.out.printf("%nТоп-%d нещодавно створених публічних колекцій:%n", recent.size());
            recent.forEach(collection -> 
                System.out.printf("  - %s (створено: %s)%n", 
                    collection.getName(), collection.getCreatedAt()));

        } catch (Exception e) {
            System.err.printf("Помилка при отриманні популярних/нещодавніх колекцій: %s%n", e.getMessage());
        }
    }

    private void checkCollectionTypes() {
        try {
            // Отримуємо кілька колекцій для перевірки
            List<Collection> collections = collectionService.findPublicCollections(0, 3);
            
            for (Collection collection : collections) {
                boolean isPublic = collectionService.isPublicCollection(collection.getId());
                System.out.printf("Колекція '%s': %s%n", 
                    collection.getName(), 
                    isPublic ? "Публічна" : "Приватна");
            }

        } catch (Exception e) {
            System.err.printf("Помилка при перевірці типів колекцій: %s%n", e.getMessage());
        }
    }

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(
            InfrastructureConfig.class,
            ApplicationConfig.class,
            DemoConfig.class
        );
        PublicCollectionDemo demo = context.getBean(PublicCollectionDemo.class);
        demo.run();
    }

    @Configuration
    @ComponentScan("com.arakviel.application")
    static class ApplicationConfig {
        // Конфігурація для сканування сервісів
    }

    @Configuration
    static class DemoConfig {
        @Bean
        public PublicCollectionDemo publicCollectionDemo(
                CollectionService collectionService,
                PersistenceInitializer persistenceInitializer,
                ConnectionPool connectionPool) {
            return new PublicCollectionDemo(collectionService, persistenceInitializer, connectionPool);
        }
    }
}
