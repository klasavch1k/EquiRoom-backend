package com.klasavchik.modelHorseProject.dto.show;

import com.klasavchik.modelHorseProject.dto.show.price.TicketPriceDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

// CreateShowRequest.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowRequest {
    @NotBlank(message = "Название обязательно")
    private String name;

    private String description;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private boolean lotteryEnabled;

    private Integer additionalPrice;         // null = бесплатно

    @NotNull
    private Boolean isPaid;

    private Integer maxAdditionalPhotos;     // null = 3 по умолчанию

    // сразу можно передать цены билетов
    private List<TicketPriceDto> ticketPrices;
}

