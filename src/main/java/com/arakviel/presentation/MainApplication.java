package com.arakviel.presentation;

import com.arakviel.config.MainConfig;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import com.arakviel.presentation.controller.LoginController;
import com.arakviel.presentation.util.SceneManager;
import com.arakviel.presentation.util.ServiceProvider;
import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Головний клас JavaFX додатку для системи управління аудіокнигами. Відповідає за ініціалізацію
 * всіх шарів архітектури та запуск UI.
 */
public class MainApplication extends Application {

    private static ApplicationContext springContext;
    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static ApplicationContext getSpringContext() {
        return springContext;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        // Ініціалізація Spring контексту з усіма шарами архітектури
        springContext = new AnnotationConfigApplicationContext(MainConfig.class);
        ServiceProvider.setApplicationContext(springContext);

        System.out.println("✅ Spring контекст ініціалізовано з усіма шарами:");
        System.out.println("   - Infrastructure шар (репозиторії, база даних)");
        System.out.println("   - Application шар (сервіси, бізнес-логіка)");
        System.out.println("   - Presentation шар (JavaFX контролери)");

        // Ініціалізація бази даних
        initializeDatabase();
    }

    /**
     * Ініціалізує базу даних за допомогою PersistenceInitializer.
     */
    private void initializeDatabase() {
        try {
            System.out.println("🔧 Ініціалізація бази даних...");

            PersistenceInitializer persistenceInitializer = springContext.getBean(PersistenceInitializer.class);
            persistenceInitializer.init(); // Виконує DDL та DML скрипти

            System.out.println("✅ База даних успішно ініціалізована!");
            System.out.println("   - DDL скрипти виконано (створення таблиць)");
            System.out.println("   - DML скрипти виконано (початкові дані)");

        } catch (Exception e) {
            System.err.println("❌ Помилка ініціалізації бази даних: " + e.getMessage());
            e.printStackTrace();

            // Можна вирішити, чи зупиняти додаток при помилці БД
            // throw new RuntimeException("Критична помилка ініціалізації БД", e);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Налаштування головного вікна
        stage.setTitle("Бібліотека Аудіокниг");
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        // Встановлення іконки додатку
        try {
            Image icon = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/app-icon.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Не вдалося завантажити іконку додатку: " + e.getMessage());
        }

        // Ініціалізація SceneManager
        SceneManager.init(stage);

        // Показ екрану авторизації
        showLoginScreen();

        stage.show();
    }

    /**
     * Показує екран авторизації.
     */
    private void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(loader.load(), 400, 500);

        // Додавання CSS стилів
        scene.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource("/css/login.css")).toExternalForm());

        LoginController controller = loader.getController();
        controller.setMainApplication(this);

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    /**
     * Показує головне вікно додатку після успішної авторизації.
     */
    public void showMainWindow() {
        try {
            SceneManager.switchToMainWindow();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Помилка при переході до головного вікна: " + e.getMessage());
        }
    }

    /**
     * Повертає до екрану авторизації.
     */
    public void returnToLoginScreen() {
        try {
            showLoginScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Помилка при поверненні до екрану авторизації: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Закриття Spring контексту при завершенні додатку
        if (springContext != null) {
            ((AnnotationConfigApplicationContext) springContext).close();
        }
    }
}
