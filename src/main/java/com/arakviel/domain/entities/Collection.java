package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сутність, що представляє колекцію аудіокниг користувача.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Collection {

    private UUID id;
    private UUID userId;
    private String name;
    private LocalDateTime createdAt;
}
