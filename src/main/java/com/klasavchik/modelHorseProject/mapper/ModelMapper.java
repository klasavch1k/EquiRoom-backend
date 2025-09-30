package com.klasavchik.modelHorseProject.mapper;

import com.klasavchik.modelHorseProject.entity.Model;
import com.klasavchik.modelHorseProject.entity.ModelMedia;
import com.klasavchik.modelHorseProject.entity.Reward;
import com.klasavchik.modelHorseProject.newDto.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelMapper {
    public CardModelResponse toCardModelResponse(Model model) {
        return CardModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .artMasterName(model.getArtMasterName())
                .yearOfPainting(model.getYearOfPainting())
                .salesInformation(model.getSalesInformation())
                .avatar(model.getAvatar())
                .build();
    }
    public DetailModelResponse toDetailModelResponse(Model model){
        return DetailModelResponse.builder()
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
                                .toList()
                                : List.of()
                )
                .rewards(
                        model.getRewards() != null
                                ? model.getRewards().stream()
                                .map(r -> new RewardDto(
                                        r.getId(),
                                        r.getRewardName(),
                                        r.getOrganizationName(),
                                        r.getYear(),
                                        r.getAvatar()
                                ))
                                .toList()
                                : List.of()
                )
                .build();
    }
    public Model toEntity(CreateModelRequest createModelRequest) {
        Model model = Model.builder()
                .name(createModelRequest.getName())
                .avatar(createModelRequest.getAvatar())
                .breed(createModelRequest.getBreed())
                .horseColor(createModelRequest.getHorseColor())
                .salesInformation(createModelRequest.getSalesInformation())
                .modelMasterName(createModelRequest.getModelMasterName())
                .artMasterName(createModelRequest.getArtMasterName())
                .yearOfPainting(createModelRequest.getYearOfPainting())
                .build();
        if (createModelRequest.getModelMedia() != null) {
            model.setModelMedia(createModelRequest.getModelMedia().stream()
                    .map(m -> ModelMedia.builder()
                            .url(m.getUrl())
                            .mediaType(m.getMediaType())
                            .model(model)
                            .build())
                    .toList());
        }
        if (createModelRequest.getRewards() != null) {
            model.setRewards(createModelRequest.getRewards().stream()
                    .map(r -> Reward.builder()
                            .rewardName(r.getRewardName())
                            .organizationName(r.getOrganizationName())
                            .year(r.getYear())
                            .avatar(r.getAvatar())
                            .model(model)
                            .build())
                    .toList());
        }
        return model;
    }
}
