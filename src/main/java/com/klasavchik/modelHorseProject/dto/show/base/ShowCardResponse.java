// ShowCardResponse.java
package com.klasavchik.modelHorseProject.dto.show.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusRegOfShow;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowCardResponse {

    private Long id;
    private String name;
    private String bannerUrl;           // для карточки — главное изображение
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isPaid;
    private Integer additionalPrice;
    private boolean isCompleted;
    
    private List<String> organizers;    // никнеймы организаторов (creator + co-organizer)
    
    private StatusRegOfShow registrationStatus;  // статус заявки текущего пользователя (может быть null)
    private Long registrationId;                 // id заявки (может быть null)
    private String applicationNumber;            // номер заявки (может быть null)

    @JsonProperty("total_sum")
    private Integer totalSum;                    // цена билета + цена доп. модели * кол-во доп. моделей
}