package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.newDto.show.CreateShowRequest;
import com.klasavchik.modelHorseProject.newDto.show.ShowCardResponse;
import com.klasavchik.modelHorseProject.newDto.show.ShowShortResponse;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows")
@CrossOrigin(origins = "http://localhost:3000")
public class ShowController {

    private final ShowService showService;

    // Создание шоу (без регламента — он загружается отдельно)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowShortResponse createShow(
            @Valid @RequestBody CreateShowRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUserId();

        return showService.createShow(request, userId);
    }

    // Загрузка баннера
    @PutMapping("/{id}/banner")
    @ResponseStatus(HttpStatus.OK)
    public ShowShortResponse uploadBanner(
            @PathVariable Long id,
            @RequestPart("banner") MultipartFile bannerFile) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUserId();

        return showService.uploadBanner(id, bannerFile, userId);
    }

    // Загрузка регламента (новая ручка)
    @PutMapping("/{id}/rules")
    @ResponseStatus(HttpStatus.OK)
    public ShowShortResponse uploadRules(
            @PathVariable Long id,
            @RequestPart("rules") MultipartFile rulesFile) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUserId();

        return showService.uploadRules(id, rulesFile, userId);
    }

    // Мои шоу (карточки)
    @GetMapping("/my")
    public ResponseEntity<List<ShowCardResponse>> getMyShows() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUserId();

        List<ShowCardResponse> myShows = showService.getMyShows(userId);
        return ResponseEntity.ok(myShows);
    }
}