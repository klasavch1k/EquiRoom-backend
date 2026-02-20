package com.klasavchik.modelHorseProject.dto.show.registration;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RegistrationListItemResponse {

    private Long registrationId;            // id регистрации

    // Информация об участнике
    private Long userId;
    private String userNickname;            // основной ник или "test@test.test"
    private String userDisplayName;         // "Анна К." или "Иван Петров"
    private String userAvatarUrl;           // аватарка (null если нет)

    // Тариф
    private Long ticketPriceId;
    private String ticketType;              // "Стандарт", "VIP"
    private Integer ticketPriceValue;       // 0, 1500
    private Integer includedModels;         // сколько входит по тарифу

    // Статистика участия
    private int totalModels;                // всего подано моделей
    private int additionalModels;           // сверх тарифа
    private Integer additionalPrice;        // цена за доп. модель (из Show)

    // Флаги и статус
    private boolean isSponsor;
    private boolean isJudge;
    private Integer lotteryTickets;
    private String status;                  // PENDING, PAID, APPROVED, CANCELLED

    private LocalDateTime createdAt;
}