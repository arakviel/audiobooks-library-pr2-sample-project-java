package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Сутність, що представляє жанр аудіокниги.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

    private UUID id;
    private String name;
    private String description;
}
