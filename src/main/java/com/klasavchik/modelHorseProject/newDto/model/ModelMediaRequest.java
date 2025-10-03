package com.klasavchik.modelHorseProject.newDto.model;

import com.klasavchik.modelHorseProject.entity.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

// не используется напрямую, нужен для создания модели
public class ModelMediaRequest {
    private Long id;                 // новый
    private String url;           // ссылка на фото/видео
    private MediaType mediaType;  // IMAGE или VIDEO
}
