package com.arakviel.presentation.controller;

import com.arakviel.application.contract.AuthorService;
import com.arakviel.application.contract.GenreService;
import com.arakviel.domain.entities.Audiobook;
import com.arakviel.domain.entities.Author;
import com.arakviel.domain.entities.Genre;
import com.arakviel.presentation.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Контролер для картки аудіокниги.
 */
public class AudiobookCardController implements Initializable {

    @FXML private VBox cardContainer;
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label genreLabel;
    @FXML private Label durationLabel;
    @FXML private Label yearLabel;
    @FXML private Button playButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button infoButton;

    private Audiobook audiobook;
    private AuthorService authorService;
    private GenreService genreService;
    
    // Callback функції для обробки дій
    private Consumer<Audiobook> onEdit;
    private Consumer<Audiobook> onDelete;
    private Consumer<Audiobook> onPlay;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authorService = ServiceProvider.getAuthorService();
        genreService = ServiceProvider.getGenreService();
        
        // Додаємо hover ефект для кнопки відтворення
        cardContainer.setOnMouseEntered(e -> playButton.setVisible(true));
        cardContainer.setOnMouseExited(e -> playButton.setVisible(false));
    }

    /**
     * Встановлює дані аудіокниги для відображення.
     */
    public void setAudiobook(Audiobook audiobook) {
        this.audiobook = audiobook;
        updateDisplay();
    }

    /**
     * Встановлює callback функції для обробки дій.
     */
    public void setCallbacks(Consumer<Audiobook> onEdit, Consumer<Audiobook> onDelete, Consumer<Audiobook> onPlay) {
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onPlay = onPlay;
    }

    /**
     * Оновлює відображення даних аудіокниги.
     */
    private void updateDisplay() {
        if (audiobook == null) return;

        titleLabel.setText(audiobook.getTitle());
        yearLabel.setText(String.valueOf(audiobook.getReleaseYear()));
        durationLabel.setText(formatDuration(audiobook.getDuration()));

        // Завантажуємо ім'я автора
        loadAuthorName();
        
        // Завантажуємо назву жанру
        loadGenreName();
    }

    /**
     * Завантажує та відображає ім'я автора.
     */
    private void loadAuthorName() {
        try {
            var authorOpt = authorService.findById(audiobook.getAuthorId());
            if (authorOpt.isPresent()) {
                Author author = authorOpt.get();
                authorLabel.setText(author.getFirstName() + " " + author.getLastName());
            } else {
                authorLabel.setText("Невідомий автор");
            }
        } catch (Exception e) {
            authorLabel.setText("Помилка завантаження автора");
        }
    }

    /**
     * Завантажує та відображає назву жанру.
     */
    private void loadGenreName() {
        try {
            var genreOpt = genreService.findById(audiobook.getGenreId());
            if (genreOpt.isPresent()) {
                Genre genre = genreOpt.get();
                genreLabel.setText(genre.getName());
            } else {
                genreLabel.setText("Невідомий жанр");
            }
        } catch (Exception e) {
            genreLabel.setText("Помилка завантаження жанру");
        }
    }

    /**
     * Форматує тривалість у зручний для читання формат.
     */
    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " хв";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            return hours + " год " + remainingMinutes + " хв";
        }
    }

    /**
     * Обробляє натискання кнопки відтворення.
     */
    @FXML
    private void handlePlay() {
        if (onPlay != null && audiobook != null) {
            onPlay.accept(audiobook);
        }
    }

    /**
     * Обробляє натискання кнопки редагування.
     */
    @FXML
    private void handleEdit() {
        if (onEdit != null && audiobook != null) {
            onEdit.accept(audiobook);
        }
    }

    /**
     * Обробляє натискання кнопки видалення.
     */
    @FXML
    private void handleDelete() {
        if (audiobook == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження видалення");
        alert.setHeaderText("Видалити аудіокнигу?");
        alert.setContentText("Ви дійсно хочете видалити аудіокнигу \"" + audiobook.getTitle() + "\"?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && onDelete != null) {
                onDelete.accept(audiobook);
            }
        });
    }

    /**
     * Обробляє натискання кнопки інформації.
     */
    @FXML
    private void handleInfo() {
        if (audiobook == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Інформація про аудіокнигу");
        alert.setHeaderText(audiobook.getTitle());
        
        StringBuilder content = new StringBuilder();
        content.append("Автор: ").append(authorLabel.getText()).append("\n");
        content.append("Жанр: ").append(genreLabel.getText()).append("\n");
        content.append("Рік випуску: ").append(audiobook.getReleaseYear()).append("\n");
        content.append("Тривалість: ").append(formatDuration(audiobook.getDuration())).append("\n");
        
        if (audiobook.getDescription() != null && !audiobook.getDescription().trim().isEmpty()) {
            content.append("\nОпис:\n").append(audiobook.getDescription());
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
}
