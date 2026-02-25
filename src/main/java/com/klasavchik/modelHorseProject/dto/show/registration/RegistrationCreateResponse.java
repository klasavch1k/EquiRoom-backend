package com.klasavchik.modelHorseProject.dto.show.registration;

import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusRegOfShow;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationCreateResponse {
    private Long registrationId;
    private String applicationNumber; // добавлено: номер заявки
    private StatusRegOfShow status;     // теперь enum: PENDING, APPROVED и т.д.
    private boolean isSponsor;
    private String message;             // человекопонятное сообщение
}