package com.klasavchik.modelHorseProject.dto.show.registration;

import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusRegOfShow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRegistrationStatusRequest {
    private StatusRegOfShow status;
}
