package com.arakviel.presentation.model;

import com.arakviel.domain.entities.User;

/**
 * Singleton клас для зберігання інформації про поточного авторизованого користувача.
 */
public class CurrentUser {
    
    private static CurrentUser instance;
    private User user;

    private CurrentUser() {}

    /**
     * Повертає єдиний екземпляр CurrentUser.
     */
    public static CurrentUser getInstance() {
        if (instance == null) {
            instance = new CurrentUser();
        }
        return instance;
    }

    /**
     * Встановлює поточного користувача.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Повертає поточного користувача.
     */
    public User getUser() {
        return user;
    }

    /**
     * Перевіряє, чи є користувач авторизованим.
     */
    public boolean isLoggedIn() {
        return user != null;
    }

    /**
     * Виходить з системи (очищає дані користувача).
     */
    public void logout() {
        user = null;
    }

    /**
     * Повертає ім'я користувача або "Гість", якщо не авторизований.
     */
    public String getDisplayName() {
        return user != null ? user.getUsername() : "Гість";
    }
}
