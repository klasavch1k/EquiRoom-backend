package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.judging.*;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.JudgingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows/{showId}/classes/{classId}/judging")
@CrossOrigin(origins = "http://localhost:3000")
public class JudgingController {

    private final JudgingService judgingService;

    private void requireAuth(CustomUserDetails details) {
        if (details == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization token");
        }
    }

    /**
     * GET /api/v1/shows/{showId}/classes/{classId}/judging
     * Загрузка всех данных страницы оценивания одним запросом.
     */
    @GetMapping
    public ResponseEntity<JudgingPageResponse> getJudgingPage(
            @PathVariable Long showId,
            @PathVariable Long classId,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        return ResponseEntity.ok(judgingService.getJudgingPage(showId, classId, details.getUserId()));
    }

    /**
     * GET /api/v1/shows/{showId}/classes/{classId}/judging/scores?judgeId={judgeId}
     * Оценки конкретного судьи по всем моделям класса.
     */
    @GetMapping("/scores")
    public ResponseEntity<JudgeScoresResponse> getScores(
            @PathVariable Long showId,
            @PathVariable Long classId,
            @RequestParam Long judgeId,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        return ResponseEntity.ok(judgingService.getScores(showId, classId, judgeId, details.getUserId()));
    }

    /**
     * PUT /api/v1/shows/{showId}/classes/{classId}/judging/scores
     * Сохранение/обновление оценок текущего судьи (bulk upsert).
     */
    @PutMapping("/scores")
    public ResponseEntity<JudgeScoresResponse> saveScores(
            @PathVariable Long showId,
            @PathVariable Long classId,
            @Valid @RequestBody SaveScoresRequest request,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        return ResponseEntity.ok(judgingService.saveScores(showId, classId, request, details.getUserId()));
    }

    /**
     * GET /api/v1/shows/{showId}/classes/{classId}/judging/summary
     * Вкладка «Итог» — средние оценки по всем судьям.
     */
    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @PathVariable Long showId,
            @PathVariable Long classId,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        return ResponseEntity.ok(judgingService.getSummary(showId, classId, details.getUserId()));
    }
}
