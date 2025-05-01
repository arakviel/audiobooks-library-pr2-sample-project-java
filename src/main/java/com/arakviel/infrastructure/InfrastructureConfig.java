package com.arakviel.infrastructure;

import com.arakviel.domain.entities.*;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.*;
import com.arakviel.infrastructure.persistence.impl.*;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.ConnectionPool.PoolConfig;
import com.arakviel.infrastructure.persistence.util.PersistenceInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class InfrastructureConfig {

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.pool.size}")
    private int dbPoolSize;

    @Value("${db.auto.commit}")
    private boolean dbAutoCommit;

    @Bean
    public ConnectionPool connectionPool() {
        PoolConfig poolConfig = new PoolConfig.Builder()
                .withUrl(dbUrl)
                .withUser(dbUsername)
                .withPassword(dbPassword)
                .withMaxConnections(dbPoolSize)
                .withAutoCommit(dbAutoCommit)
                .build();
        return ConnectionPool.getInstance(poolConfig);
    }

    @Bean
    public PersistenceContext persistenceContext(ConnectionPool connectionPool,
                                                 AudiobookRepository audiobookRepository,
                                                 AudiobookFileRepository audiobookFileRepository,
                                                 AuthorRepository authorRepository,
                                                 GenreRepository genreRepository,
                                                 CollectionRepository collectionRepository,
                                                 ListeningProgressRepository listeningProgressRepository,
                                                 UserRepository userRepository) {
        PersistenceContext persistenceContext = new PersistenceContext(connectionPool);

        persistenceContext.registerRepository(Audiobook.class, audiobookRepository);
        persistenceContext.registerRepository(AudiobookFile.class, audiobookFileRepository);
        persistenceContext.registerRepository(Author.class, authorRepository);
        persistenceContext.registerRepository(Genre.class, genreRepository);
        persistenceContext.registerRepository(Collection.class, collectionRepository);
        persistenceContext.registerRepository(ListeningProgress.class, listeningProgressRepository);
        persistenceContext.registerRepository(User.class, userRepository);
        return persistenceContext;
    }

    @Bean
    public AudiobookRepository audiobookRepository(ConnectionPool connectionPool) {
        return new AudiobookRepositoryImpl(connectionPool);
    }

    @Bean
    public AudiobookFileRepository audiobookFileRepository(ConnectionPool connectionPool) {
        return new AudiobookFileRepositoryImpl(connectionPool);
    }

    @Bean
    public AuthorRepository authorRepository(ConnectionPool connectionPool) {
        return new AuthorRepositoryImpl(connectionPool);
    }

    @Bean
    public GenreRepository genreRepository(ConnectionPool connectionPool) {
        return new GenreRepositoryImpl(connectionPool);
    }

    @Bean
    public CollectionRepository collectionRepository(ConnectionPool connectionPool) {
        return new CollectionRepositoryImpl(connectionPool);
    }

    @Bean
    public ListeningProgressRepository listeningProgressRepository(ConnectionPool connectionPool) {
        return new ListeningProgressRepositoryImpl(connectionPool);
    }

    @Bean
    public UserRepository userRepository(ConnectionPool connectionPool) {
        return new UserRepositoryImpl(connectionPool);
    }

    @Bean
    public PersistenceInitializer persistenceInitializer(ConnectionPool connectionPool) {
        return new PersistenceInitializer(connectionPool);
    }
}