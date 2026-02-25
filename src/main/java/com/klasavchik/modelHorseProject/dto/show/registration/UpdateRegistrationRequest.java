package com.klasavchik.modelHorseProject.dto.show.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRegistrationRequest {
    private Long ticketPriceId;         // может измениться
    private boolean isSponsor;          // может измениться статус спонсора
    private String sponsorLink;
    private Integer lotteryTickets;

    private Integer extraModels;        // изменение количества доп. моделей
}
