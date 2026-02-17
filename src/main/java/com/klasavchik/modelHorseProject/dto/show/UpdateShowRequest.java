package com.klasavchik.modelHorseProject.dto.show;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateShowRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String rulesFileUrl;           // можно обновлять ссылку
    private String bannerUrl;              // можно обновлять ссылку
    private Boolean lotteryEnabled;
    private Integer additionalPrice;
    private Boolean isPaid;
    private Integer maxAdditionalPhotos;
    // isCompleted не даём менять через эту ручку — только отдельно
}