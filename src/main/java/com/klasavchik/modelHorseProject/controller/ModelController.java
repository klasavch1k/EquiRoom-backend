package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.entity.Model;
import com.klasavchik.modelHorseProject.mapper.ModelMapper;
import com.klasavchik.modelHorseProject.newDto.model.CreateModelRequest;
import com.klasavchik.modelHorseProject.newDto.model.CardModelResponse;
import com.klasavchik.modelHorseProject.newDto.model.DetailModelResponse;
import com.klasavchik.modelHorseProject.repository.ModelRepository;
import com.klasavchik.modelHorseProject.security.JwtUtil;
import com.klasavchik.modelHorseProject.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user/{id}/collection")
public class ModelController {
    private final ModelService modelService;
    private final JwtUtil jwtUtil;
    private final ModelRepository modelRepository;
    private final ModelMapper modelMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CardModelResponse> getListOfModels(@PathVariable Long id) {
        return modelService.findAllForCards(id);
    }

    @GetMapping("/{horseId}")
    @ResponseStatus(HttpStatus.OK)
    public DetailModelResponse getModel(@PathVariable Long id, @PathVariable Long horseId) {
        return modelService.findById(horseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createModel(
            @PathVariable Long id,
            @RequestPart("modelData") CreateModelRequest createModelRequest,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
            @RequestPart(value = "rewardFiles", required = false) List<MultipartFile> rewardFiles // optional
    ) throws IOException {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        Long currentUserId = jwtUtil.extractUserId(token);
        List<String> roles = jwtUtil.extractRoles(token);

        if (!roles.contains("ROLE_ADMIN") && !id.equals(currentUserId)) {
            throw new RuntimeException("Access denied: You can only add to your own collection");
        }

        modelService.addModel(id, createModelRequest, avatarFile, mediaFiles, rewardFiles);
    }

    @PutMapping("/{horseId}")
    public DetailModelResponse updateModel(
            @PathVariable Long id,
            @PathVariable Long horseId,
            @RequestPart("modelData") CreateModelRequest createModelRequest,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
            @RequestPart(value = "rewardFiles", required = false) List<MultipartFile> rewardFiles
    ) throws IOException {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        Long currentUserId = jwtUtil.extractUserId(token);
        List<String> roles = jwtUtil.extractRoles(token);

        if (!roles.contains("ROLE_ADMIN") && !id.equals(currentUserId)) {
            throw new RuntimeException("Access denied: You can only update your own collection");
        }

        return modelService.updateModel(horseId, createModelRequest, avatarFile, mediaFiles, rewardFiles);
    }



    @DeleteMapping("/{horseId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteModel(@PathVariable Long id, @PathVariable Long horseId) {
        //modelService.delete();
    }
}
