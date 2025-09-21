package com.klasavchik.modelHorseProject.dto;

import com.klasavchik.modelHorseProject.entity.HorseModelMedia;
import com.klasavchik.modelHorseProject.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HorseModelResponse {
    private Long id;
    private String name;
    private String breed; // порода лошади
    private String avatar;
    private String description;
    private LocalDateTime releaseDate;
    private String masterName;
//    private User master;
//    private List<HorseModelMedia> horseModelMedia;


}
