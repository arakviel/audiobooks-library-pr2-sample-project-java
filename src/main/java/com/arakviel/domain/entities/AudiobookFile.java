package com.arakviel.domain.entities;

import com.arakviel.domain.enums.FileFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * Сутність, що представляє файл аудіокниги
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AudiobookFile implements Comparable<AudiobookFile> {

    private UUID id;
    private UUID audiobookId;
    private String filePath;
    private FileFormat format;
    private Integer size;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudiobookFile that = (AudiobookFile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(AudiobookFile other) {
        // First compare by format
        int formatComparison = this.format.compareTo(other.format);
        if (formatComparison != 0) {
            return formatComparison;
        }

        // If formats are equal, compare by size (ascending)
        return Integer.compare(this.size, other.size);
    }
}
