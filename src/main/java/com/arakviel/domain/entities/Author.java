package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Сутність, що представляє автора аудіокниги.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {

    private UUID id;
    private String firstName;
    private String lastName;
    private String bio;
    private String imagePath;
}
