package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Сутність, що представляє колекцію аудіокниг користувача.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Collection implements Comparable<Collection> {

    private UUID id;
    private UUID userId;
    private String name;
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Collection other) {
        // First compare by name
        int nameComparison = this.name.compareToIgnoreCase(other.name);
        if (nameComparison != 0) {
            return nameComparison;
        }

        // If names are equal, compare by creation date (newer first)
        return other.createdAt.compareTo(this.createdAt);
    }
}
