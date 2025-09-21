package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.CreateHorseRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelListRequest;
import com.klasavchik.modelHorseProject.dto.HorseModelResponse;
import com.klasavchik.modelHorseProject.security.JwtUtil;
import com.klasavchik.modelHorseProject.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users/{id}/collection")
public class CollectionController {
    private final CollectionService collectionService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<HorseModelListRequest> getCollection(@PathVariable Long id) {
        return collectionService.getCollection(id);
    }
    @PostMapping("/addHorse")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHorse(@PathVariable Long id, @RequestBody CreateHorseRequest horseDto) {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        Long currentUserId = jwtUtil.extractUserId(token);
        List<String> roles = jwtUtil.extractRoles(token);

        // Ограничение: обычный пользователь может добавлять только в свою коллекцию
        if (!roles.contains("ROLE_ADMIN") && !id.equals(currentUserId)) {
            throw new RuntimeException("Access denied: You can only add to your own collection");
        }
        collectionService.addHorse(id, horseDto);
    }
    @GetMapping("/{horseId}")
    @ResponseStatus(HttpStatus.OK)
    public HorseModelResponse getHorse(@PathVariable Long horseId) {
        return collectionService.getHorse(horseId);
    }

}
