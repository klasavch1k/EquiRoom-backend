package com.klasavchik.modelHorseProject.dto.show;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TicketPriceDto {
    private Long id;
    private String type;
    private Integer price;
    private Integer includedModels;
    private String description;
}