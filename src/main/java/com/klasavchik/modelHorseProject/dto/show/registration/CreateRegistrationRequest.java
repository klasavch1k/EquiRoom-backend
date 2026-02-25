package com.klasavchik.modelHorseProject.dto.show.registration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateRegistrationRequest {
    private Long ticketPriceId;         // обязательно если !isSponsor
    private boolean isSponsor;
    private String sponsorLink;
    private Integer lotteryTickets;
    
    private Integer extraModels;        // ← сколько ДОПОЛНИТЕЛЬНЫХ моделей хочет купить сверх тарифа
}