package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.CreateShowRequest;
import com.klasavchik.modelHorseProject.dto.show.ShowCardResponse;
import com.klasavchik.modelHorseProject.dto.show.ShowShortResponse;
import com.klasavchik.modelHorseProject.dto.show.UpdateShowRequest;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.ShowService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
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
    private final ShowRepository showRepository;

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
            @RequestPart(value = "banner", required = false) MultipartFile bannerFile,
            @RequestPart(value = "delete", required = false) Boolean delete) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUserId();

        boolean shouldDelete = delete != null && delete;   // null → false, true → удаляем
        return showService.updateBanner(id, bannerFile, shouldDelete, userId);
    }

    // То же самое для регламента
    @PutMapping("/{id}/rules")
    @ResponseStatus(HttpStatus.OK)
    public ShowShortResponse uploadRules(
            @PathVariable Long id,
            @RequestPart(value = "rules", required = false) MultipartFile rulesFile,
            @RequestPart(value = "delete", required = false) Boolean delete) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUserId();

        boolean shouldDelete = delete != null && delete;   // null → false, true → удаляем
        return showService.updateRules(id, rulesFile, shouldDelete, userId);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ShowCardResponse>> getMyShows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            Authentication authentication) {

        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        Long userId = details.getUserId();

        // Защита от некорректных значений
        int safeSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(page, safeSize);

        Page<ShowCardResponse> pagedResult = showService.getMyShowsPaged(userId, pageable);

        return ResponseEntity.ok(pagedResult);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ShowShortResponse updateShow(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShowRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUserId();

        return showService.updateShowBasicFields(id, request, userId);
    }
}