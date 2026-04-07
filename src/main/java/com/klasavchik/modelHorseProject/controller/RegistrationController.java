package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.registration.CreateRegistrationRequest;
import com.klasavchik.modelHorseProject.dto.show.registration.RegistrationCreateResponse;
import com.klasavchik.modelHorseProject.dto.show.registration.RegistrationListItemResponse;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import com.klasavchik.modelHorseProject.service.RegistrationService;
import com.klasavchik.modelHorseProject.service.ShowService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows")
@CrossOrigin(origins = "http://localhost:3000")
public class RegistrationController
{
    private final RegistrationService registrationService;
    private final ShowRepository showRepository;
    private final ShowService showService;

    @GetMapping("/{showId}/registrations")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<RegistrationListItemResponse>> getShowRegistrations(
            @PathVariable Long showId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails details) {

        // 1. Не залогинен → запрещено
        if (details == null) {
            return ResponseEntity.status(403).build();
        }

        Long currentUserId = details.getUserId();

        // 2. Проверяем, что это организатор или судья именно этого шоу
        Show show = showRepository.findShowById(showId);
        if (!showService.isOrganizerOrJudge(show, currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        // 3. Параметры пагинации
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(
                page,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 4. Получаем данные — сервис теперь не проверяет роли
        Page<RegistrationListItemResponse> result = registrationService
                .getRegistrationsForShowPaged(showId, pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{showId}/register")
    public ResponseEntity<RegistrationCreateResponse> registerOnShow(
            @PathVariable Long showId,
            @RequestBody CreateRegistrationRequest request,
            @AuthenticationPrincipal CustomUserDetails details) {

        if (details == null) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized
        }

        Long userId = details.getUserId();

        RegistrationCreateResponse response = registrationService.createRegistration(
                showId, request, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{showId}/registrations/search")
    @Transactional(readOnly = true)  // ← вот это решает проблему
    public ResponseEntity<Page<RegistrationListItemResponse>> searchRegistrations(
            @PathVariable Long showId,
            @RequestParam("q") String query,                      // обязательный параметр поиска
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails details) {

        // 1. Не залогинен → 403
        if (details == null) {
            return ResponseEntity.status(403).build();
        }

        Long currentUserId = details.getUserId();

        // 2. Проверяем, что это организатор или судья именно этого шоу
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        if (!showService.isOrganizerOrJudge(show, currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        // 3. Параметры пагинации
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(
                page,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 4. Поиск
        Page<RegistrationListItemResponse> result = registrationService
                .searchRegistrations(showId, query.trim(), pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/registrations/{registrationId}")
    public ResponseEntity<RegistrationListItemResponse> getRegistrationById(
            @PathVariable Long registrationId,
            @AuthenticationPrincipal CustomUserDetails details) {
        if (details == null) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(registrationService.getRegistrationById(registrationId, details.getUserId()));
    }

    @PutMapping("/registrations/{registrationId}")
    public ResponseEntity<RegistrationListItemResponse> updateRegistration(
            @PathVariable Long registrationId,
            @RequestBody com.klasavchik.modelHorseProject.dto.show.registration.UpdateRegistrationRequest request,
            @AuthenticationPrincipal CustomUserDetails details) {
        if (details == null) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(registrationService.updateRegistration(registrationId, request, details.getUserId()));
    }

    @PutMapping("/registrations/{registrationId}/status")
    public ResponseEntity<RegistrationListItemResponse> updateRegistrationStatus(
            @PathVariable Long registrationId,
            @RequestBody com.klasavchik.modelHorseProject.dto.show.registration.UpdateRegistrationStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails details) {
        if (details == null) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(registrationService.updateRegistrationStatus(registrationId, request, details.getUserId()));
    }

}
