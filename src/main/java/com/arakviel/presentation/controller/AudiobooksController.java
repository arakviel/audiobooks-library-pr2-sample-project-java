package com.arakviel.presentation.controller;

import com.arakviel.application.contract.AudiobookService;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.presentation.util.ServiceProvider;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Контролер для сторінки аудіокниг.
 */
public class AudiobooksController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private FlowPane audiobooksContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;

    private AudiobookService audiobookService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audiobookService = ServiceProvider.getAudiobookService();
        loadAudiobooks();
    }

    /**
     * Створює картку для аудіокниги.
     */
    private void createAudiobookCard(Audiobook audiobook) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/audiobook-card.fxml"));
            loader.load();

            AudiobookCardController cardController = loader.getController();
            cardController.setAudiobook(audiobook);

            // Встановлюємо callback функції
            cardController.setCallbacks(
                this::editAudiobook,    // onEdit
                this::deleteAudiobook,  // onDelete
                this::playAudiobook     // onPlay
            );

            // Додаємо картку до контейнера
            audiobooksContainer.getChildren().add(loader.getRoot());

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка", "Не вдалося створити картку аудіокниги");
        }
    }

    /**
     * Завантажує список аудіокниг.
     */
    private void loadAudiobooks() {
        loadingIndicator.setVisible(true);
        statusLabel.setText("Завантаження аудіокниг...");

        Task<List<Audiobook>> loadTask = new Task<List<Audiobook>>() {
            @Override
            protected List<Audiobook> call() throws Exception {
                return audiobookService.findAll(0, 100); // Завантажуємо перші 100 записів
            }
        };

        loadTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            List<Audiobook> loadedAudiobooks = loadTask.getValue();
            displayAudiobooks(loadedAudiobooks);
            statusLabel.setText("Завантажено " + loadedAudiobooks.size() + " аудіокниг");
        });

        loadTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            statusLabel.setText("Помилка завантаження аудіокниг");
            showErrorAlert("Помилка", "Не вдалося завантажити список аудіокниг");
        });

        new Thread(loadTask).start();
    }

    /**
     * Обробляє пошук аудіокниг.
     */
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadAudiobooks();
            return;
        }

        loadingIndicator.setVisible(true);
        statusLabel.setText("Пошук...");

        Task<List<Audiobook>> searchTask = new Task<List<Audiobook>>() {
            @Override
            protected List<Audiobook> call() throws Exception {
                return audiobookService.findByTitle(searchText);
            }
        };

        searchTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            List<Audiobook> foundAudiobooks = searchTask.getValue();
            displayAudiobooks(foundAudiobooks);
            statusLabel.setText("Знайдено " + foundAudiobooks.size() + " аудіокниг");
        });

        searchTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            statusLabel.setText("Помилка пошуку");
            showErrorAlert("Помилка", "Не вдалося виконати пошук");
        });

        new Thread(searchTask).start();
    }

    /**
     * Обробляє додавання нової аудіокниги.
     */
    @FXML
    private void handleAdd() {
        // Тут буде відкриватися діалог додавання нової аудіокниги
        showInfoAlert("Інформація", "Функція додавання аудіокниги буде реалізована пізніше");
    }

    /**
     * Відображає список аудіокниг у вигляді карток.
     */
    private void displayAudiobooks(List<Audiobook> audiobooks) {
        // Очищуємо контейнер
        audiobooksContainer.getChildren().clear();

        // Створюємо картки для кожної аудіокниги
        for (Audiobook audiobook : audiobooks) {
            createAudiobookCard(audiobook);
        }
    }

    /**
     * Редагує аудіокнигу.
     */
    private void editAudiobook(Audiobook audiobook) {
        showInfoAlert("Інформація", "Функція редагування аудіокниги \"" + audiobook.getTitle() + "\" буде реалізована пізніше");
    }

    /**
     * Видаляє аудіокнигу.
     */
    private void deleteAudiobook(Audiobook audiobook) {
        // Тут буде логіка видалення з бази даних
        showInfoAlert("Інформація", "Функція видалення аудіокниги \"" + audiobook.getTitle() + "\" буде реалізована пізніше");

        // Після видалення перезавантажуємо список
        // loadAudiobooks();
    }

    /**
     * Відтворює аудіокнигу.
     */
    private void playAudiobook(Audiobook audiobook) {
        showInfoAlert("Відтворення", "Відтворення аудіокниги \"" + audiobook.getTitle() + "\" буде реалізовано пізніше");
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

    /**
     * Показує інформаційний діалог.
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
