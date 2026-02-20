package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.organizersJudges.AddJudgeRequest;
import com.klasavchik.modelHorseProject.dto.show.organizersJudges.AddOrganizerRequest;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.OrganizersJudgesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows")
@CrossOrigin(origins = "http://localhost:3000")
public class OrganizersJudgesController {

    private final OrganizersJudgesService organizersJudgesService;

    // Добавление co-organizer (юзера приложения)
    @PostMapping("/{id}/organizers")
    @ResponseStatus(HttpStatus.CREATED)
    public void addOrganizer(
            @PathVariable Long id,
            @RequestBody @Valid AddOrganizerRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long currentUserId = userDetails.getUserId();

        organizersJudgesService.addOrganizer(id, request.getUserId(), currentUserId);
    }
    // Удаление co-organizer
    @DeleteMapping("/{id}/organizers/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOrganizer(@PathVariable Long id, @PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long creatorId = userDetails.getUserId();
        organizersJudgesService.removeOrganizer(id, userId, creatorId);
    }

    // Добавление судьи (юзер или внешний)
    @PostMapping("/{id}/judges")
    @ResponseStatus(HttpStatus.CREATED)
    public void addJudge(@PathVariable Long id, @RequestBody @Valid AddJudgeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long creatorId = userDetails.getUserId();
        organizersJudgesService.addJudge(id, request, creatorId);
    }

    // Удаление судьи
    @DeleteMapping("/{id}/judges/{judgeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJudge(@PathVariable Long id, @PathVariable Long judgeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long creatorId = userDetails.getUserId();
        organizersJudgesService.removeJudge(id, judgeId, creatorId);
    }
}
