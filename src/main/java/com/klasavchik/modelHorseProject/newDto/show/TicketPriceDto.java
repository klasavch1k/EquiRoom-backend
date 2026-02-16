package com.klasavchik.modelHorseProject.newDto.show;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketPriceDto {
    private String type;
    private Integer price;
    private Integer includedModels;
    private String description;
}