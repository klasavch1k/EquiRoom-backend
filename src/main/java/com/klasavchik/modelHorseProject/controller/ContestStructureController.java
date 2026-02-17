package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.*;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.ContestStructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ContestStructureController {

    private final ContestStructureService contestStructureService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        throw new IllegalStateException("Пользователь не аутентифицирован");
    }
    // Создание дивизиона в шоу
    @PostMapping("/shows/{showId}/divisions")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createDivision(
            @PathVariable Long showId,
            @Valid @RequestBody CreateDivisionDto dto) {

        Long userId = getCurrentUserId(); // твой метод из SecurityContext
        contestStructureService.createDivision(showId, dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Создание секции в дивизионе
    @PostMapping("/divisions/{divisionId}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createSection(
            @PathVariable Long divisionId,
            @Valid @RequestBody CreateSectionDto dto) {

        Long userId = getCurrentUserId();
        contestStructureService.createSection(divisionId, dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Создание класса в секции
    @PostMapping("/sections/{sectionId}/classes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createClass(
            @PathVariable Long sectionId,
            @Valid @RequestBody CreateClassDto dto) {

        Long userId = getCurrentUserId();
        contestStructureService.createClass(sectionId, dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/shows/{id}/structure")
    public ShowStructureResponse getShowStructure(@PathVariable Long id) {
        return contestStructureService.getShowStructure(id);
    }
    // Удаление дивизиона (и всего поддерева: секции + классы)
    @DeleteMapping("/divisions/{divisionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteDivision(@PathVariable Long divisionId) {
        Long userId = getCurrentUserId();
        contestStructureService.deleteDivision(divisionId, userId);
        return ResponseEntity.noContent().build();
    }

    // Удаление секции (и всех её классов)
    @DeleteMapping("/sections/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteSection(@PathVariable Long sectionId) {
        Long userId = getCurrentUserId();
        contestStructureService.deleteSection(sectionId, userId);
        return ResponseEntity.noContent().build();
    }

    // Удаление отдельного класса
    @DeleteMapping("/classes/{classId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteClass(@PathVariable Long classId) {
        Long userId = getCurrentUserId();
        contestStructureService.deleteClass(classId, userId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/shows/{id}")
    public ShowFullInfoResponse getShowFullInfo(@PathVariable Long id) {
        return contestStructureService.getShowFullInfo(id);
    }
    // Обновление основных полей шоу
    @PatchMapping("/shows/{showId}")
    public void updateShow(
            @PathVariable Long showId,
            @RequestBody UpdateShowRequest request) {

        Long userId = getCurrentUserId();
        Show updated = contestStructureService.updateShow(showId, request, userId);
        // или полный ShowFullInfoResponse, как у тебя сейчас
    }

    // Полная замена списка цен билетов
    @PutMapping("/shows/{showId}/ticket-prices")
    @ResponseStatus(HttpStatus.OK)
    public void updateTicketPrices(
            @PathVariable Long showId,
            @RequestBody UpdateTicketPricesRequest request) {

        Long userId = getCurrentUserId();
        contestStructureService.updateTicketPrices(showId, request.getTicketPrices(), userId);
    }
}