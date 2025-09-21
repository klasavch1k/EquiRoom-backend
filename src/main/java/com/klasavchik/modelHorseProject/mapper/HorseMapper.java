package com.klasavchik.modelHorseProject.mapper;

import com.klasavchik.modelHorseProject.dto.CreateHorseRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelListRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelResponse;
import com.klasavchik.modelHorseProject.entity.Model;
import com.klasavchik.modelHorseProject.entity.HorseModelMedia;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HorseMapper {
    public HorseModelListRequest toDto(Model model){
        return HorseModelListRequest.builder()
                .id(model.getId())
                .name(model.getName())
                .breed(model.getBreed())
                .avatar(model.getHorseModelMedia().get(0).getImageLink())
                .build();
    }
    public Model toEntity(CreateHorseRequest dto){
        Model model = Model.builder()
                .name(dto.getName())
                .breed(dto.getBreed())
                .description(dto.getDescription())
                .masterName(dto.getMasterName())
                .build();

        List<HorseModelMedia> mediaList = dto.getMedia().stream()
                .map(m -> HorseModelMedia.builder()
                        .dateUpdate(LocalDateTime.now())
                        .imageLink(m.getLink())
                        .model(model)
                        .build())
                .toList();
        model.setHorseModelMedia(mediaList);
        return model;
    }
    public HorseModelResponse toHorseModelResponse(Model model){
        return HorseModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .breed(model.getBreed())
                .description(model.getDescription())
                .masterName(model.getMasterName())
                .avatar(model.getHorseModelMedia().get(0).getImageLink())
//                .master(horseModel.getMaster())
                .releaseDate(model.getReleaseDate())
//                .horseModelMedia(horseModel.getHorseModelMedia())
                .build();
    }
}
