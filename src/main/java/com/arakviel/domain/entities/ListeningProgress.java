package com.arakviel.domain.entities;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Сутність, що представляє прогрес прослуховування аудіокниги користувачем.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListeningProgress implements Comparable<ListeningProgress> {

    @EqualsAndHashCode.Include
    private UUID id;
    private UUID userId;
    private UUID audiobookId;
    private int position;
    private LocalDateTime lastListened;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListeningProgress that = (ListeningProgress) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(ListeningProgress other) {
        // First compare by lastListened (descending - newest first)
        if (this.lastListened != null && other.lastListened != null) {
            int timeComparison = other.lastListened.compareTo(this.lastListened);
            if (timeComparison != 0) {
                return timeComparison;
            }
        } else if (this.lastListened == null && other.lastListened != null) {
            return 1; // Null values come after non-null values
        } else if (this.lastListened != null && other.lastListened == null) {
            return -1; // Non-null values come before null values
        }

        // If lastListened dates are equal or both null, compare by position (descending)
        return Integer.compare(other.position, this.position);
    }
}
