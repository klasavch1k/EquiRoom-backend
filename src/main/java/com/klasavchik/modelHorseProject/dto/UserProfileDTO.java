package com.klasavchik.modelHorseProject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String nickName; // Используем как аватар или отображаемое имя
    private int modelsCount; // Кол-во фигурок из коллекции
    private int collectingCount; // Кол-во коллекций (пока заглушка)
    private int membersCount; // Кол-во членов (пока заглушка)
}