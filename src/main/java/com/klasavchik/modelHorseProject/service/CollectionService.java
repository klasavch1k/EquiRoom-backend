package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.CreateModelRequest;
import com.klasavchik.modelHorseProject.dto.ModelListRequest;
import com.klasavchik.modelHorseProject.dto.ModelResponse;
import com.klasavchik.modelHorseProject.entity.Model;
import com.klasavchik.modelHorseProject.mapper.ModelMapper;
import com.klasavchik.modelHorseProject.repository.CollectionRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final ModelMapper modelMapper;

    private final ModelMapper mapper = new ModelMapper();
    private final UserRepository userRepository;

    @Transactional
    public void addModel(Long id, CreateModelRequest Model) {
        Model entity = modelMapper.toEntity(Model);
        entity.setReleaseDate(LocalDateTime.now());
        entity.setOwner(userRepository.findById(id).get());
        collectionRepository.save(entity);
    }

    @Transactional
    public List<ModelListRequest> getCollection(Long id) {
        List<Model> allByOwnerWithMedia = collectionRepository.findAllByOwnerWithMedia(id);
        return collectionRepository.findAllByOwnerWithMedia(id)
                .stream()
                .sorted(Comparator.comparing(Model::getReleaseDate, Comparator.reverseOrder()))
                .map(mapper::toDto)
                .toList();
    }

    public ModelResponse getModel(Long modelId, Long id) {
        return modelMapper.toModelResponse(collectionRepository.findByIdWithMedia(modelId).get());
    }
}
