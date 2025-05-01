package com.arakviel.domain.entities;

import com.arakviel.domain.enums.FileFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Сутність, що представляє файл аудіокниги
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudiobookFile {

    private UUID id;
    private UUID audiobookId;
    private String filePath;
    private FileFormat format;
    private Integer size;
}
