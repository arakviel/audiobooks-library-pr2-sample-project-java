package com.arakviel;

import com.arakviel.domain.entities.Author;
import com.arakviel.infrastructure.InfrastructureConfig;
import com.arakviel.infrastructure.persistence.contract.AuthorRepository;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * TODO: delete database by rerun
 * Основний клас додатку для демонстрації вибірки авторів із бази даних.
 */
public class Application {

    private final AuthorRepository authorRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;

    public Application(AuthorRepository authorRepository, PersistenceInitializer persistenceInitializer, ConnectionPool connectionPool) {
        this.authorRepository = authorRepository;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
    }

    /**
     * Виконує ініціалізацію бази даних і виводить усіх авторів у консоль.
     */
    public void run() {
        // Ініціалізація бази даних
        persistenceInitializer.init();

        // Вибірка всіх авторів
        List<Author> authors = authorRepository.findAll();
        System.out.println("Знайдені автори:");
        authors.forEach(author ->
                System.out.printf("ID: %s, Ім'я: %s %s%n",
                        author.getId(),
                        author.getFirstName(),
                        author.getLastName())
        );

        // Закриття пулу з'єднань
        connectionPool.shutdown();
    }

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(InfrastructureConfig.class, AppConfig.class);
        Application app = context.getBean(Application.class);
        app.run();
    }

    @Configuration
    static class AppConfig {
        @Bean
        public Application application(AuthorRepository authorRepository, PersistenceInitializer persistenceInitializer, ConnectionPool connectionPool) {
            return new Application(authorRepository, persistenceInitializer, connectionPool);
        }
    }
}