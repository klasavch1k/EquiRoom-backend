package com.klasavchik.modelHorseProject.dto.show.entry;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClassEntryListItem {
    private Long entryId;
    private Long userId;
    private String userDisplayName;
    private String userNickname;
    private String userAvatarUrl;
    private Long horseId;
    private String horseName;
    private String horseAvatar;
    private String mainPhotoUrl;
    private String status;
    private LocalDateTime createdAt;
}
