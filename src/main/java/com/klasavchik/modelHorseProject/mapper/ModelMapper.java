package com.klasavchik.modelHorseProject.mapper;

import com.klasavchik.modelHorseProject.dto.CreateModelRequest;
import com.klasavchik.modelHorseProject.dto.ModelListRequest;
import com.klasavchik.modelHorseProject.dto.ModelResponse;
import com.klasavchik.modelHorseProject.entity.Model;
import com.klasavchik.modelHorseProject.entity.ModelMedia;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ModelMapper {
    public ModelListRequest toDto(Model model){
        return ModelListRequest.builder()
                .id(model.getId())
                .name(model.getName())
                .breed(model.getBreed())
                .image(model.getModelMedia().get(0).getImageLink())
                .build();
    }
    public Model toEntity(CreateModelRequest dto){
        Model model = Model.builder()
                .name(dto.getName())
                .breed(dto.getBreed())
                .description(dto.getDescription())
                .masterName(dto.getMasterName())
                .build();

        List<ModelMedia> mediaList = dto.getMedia().stream()
                .map(m -> ModelMedia.builder()
                        .dateUpdate(LocalDateTime.now())
                        .imageLink(m.getLink())
                        .model(model)
                        .build())
                .toList();
        model.setModelMedia(mediaList);
        return model;
    }
    public ModelResponse toModelResponse(Model model){
        return ModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .breed(model.getBreed())
                .description(model.getDescription())
                .masterName(model.getMasterName())
                .avatar(model.getModelMedia().get(0).getImageLink())
                .releaseDate(model.getReleaseDate())
                .build();
    }
}
