package com.arakviel.presentation.controller;

import com.arakviel.application.contract.UserService;
import com.arakviel.domain.entities.User;
import com.arakviel.presentation.MainApplication;
import com.arakviel.presentation.model.CurrentUser;
import com.arakviel.presentation.util.ServiceProvider;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Контролер для екрану авторизації та реєстрації.
 */
public class LoginController implements Initializable {

    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private TextField registerUsername;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private PasswordField registerConfirmPassword;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button switchToRegisterButton;
    @FXML private Button switchToLoginButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    private MainApplication mainApplication;
    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = ServiceProvider.getUserService();
        
        // Початково показуємо форму авторизації
        showLoginForm();
        
        // Приховуємо індикатор прогресу
        progressIndicator.setVisible(false);
    }

    /**
     * Встановлює посилання на головний додаток.
     */
    public void setMainApplication(MainApplication mainApplication) {
        this.mainApplication = mainApplication;
    }

    /**
     * Обробляє натискання кнопки "Увійти".
     */
    @FXML
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Будь ласка, заповніть всі поля", true);
            return;
        }

        // Показуємо індикатор завантаження
        setLoading(true);

        // Виконуємо авторизацію в окремому потоці
        Task<Boolean> loginTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    List<User> users = userService.findByUsername(username);
                    if (!users.isEmpty()) {
                        User user = users.get(0);
                        if (BCrypt.checkpw(password, user.getPasswordHash())) {
                            CurrentUser.getInstance().setUser(user);
                            return true;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        loginTask.setOnSucceeded(e -> {
            setLoading(false);
            if (loginTask.getValue()) {
                showStatus("Успішна авторизація!", false);
                mainApplication.showMainWindow();
            } else {
                showStatus("Невірне ім'я користувача або пароль", true);
            }
        });

        loginTask.setOnFailed(e -> {
            setLoading(false);
            showStatus("Помилка при авторизації", true);
        });

        new Thread(loginTask).start();
    }

    /**
     * Обробляє натискання кнопки "Зареєструватися".
     */
    @FXML
    private void handleRegister() {
        String username = registerUsername.getText().trim();
        String email = registerEmail.getText().trim();
        String password = registerPassword.getText();
        String confirmPassword = registerConfirmPassword.getText();

        // Валідація
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("Будь ласка, заповніть всі поля", true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showStatus("Паролі не співпадають", true);
            return;
        }

        if (password.length() < 6) {
            showStatus("Пароль повинен містити принаймні 6 символів", true);
            return;
        }

        // Показуємо індикатор завантаження
        setLoading(true);

        // Виконуємо реєстрацію в окремому потоці
        Task<Boolean> registerTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    // Перевіряємо, чи не існує користувач з таким ім'ям
                    if (!userService.findByUsername(username).isEmpty()) {
                        return false;
                    }

                    // Створюємо нового користувача
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    User newUser = new User(UUID.randomUUID(), username, hashedPassword, email, null);

                    userService.create(newUser, null, null);
                    CurrentUser.getInstance().setUser(newUser);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        registerTask.setOnSucceeded(e -> {
            setLoading(false);
            if (registerTask.getValue()) {
                showStatus("Успішна реєстрація!", false);
                mainApplication.showMainWindow();
            } else {
                showStatus("Користувач з таким ім'ям вже існує", true);
            }
        });

        registerTask.setOnFailed(e -> {
            setLoading(false);
            showStatus("Помилка при реєстрації", true);
        });

        new Thread(registerTask).start();
    }

    /**
     * Переключає на форму реєстрації.
     */
    @FXML
    private void switchToRegister() {
        showRegisterForm();
        clearFields();
        clearStatus();
    }

    /**
     * Переключає на форму авторизації.
     */
    @FXML
    private void switchToLogin() {
        showLoginForm();
        clearFields();
        clearStatus();
    }

    /**
     * Показує форму авторизації.
     */
    private void showLoginForm() {
        loginForm.setVisible(true);
        registerForm.setVisible(false);
    }

    /**
     * Показує форму реєстрації.
     */
    private void showRegisterForm() {
        loginForm.setVisible(false);
        registerForm.setVisible(true);
    }

    /**
     * Очищає всі поля форм.
     */
    private void clearFields() {
        loginUsername.clear();
        loginPassword.clear();
        registerUsername.clear();
        registerEmail.clear();
        registerPassword.clear();
        registerConfirmPassword.clear();
    }

    /**
     * Показує статус повідомлення.
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #d32f2f;" : "-fx-text-fill: #388e3c;");
        statusLabel.setVisible(true);
    }

    /**
     * Очищає статус повідомлення.
     */
    private void clearStatus() {
        statusLabel.setVisible(false);
    }

    /**
     * Встановлює стан завантаження.
     */
    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        registerButton.setDisable(loading);
    }
}
