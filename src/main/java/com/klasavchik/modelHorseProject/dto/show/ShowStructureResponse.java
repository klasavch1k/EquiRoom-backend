package com.klasavchik.modelHorseProject.dto.show;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShowStructureResponse {
    private Long showId;
    private List<DivisionStructureDto> divisions;
}