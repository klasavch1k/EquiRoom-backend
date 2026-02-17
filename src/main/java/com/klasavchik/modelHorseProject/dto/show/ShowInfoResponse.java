package com.klasavchik.modelHorseProject.dto.show;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter
@Setter
@Builder
public class ShowInfoResponse {
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
    private boolean isCompleted;
    private List<OrganizerDto> organizers;
    private List<TicketPriceDto> ticketPrices;
}