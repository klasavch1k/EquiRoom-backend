package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.CreateModelRequest;
import com.klasavchik.modelHorseProject.dto.ModelListRequest;
import com.klasavchik.modelHorseProject.dto.ModelResponse;
import com.klasavchik.modelHorseProject.security.JwtUtil;
import com.klasavchik.modelHorseProject.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users/{id}/collection")
public class CollectionController {
    private final CollectionService collectionService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ModelListRequest> getCollection(@PathVariable Long id) {
        return collectionService.getCollection(id);
    }
    @PostMapping("/addModel")
    @ResponseStatus(HttpStatus.CREATED)
    public void addModel(@PathVariable Long id, @RequestBody CreateModelRequest modelDto) {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        Long currentUserId = jwtUtil.extractUserId(token);
        List<String> roles = jwtUtil.extractRoles(token);

        // Ограничение: обычный пользователь может добавлять только в свою коллекцию
        if (!roles.contains("ROLE_ADMIN") && !id.equals(currentUserId)) {
            throw new RuntimeException("Access denied: You can only add to your own collection");
        }
        collectionService.addModel(id, modelDto);
    }
    @GetMapping("/{modelId}")
    @ResponseStatus(HttpStatus.OK)
    public ModelResponse getModel(@PathVariable("modelId") Long modelId, @PathVariable Long id) {
        return collectionService.getModel(modelId, id);
    }

}
