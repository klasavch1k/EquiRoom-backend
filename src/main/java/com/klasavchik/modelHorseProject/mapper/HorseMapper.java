package com.klasavchik.modelHorseProject.mapper;

import com.klasavchik.modelHorseProject.dto.CreateHorseRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelListRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelMediaRequest;
import com.klasavchik.modelHorseProject.entity.HorseModel;
import com.klasavchik.modelHorseProject.entity.HorseModelMedia;
import org.hibernate.query.spi.HqlInterpretation;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Component
public class HorseMapper {
    public HorseModelListRequest toDto(HorseModel horseModel){
        return HorseModelListRequest.builder()
                .id(horseModel.getId())
                .name(horseModel.getName())
                .breed(horseModel.getBreed())
                .avatar(horseModel.getHorseModelMedia().get(0).getImageLink())
                .build();
    }
    public HorseModel toEntity(CreateHorseRequest dto){
        HorseModel model = HorseModel.builder()
                .name(dto.getName())
                .breed(dto.getBreed())
                .description(dto.getDescription())
                .masterName(dto.getMasterName())
                .build();

        List<HorseModelMedia> mediaList = dto.getMedia().stream()
                .map(m -> HorseModelMedia.builder()
                        .dateUpdate(LocalDateTime.now())
                        .imageLink(m.getLink())
                        .horseModel(model)
                        .build())
                .toList();
        model.setHorseModelMedia(mediaList);
        return model;

    }
}
