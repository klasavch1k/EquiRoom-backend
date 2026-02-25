package com.klasavchik.modelHorseProject.dto.show.base;// Входящие (create)

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateDivisionDto {
    private String name;         // обязательно
    private String description;  // опционально
    private String type;         // опционально (halter, performance, collectibility и т.д.)
}