package com.klasavchik.modelHorseProject.dto.show.entry;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEntryRequest {
    @NotNull(message = "horseId обязателен")
    private Long horseId;

    @NotNull(message = "mainPhotoUrl обязателен")
    private String mainPhotoUrl;

    private List<String> additionalPhotos;
}
