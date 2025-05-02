package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * Сутність, що представляє жанр аудіокниги.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre implements Comparable<Genre> {

    private UUID id;
    private String name;
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return Objects.equals(id, genre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Genre other) {
        // Compare by name
        return this.name.compareToIgnoreCase(other.name);
    }
}
