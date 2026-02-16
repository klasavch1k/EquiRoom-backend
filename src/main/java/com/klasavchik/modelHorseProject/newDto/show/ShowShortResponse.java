package com.klasavchik.modelHorseProject.newDto.show;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// ShowShortResponse.java (минимальный ответ)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowShortResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String rulesFileUrl;
    private boolean lotteryEnabled;
    private Integer additionalPrice;
    private boolean isPaid;
    private Integer maxAdditionalPhotos;
    private String bannerUrl;
    private String createdByNickname;
    private boolean isCompleted;
}