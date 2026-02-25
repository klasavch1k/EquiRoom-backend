package com.klasavchik.modelHorseProject.dto.show.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.klasavchik.modelHorseProject.dto.show.organizersJudges.JudgeShortDto;
import com.klasavchik.modelHorseProject.dto.show.organizersJudges.OrganizerShortDto;
import com.klasavchik.modelHorseProject.dto.show.price.TicketPriceDto;
import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusRegOfShow;
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
    private boolean isCurrentUserOrganizer;
    private boolean isCurrentUserJudge;

    private StatusRegOfShow registrationStatus;  // статус заявки текущего пользователя (может быть null)
    private Long registrationId;                 // id заявки (может быть null)
    private String applicationNumber;            // номер заявки (может быть null)

    @JsonProperty("total_sum")
    private Integer totalSum;                    // цена билета + цена доп. модели * кол-во доп. моделей

    private List<OrganizerShortDto> organizers;
    private List<TicketPriceDto> ticketPrices;
    private List<JudgeShortDto> judges;
}