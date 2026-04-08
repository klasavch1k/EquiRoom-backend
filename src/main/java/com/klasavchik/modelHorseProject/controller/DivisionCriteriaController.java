package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.criteria.*;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.DivisionCriteriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/divisions/{divisionId}/criteria")
public class DivisionCriteriaController {

    private final DivisionCriteriaService criteriaService;

    /**
     * GET /api/v1/divisions/{divisionId}/criteria
     * Возвращает критерии дивизиона для просмотра/редактирования.
     * Доступно всем аутентифицированным пользователям.
     */
    @GetMapping
    public ResponseEntity<CriteriaResponse> getCriteria(@PathVariable Long divisionId) {
        CriteriaResponse response = criteriaService.getCriteria(divisionId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/divisions/{divisionId}/criteria
     * Сохранение таблицы критериев целиком (добавление/удаление/редактирование/перестановка).
     * Доступно только организаторам, до старта шоу.
     */
    @PutMapping
    public ResponseEntity<CriteriaResponse> saveCriteria(
            @PathVariable Long divisionId,
            @Valid @RequestBody CriteriaSaveRequest request) {

        Long userId = getCurrentUserId();
        CriteriaResponse response = criteriaService.saveCriteria(divisionId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/divisions/{divisionId}/criteria/copy
     * Копирование критериев из другого дивизиона (в рамках одного шоу).
     * Доступно только организаторам, до старта шоу.
     */
    @PostMapping("/copy")
    public ResponseEntity<CriteriaResponse> copyCriteria(
            @PathVariable Long divisionId,
            @Valid @RequestBody CriteriaCopyRequest request) {

        Long userId = getCurrentUserId();
        CriteriaResponse response = criteriaService.copyCriteria(divisionId, request, userId);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        throw new IllegalStateException("Пользователь не аутентифицирован");
    }
}
