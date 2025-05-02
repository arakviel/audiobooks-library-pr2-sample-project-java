package com.arakviel.domain.entities;

import lombok.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Сутність, що представляє аудіокнигу.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Audiobook implements Comparable<Audiobook> {

    @EqualsAndHashCode.Include
    private UUID id;
    private UUID authorId;
    private UUID genreId;
    private String title;
    private int duration;
    private int releaseYear;
    private String description;
    private String coverImagePath;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Audiobook audiobook = (Audiobook) o;
        return Objects.equals(id, audiobook.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Audiobook other) {
        // First compare by title
        int titleComparison = this.title.compareToIgnoreCase(other.title);
        if (titleComparison != 0) {
            return titleComparison;
        }

        // If titles are equal, compare by release year (descending - newer first)
        return Integer.compare(other.releaseYear, this.releaseYear);
    }
}
