package com.arakviel.infrastructure;

import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.file.impl.FileStorageServiceImpl;
import com.arakviel.infrastructure.persistence.util.ConnectionPool;
import com.arakviel.infrastructure.persistence.util.ConnectionPool.PoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("com.arakviel.infrastructure")
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

    @Value("${file.storage.root}")
    private String storageRootPath;

    @Value("${file.storage.allowed-extensions}")
    private String[] allowedExtensions;

    @Value("${file.storage.max-size}")
    private long maxFileSize;

    @Bean
    public ConnectionPool connectionPool() {
        PoolConfig poolConfig = new PoolConfig.Builder()
                .withUrl(dbUrl)
                .withUser(dbUsername)
                .withPassword(dbPassword)
                .withMaxConnections(dbPoolSize)
                .withAutoCommit(dbAutoCommit)
                .build();
        return new ConnectionPool(poolConfig);
    }

    @Bean
    public FileStorageService fileStorageService() {
        return new FileStorageServiceImpl(storageRootPath, allowedExtensions, maxFileSize);
    }
}