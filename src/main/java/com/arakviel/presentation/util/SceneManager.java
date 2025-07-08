package com.arakviel.presentation.util;

import com.arakviel.presentation.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Утилітний клас для управління сценами та переходами між ними.
 */
public class SceneManager {
    
    private static Stage primaryStage;
    private static Scene mainScene;
    private static MainController mainController;

    /**
     * Ініціалізує SceneManager з головним Stage.
     */
    public static void init(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Переключає на головне вікно додатку.
     */
    public static void switchToMainWindow() throws IOException {
        if (mainScene == null) {
            loadMainScene();
        }
        
        primaryStage.setScene(mainScene);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.centerOnScreen();
    }

    /**
     * Завантажує головну сцену додатку.
     */
    private static void loadMainScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/view/main.fxml"));
        mainScene = new Scene(loader.load(), 1200, 800);
        
        // Додавання CSS стилів
        mainScene.getStylesheets().add(Objects.requireNonNull(
            SceneManager.class.getResource("/css/main.css")).toExternalForm());
        
        mainController = loader.getController();
    }

    /**
     * Повертає контролер головного вікна.
     */
    public static MainController getMainController() {
        return mainController;
    }

    /**
     * Повертає головний Stage.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Встановлює заголовок вікна.
     */
    public static void setTitle(String title) {
        if (primaryStage != null) {
            primaryStage.setTitle(title);
        }
    }
}
