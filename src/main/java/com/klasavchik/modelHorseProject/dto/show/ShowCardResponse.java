// ShowCardResponse.java
package com.klasavchik.modelHorseProject.dto.show;

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
    
}