package com.arakviel.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сутність, що представляє прогрес прослуховування аудіокниги користувачем.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListeningProgress {

    private UUID id;
    private UUID userId;
    private UUID audiobookId;
    private int position;
    private LocalDateTime lastListened;
}
