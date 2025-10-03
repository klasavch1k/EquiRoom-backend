package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.entity.*;
import com.klasavchik.modelHorseProject.mapper.ModelMapper;
import com.klasavchik.modelHorseProject.newDto.model.*;
import com.klasavchik.modelHorseProject.repository.ModelRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Transactional
    public DetailModelResponse updateModel(
            Long horseId,
            CreateModelRequest createModelRequest,
            MultipartFile avatarFile,
            List<MultipartFile> mediaFiles,
            List<MultipartFile> rewardFiles
    ) throws IOException {

        Model model = modelRepository.findModelWithDetails(horseId)
                .orElseThrow(() -> new RuntimeException("Model not found"));

        // --- Обновляем основные поля ---
        model.setName(createModelRequest.getName());
        model.setBreed(createModelRequest.getBreed());
        model.setHorseColor(createModelRequest.getHorseColor());
        model.setSalesInformation(createModelRequest.getSalesInformation());
        model.setModelMasterName(createModelRequest.getModelMasterName());
        model.setArtMasterName(createModelRequest.getArtMasterName());
        model.setYearOfPainting(createModelRequest.getYearOfPainting());

        // --- Аватар ---
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = fileStorageService.saveFile(avatarFile);
            model.setAvatar(avatarUrl);
        }

        // --- Обновление медиа ---
        Map<Long, ModelMedia> existingMedia = model.getModelMedia().stream()
                .collect(Collectors.toMap(ModelMedia::getId, m -> m));

        Set<ModelMedia> updatedMedia = new HashSet<>();
        int mediaIndex = 0;
        for (ModelMediaRequest mReq : createModelRequest.getModelMedia()) {
            ModelMedia media;
            if (mReq.getId() != null && existingMedia.containsKey(mReq.getId())) {
                media = existingMedia.get(mReq.getId());
                media.setUrl(mReq.getUrl());
                media.setMediaType(mReq.getMediaType());
            } else {
                // Новый файл
                MultipartFile file = (mediaFiles != null && mediaFiles.size() > mediaIndex) ? mediaFiles.get(mediaIndex) : null;
                String url = mReq.getUrl();
                if (file != null && !file.isEmpty()) {
                    url = fileStorageService.saveFile(file);
                }
                media = ModelMedia.builder()
                        .url(url)
                        .mediaType(mReq.getMediaType())
                        .model(model)
                        .build();
                mediaIndex++;
            }
            updatedMedia.add(media);
        }
        model.getModelMedia().clear();
        model.getModelMedia().addAll(updatedMedia);

        // --- Обновление наград ---
        Map<Long, Reward> existingRewards = model.getRewards().stream()
                .collect(Collectors.toMap(r -> r.getId().longValue(), r -> r));

        Set<Reward> updatedRewards = new HashSet<>();
        int rewardIndex = 0;
        for (RewardRequest rReq : createModelRequest.getRewards()) {
            Reward reward;
            if (rReq.getId() != null && existingRewards.containsKey(rReq.getId())) {
                reward = existingRewards.get(rReq.getId());
                reward.setRewardName(rReq.getRewardName());
                reward.setOrganizationName(rReq.getOrganizationName());
                reward.setYear(rReq.getYear());
            } else {
                MultipartFile file = (rewardFiles != null && rewardFiles.size() > rewardIndex) ? rewardFiles.get(rewardIndex) : null;
                String avatarUrl = rReq.getAvatar();
                if (file != null && !file.isEmpty()) {
                    avatarUrl = fileStorageService.saveFile(file);
                }
                reward = Reward.builder()
                        .rewardName(rReq.getRewardName())
                        .organizationName(rReq.getOrganizationName())
                        .year(rReq.getYear())
                        .avatar(avatarUrl)
                        .model(model)
                        .build();
                rewardIndex++;
            }
            updatedRewards.add(reward);
        }
        model.getRewards().clear();
        model.getRewards().addAll(updatedRewards);

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
