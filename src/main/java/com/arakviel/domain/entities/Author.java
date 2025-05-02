package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * Сутність, що представляє автора аудіокниги.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Author implements Comparable<Author> {

    private UUID id;
    private String firstName;
    private String lastName;
    private String bio;
    private String imagePath;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Author other) {
        // First compare by lastName
        int lastNameComparison = this.lastName.compareToIgnoreCase(other.lastName);
        if (lastNameComparison != 0) {
            return lastNameComparison;
        }

        // If last names are equal, compare by firstName
        return this.firstName.compareToIgnoreCase(other.firstName);
    }
}
