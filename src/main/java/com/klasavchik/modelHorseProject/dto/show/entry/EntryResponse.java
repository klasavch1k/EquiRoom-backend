package com.klasavchik.modelHorseProject.dto.show.entry;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EntryResponse {
    private Long entryId;
    private Long classId;
    private Long horseId;
    private String horseName;
    private String status;
    private String mainPhotoUrl;
    private List<String> additionalPhotos;
    private LocalDateTime createdAt;
}
