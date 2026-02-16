package com.klasavchik.modelHorseProject.mapper;

import com.klasavchik.modelHorseProject.entity.model.Model;
import com.klasavchik.modelHorseProject.newDto.model.*;
import com.klasavchik.modelHorseProject.repository.ModelRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ModelMapper {
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;

    public ModelMapper(ModelRepository modelRepository, UserRepository userRepository) {
        this.modelRepository = modelRepository;
        this.userRepository = userRepository;
    }

    public CardModelResponse toCardModelResponse(Model model) {
        return CardModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .modelMasterName(model.getModelMasterName())
                .yearOfPainting(model.getYearOfPainting())
                .salesInformation(model.getSalesInformation())
                .avatar(model.getAvatar())
                .build();
    }
    public DetailModelResponse toDetailModelResponse(Model model){
        DetailModelResponse dto = DetailModelResponse.builder()
                .owner(model.getOwner().getProfile().getFirstName()+" "+model.getOwner().getProfile().getLastName())
                .id(model.getId())
                .name(model.getName())
                .avatar(model.getAvatar())
                .breed(model.getBreed())
                .horseColor(model.getHorseColor())
                .salesInformation(model.getSalesInformation())
                .modelMasterName(model.getModelMasterName())
                .artMasterName(model.getArtMasterName())
                .yearOfPainting(model.getYearOfPainting())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .media(
                        model.getModelMedia() != null
                                ? model.getModelMedia().stream()
                                .map(m -> new MediaDto(
                                        m.getId(),
                                        m.getUrl(),
                                        m.getMediaType(),
                                        m.getCreatedAt()
                                ))
                                .collect(Collectors.toSet())
                                : Set.of()
                )
                .rewards(
                        model.getRewards() != null
                                ? model.getRewards().stream()
                                .map(r -> new RewardDto(
                                        r.getId(),
                                        r.getRewardName(),
                                        r.getOrganizationName(),
                                        r.getYear(),
                                        r.getAvatar(),
                                        r.getCreatedAt()
                                ))
                                .collect(Collectors.toSet())
                                : Set.of()
                )
                .build();
        return dto;
    }
    public Model toEntity(CreateModelRequest createModelRequest) {
        return Model.builder()
                .name(createModelRequest.getName())
                .avatar(createModelRequest.getAvatar())
                .breed(createModelRequest.getBreed())
                .horseColor(createModelRequest.getHorseColor())
                .salesInformation(createModelRequest.getSalesInformation())
                .modelMasterName(createModelRequest.getModelMasterName())
                .artMasterName(createModelRequest.getArtMasterName())
                .yearOfPainting(createModelRequest.getYearOfPainting())
                .build();
    }

}
