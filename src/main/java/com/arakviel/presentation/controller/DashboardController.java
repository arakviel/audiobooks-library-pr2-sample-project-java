package com.arakviel.presentation.controller;

import com.arakviel.application.contract.AudiobookService;
import com.arakviel.application.contract.AuthorService;
import com.arakviel.application.contract.CollectionService;
import com.arakviel.application.contract.UserService;
import com.arakviel.presentation.model.CurrentUser;
import com.arakviel.presentation.util.ServiceProvider;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Контролер для дашборду (головної сторінки).
 */
public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label totalAudiobooksLabel;
    @FXML private Label totalAuthorsLabel;
    @FXML private Label totalCollectionsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private AudiobookService audiobookService;
    private AuthorService authorService;
    private CollectionService collectionService;
    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ініціалізація сервісів
        audiobookService = ServiceProvider.getAudiobookService();
        authorService = ServiceProvider.getAuthorService();
        collectionService = ServiceProvider.getCollectionService();
        userService = ServiceProvider.getUserService();

        // Встановлюємо привітання
        welcomeLabel.setText("Ласкаво просимо, " + CurrentUser.getInstance().getDisplayName() + "!");

        // Завантажуємо статистику
        loadStatistics();
    }

    /**
     * Завантажує статистику системи.
     */
    private void loadStatistics() {
        loadingIndicator.setVisible(true);

        Task<Void> statisticsTask = new Task<Void>() {
            private long audiobooksCount = 0;
            private long authorsCount = 0;
            private long collectionsCount = 0;
            private long usersCount = 0;

            @Override
            protected Void call() throws Exception {
                try {
                    audiobooksCount = audiobookService.count();
                    authorsCount = authorService.count();
                    collectionsCount = collectionService.count();
                    usersCount = userService.count();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                loadingIndicator.setVisible(false);
                updateStatisticsLabels();
            }

            @Override
            protected void failed() {
                loadingIndicator.setVisible(false);
                updateStatisticsLabels();
            }

            private void updateStatisticsLabels() {
                totalAudiobooksLabel.setText(String.valueOf(audiobooksCount));
                totalAuthorsLabel.setText(String.valueOf(authorsCount));
                totalCollectionsLabel.setText(String.valueOf(collectionsCount));
                totalUsersLabel.setText(String.valueOf(usersCount));
            }
        };

        new Thread(statisticsTask).start();
    }
}
