package com.klasavchik.modelHorseProject.dto.show.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EntryCountResponse {
    private int totalSubmitted;
    private int maxAllowed;
}
