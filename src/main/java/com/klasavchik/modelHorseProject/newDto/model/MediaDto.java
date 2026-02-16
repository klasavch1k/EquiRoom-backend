package com.klasavchik.modelHorseProject.newDto.model;

import com.klasavchik.modelHorseProject.entity.settings.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// не используется на прямую, нужен для детпльного возврата информации о модели
public class MediaDto {
    private Long id;
    private String url;
    private MediaType mediaType;
    private LocalDateTime createdAt;
}
