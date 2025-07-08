package com.arakviel.presentation.util;

import com.arakviel.application.contract.*;
import org.springframework.context.ApplicationContext;

/**
 * Провайдер сервісів для доступу до бізнес-логіки з презентаційного шару.
 */
public class ServiceProvider {
    
    private static ApplicationContext applicationContext;

    /**
     * Встановлює Spring ApplicationContext.
     */
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * Повертає сервіс користувачів.
     */
    public static UserService getUserService() {
        return applicationContext.getBean(UserService.class);
    }

    /**
     * Повертає сервіс аудіокниг.
     */
    public static AudiobookService getAudiobookService() {
        return applicationContext.getBean(AudiobookService.class);
    }

    /**
     * Повертає сервіс авторів.
     */
    public static AuthorService getAuthorService() {
        return applicationContext.getBean(AuthorService.class);
    }

    /**
     * Повертає сервіс жанрів.
     */
    public static GenreService getGenreService() {
        return applicationContext.getBean(GenreService.class);
    }

    /**
     * Повертає сервіс колекцій.
     */
    public static CollectionService getCollectionService() {
        return applicationContext.getBean(CollectionService.class);
    }

    /**
     * Повертає сервіс файлів аудіокниг.
     */
    public static AudiobookFileService getAudiobookFileService() {
        return applicationContext.getBean(AudiobookFileService.class);
    }

    /**
     * Повертає сервіс прогресу прослуховування.
     */
    public static ListeningProgressService getListeningProgressService() {
        return applicationContext.getBean(ListeningProgressService.class);
    }
}
