package com.klasavchik.modelHorseProject.dto.show;

import lombok.Data;

@Data
public class UpdateTicketPriceDto {
    private Long id;                       // если есть — обновляем существующий, если null — создаём новый
    private String type;
    private Integer price;
    private Integer includedModels;
    private String description;
}