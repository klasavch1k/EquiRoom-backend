package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.CreateHorseRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelListRequest;
import com.klasavchik.modelHorseProject.entity.HorseModel;
import com.klasavchik.modelHorseProject.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users/{id}/collection")
public class CollectionController {
    private final CollectionService collectionService;

    @GetMapping
    public List<HorseModelListRequest> getCollection(@PathVariable Long id) {
        return collectionService.getCollection(id);
    }
    @PostMapping("/addHorse")
    public void addHorse(@PathVariable Long id, @RequestBody CreateHorseRequest horseDto) {
        collectionService.addHorse(id, horseDto);
    }
}
