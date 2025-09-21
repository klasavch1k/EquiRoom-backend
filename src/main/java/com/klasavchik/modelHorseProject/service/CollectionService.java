package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.CreateHorseRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelListRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelResponse;
import com.klasavchik.modelHorseProject.entity.Model;
import com.klasavchik.modelHorseProject.mapper.HorseMapper;
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
    private final HorseMapper horseMapper;

    private final HorseMapper mapper = new HorseMapper();
    private final UserRepository userRepository;

    @Transactional
    public void addHorse(Long id, CreateHorseRequest horseModel) {
        Model entity = horseMapper.toEntity(horseModel);
        entity.setReleaseDate(LocalDateTime.now());
        entity.setOwner(userRepository.findById(id).get());
        collectionRepository.save(entity);
    }

    @Transactional
    public List<HorseModelListRequest> getCollection(Long id) {
        return   collectionRepository.findAllByOwnerWithMedia(id)
                .stream()
                .sorted(Comparator.comparing(Model::getReleaseDate, Comparator.reverseOrder()))
                .map(mapper::toDto)
                .toList();
    }

    public HorseModelResponse getHorse(Long horseId) {
        return horseMapper.toHorseModelResponse(collectionRepository.findByIdWithMedia(horseId).get());
    }
}
