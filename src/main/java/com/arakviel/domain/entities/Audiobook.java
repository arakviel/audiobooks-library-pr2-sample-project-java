package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Сутність, що представляє аудіокнигу.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Audiobook {

    private UUID id;
    private UUID authorId;
    private UUID genreId;
    private String title;
    private int duration;
    private int releaseYear;
    private String description;
    private String coverImagePath;
}
