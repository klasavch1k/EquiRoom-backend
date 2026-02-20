package com.klasavchik.modelHorseProject.dto.show.price;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdditionalPriceResponse {
    private Integer additionalPrice;  // например 300 за каждую доп. модель сверх includedModels
}