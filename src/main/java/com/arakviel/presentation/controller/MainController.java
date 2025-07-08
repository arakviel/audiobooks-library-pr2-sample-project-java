package com.arakviel.presentation.controller;

import com.arakviel.presentation.MainApplication;
import com.arakviel.presentation.model.CurrentUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Головний контролер додатку з навігаційним меню та областю контенту.
 */
public class MainController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private VBox menuContainer;
    @FXML private Label userLabel;
    @FXML private Button dashboardButton;
    @FXML private Button audiobooksButton;
    @FXML private Button authorsButton;
    @FXML private Button genresButton;
    @FXML private Button collectionsButton;
    @FXML private Button progressButton;
    @FXML private Button logoutButton;

    private Button currentActiveButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Встановлюємо ім'я користувача
        userLabel.setText("Привіт, " + CurrentUser.getInstance().getDisplayName() + "!");
        
        // Встановлюємо початкову активну кнопку
        setActiveButton(dashboardButton);
        
        // Завантажуємо початкову сторінку
        loadDashboard();
    }

    /**
     * Завантажує дашборд (головну сторінку).
     */
    @FXML
    private void loadDashboard() {
        setActiveButton(dashboardButton);
        loadContent("/view/dashboard.fxml");
    }

    /**
     * Завантажує сторінку аудіокниг.
     */
    @FXML
    private void loadAudiobooks() {
        setActiveButton(audiobooksButton);
        loadContent("/view/audiobooks.fxml");
    }

    /**
     * Завантажує сторінку авторів.
     */
    @FXML
    private void loadAuthors() {
        setActiveButton(authorsButton);
        loadContent("/view/authors.fxml");
    }

    /**
     * Завантажує сторінку жанрів.
     */
    @FXML
    private void loadGenres() {
        setActiveButton(genresButton);
        loadContent("/view/genres.fxml");
    }

    /**
     * Завантажує сторінку колекцій.
     */
    @FXML
    private void loadCollections() {
        setActiveButton(collectionsButton);
        loadContent("/view/collections.fxml");
    }

    /**
     * Завантажує сторінку прогресу прослуховування.
     */
    @FXML
    private void loadProgress() {
        setActiveButton(progressButton);
        loadContent("/view/progress.fxml");
    }

    /**
     * Обробляє вихід з системи.
     */
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження виходу");
        alert.setHeaderText("Ви дійсно хочете вийти з системи?");
        alert.setContentText("Всі незбережені дані будуть втрачені.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                CurrentUser.getInstance().logout();
                MainApplication mainApp = new MainApplication();
                mainApp.returnToLoginScreen();
            }
        });
    }

    /**
     * Завантажує контент у центральну область.
     */
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            mainPane.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка завантаження", "Не вдалося завантажити сторінку: " + fxmlPath);
        }
    }

    /**
     * Встановлює активну кнопку меню.
     */
    private void setActiveButton(Button button) {
        // Скидаємо стиль попередньої активної кнопки
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-menu-button");
        }
        
        // Встановлюємо нову активну кнопку
        currentActiveButton = button;
        button.getStyleClass().add("active-menu-button");
    }

    /**
     * Показує діалог помилки.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
