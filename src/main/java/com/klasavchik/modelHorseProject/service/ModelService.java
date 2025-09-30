package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.entity.Model;
import com.klasavchik.modelHorseProject.entity.ModelMedia;
import com.klasavchik.modelHorseProject.entity.Reward;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.mapper.ModelMapper;
import com.klasavchik.modelHorseProject.newDto.model.CardModelResponse;
import com.klasavchik.modelHorseProject.newDto.model.CreateModelRequest;
import com.klasavchik.modelHorseProject.newDto.model.DetailModelResponse;
import com.klasavchik.modelHorseProject.repository.ModelRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelService {
    private final ModelRepository modelRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public void addModel(Long userId, CreateModelRequest createModelRequest){
        Model model = modelMapper.toEntity(createModelRequest);
        User user = userRepository.findById(userId).get();
        model.setOwner(user);
        modelRepository.save(model);
    }
    public DetailModelResponse update(Long horseId, CreateModelRequest createModelRequest){
        Model model = modelRepository.findModelWithDetails(horseId).get();
        model.setName(createModelRequest.getName());
        model.setAvatar(createModelRequest.getAvatar());
        model.setBreed(createModelRequest.getBreed());
        model.setHorseColor(createModelRequest.getHorseColor());
        model.setSalesInformation(createModelRequest.getSalesInformation());
        model.setModelMasterName(createModelRequest.getModelMasterName());
        model.setArtMasterName(createModelRequest.getArtMasterName());
        model.setYearOfPainting(createModelRequest.getYearOfPainting());

        // чистим и пересохраняем награды и медиа
        model.getRewards().clear();
        createModelRequest.getRewards().forEach(r -> model.getRewards().add(
                Reward.builder()
                        .rewardName(r.getRewardName())
                        .organizationName(r.getOrganizationName())
                        .year(r.getYear())
                        .avatar(r.getAvatar())
                        .model(model)
                        .build()
        ));

        model.getModelMedia().clear();
        createModelRequest.getModelMedia().forEach(m -> model.getModelMedia().add(
                ModelMedia.builder()
                        .url(m.getUrl())
                        .mediaType(m.getMediaType())
                        .model(model)
                        .build()
        ));

        modelRepository.save(model);

        return modelMapper.toDetailModelResponse(model);

    }
    public void delete(Long horseId){
        Model model = modelRepository.findById(horseId)
                .orElseThrow(() -> new RuntimeException("Model not found"));
        modelRepository.delete(model);
    }
    public DetailModelResponse findById(Long horseId){
        Model model = modelRepository.findModelWithDetails(horseId).orElseThrow(() -> new RuntimeException("Model not found"));
        return modelMapper.toDetailModelResponse(model);
    }
    public List<CardModelResponse> findAllForCards(Long userId){
        return modelRepository.findAllCardsByOwnerId(userId);
    }
}
