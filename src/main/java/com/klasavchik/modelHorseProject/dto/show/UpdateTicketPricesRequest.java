package com.klasavchik.modelHorseProject.dto.show;

import lombok.Data;

import java.util.List;

@Data
public class UpdateTicketPricesRequest {
    private List<UpdateTicketPriceDto> ticketPrices;
}