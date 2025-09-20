package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.CreateHorseRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelListRequest;
import com.klasavchik.modelHorseProject.entity.HorseModel;
import com.klasavchik.modelHorseProject.mapper.HorseMapper;
import com.klasavchik.modelHorseProject.repository.CollectionRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final HorseMapper horseMapper;

    private final HorseMapper mapper = new HorseMapper();
    private final UserRepository userRepository;

    public String addHorse(Long id, CreateHorseRequest horseModel) {
        HorseModel entity = horseMapper.toEntity(horseModel);
        entity.setOwner(userRepository.findById(id).get());
        collectionRepository.save(entity);
        return "I think, it happened";
    }

    @Transactional
    public List<HorseModelListRequest> getCollection(Long id) {
        return   collectionRepository.findAllByOwnerWithMedia(id)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
