package com.klasavchik.modelHorseProject.dto.show.judging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgingEntryDto {
    private Long entryId;
    private Long horseId;
    private String horseName;
    private String horseAvatar;
    private String mainPhotoUrl;
    private List<String> additionalPhotos;
    private Long userId;
    private String userDisplayName;
    private String userNickname;
    private boolean submittedByCurrentJudge;
}
