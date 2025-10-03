package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.entity.*;
import com.klasavchik.modelHorseProject.mapper.ModelMapper;
import com.klasavchik.modelHorseProject.newDto.model.CardModelResponse;
import com.klasavchik.modelHorseProject.newDto.model.CreateModelRequest;
import com.klasavchik.modelHorseProject.newDto.model.DetailModelResponse;
import com.klasavchik.modelHorseProject.newDto.model.RewardRequest;
import com.klasavchik.modelHorseProject.repository.ModelRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelService {
    private final ModelRepository modelRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public void addModel(Long userId,
                         CreateModelRequest createModelRequest,
                         MultipartFile avatarFile,
                         List<MultipartFile> mediaFiles,
                         List<MultipartFile> rewardFiles) throws IOException {

        Model model = modelMapper.toEntity(createModelRequest);
        User user = userRepository.findById(userId).orElseThrow();
        model.setOwner(user);

        // Сохраняем аватарку
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = fileStorageService.saveFile(avatarFile);
            model.setAvatar(avatarUrl);
            System.out.println("Saving file: " + avatarFile.getOriginalFilename() + ", URL: " + avatarUrl);
        }

        // Сохраняем медиа
        if (mediaFiles != null) {
            for (MultipartFile file : mediaFiles) {
                String url = fileStorageService.saveFile(file);
                MediaType type = file.getContentType().startsWith("video/") ? MediaType.VIDEO : MediaType.IMAGE;
                model.getModelMedia().add(ModelMedia.builder()
                        .url(url)
                        .mediaType(type)
                        .model(model)
                        .build());
            }
        }

        // Сохраняем награды
        if (createModelRequest.getRewards() != null) {
            int index = 0;
            for (RewardRequest r : createModelRequest.getRewards()) {
                String rewardAvatar = null;
                if (rewardFiles != null && rewardFiles.size() > index) {
                    MultipartFile rewardFile = rewardFiles.get(index);
                    if (!rewardFile.isEmpty()) {
                        rewardAvatar = fileStorageService.saveFile(rewardFile);
                    }
                }
                model.getRewards().add(Reward.builder()
                        .rewardName(r.getRewardName())
                        .organizationName(r.getOrganizationName())
                        .year(r.getYear())
                        .avatar(rewardAvatar)
                        .model(model)
                        .build());
                index++;
            }
        }

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
