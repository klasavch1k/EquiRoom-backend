package com.klasavchik.modelHorseProject.dto.show.entry;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EntryDetailResponse {
    private Long entryId;
    private Long classId;
    private String className;
    private Long userId;
    private String userDisplayName;
    private String userNickname;
    private String userAvatarUrl;
    private Long horseId;
    private String horseName;
    private String horseAvatar;
    private String mainPhotoUrl;
    private List<String> additionalPhotos;
    private String status;
    private LocalDateTime createdAt;
    private Long registrationId;
    private String registrationStatus;
}
