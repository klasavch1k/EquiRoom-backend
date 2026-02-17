package com.klasavchik.modelHorseProject.dto.show;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShowFullInfoResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String rulesFileUrl;
    private String bannerUrl;
    private boolean lotteryEnabled;
    private Integer additionalPrice;
    private boolean isPaid;
    private Integer maxAdditionalPhotos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isCompleted;

    private List<OrganizerShortDto> organizers;
    private List<TicketPriceDto> ticketPrices;
    private List<JudgeShortDto> judges;
}