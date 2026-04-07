package com.klasavchik.modelHorseProject.dto.show.entry;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyEntryResponse {
    private Long entryId;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long divisionId;
    private String divisionName;
    private Long horseId;
    private String horseName;
    private String horseAvatar;
    private String mainPhotoUrl;
    private List<String> additionalPhotos;
    private String status;
    private LocalDateTime createdAt;
}
