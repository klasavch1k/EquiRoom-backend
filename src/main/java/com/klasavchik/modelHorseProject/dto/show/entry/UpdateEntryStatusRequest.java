package com.klasavchik.modelHorseProject.dto.show.entry;

import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEntryStatusRequest {
    private StatusEntry status;
}
