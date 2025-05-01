package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Сутність, що представляє користувача системи.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private String username;
    private String passwordHash;
    private String email;
    private String avatarPath;
}
