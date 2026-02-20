package com.klasavchik.modelHorseProject.dto.show.price;

import lombok.Data;

@Data
public class UpdateAdditionalPriceDto {
    private Integer additionalPrice; // null = бесплатно
}