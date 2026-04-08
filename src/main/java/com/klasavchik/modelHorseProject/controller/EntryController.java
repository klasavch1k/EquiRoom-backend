package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.entry.*;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows")
@CrossOrigin(origins = "http://localhost:3000")
public class EntryController {

    private final EntryService entryService;

    private void requireAuth(CustomUserDetails details) {
        if (details == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization token");
        }
    }

    // 1. POST — подача модели в класс
    @PostMapping("/{showId}/classes/{classId}/entries")
    public ResponseEntity<EntryResponse> createEntry(
            @PathVariable Long showId,
            @PathVariable Long classId,
            @Valid @RequestBody CreateEntryRequest request,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        EntryResponse response = entryService.createEntry(showId, classId, request, details.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. GET — мои entries на шоу
    @GetMapping("/{showId}/my-entries")
    public ResponseEntity<List<MyEntryResponse>> getMyEntries(
            @PathVariable Long showId,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        return ResponseEntity.ok(entryService.getMyEntries(showId, details.getUserId()));
    }

    // 3. GET — entries в классе (организатор/судья)
    @GetMapping("/{showId}/classes/{classId}/entries")
    public ResponseEntity<Page<ClassEntryListItem>> getClassEntries(
            @PathVariable Long showId,
            @PathVariable Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(entryService.getEntriesByClass(showId, classId, details.getUserId(), pageable));
    }

    // 4. GET — детали entry
    @GetMapping("/entries/{entryId}")
    public ResponseEntity<EntryDetailResponse> getEntryDetail(
            @PathVariable Long entryId,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        return ResponseEntity.ok(entryService.getEntryDetail(entryId, details.getUserId()));
    }

    // 5. PUT — смена статуса entry (организатор/судья)
    @PutMapping("/entries/{entryId}/status")
    public ResponseEntity<EntryDetailResponse> updateEntryStatus(
            @PathVariable Long entryId,
            @RequestBody UpdateEntryStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        return ResponseEntity.ok(entryService.updateEntryStatus(entryId, request, details.getUserId()));
    }

    // 6. DELETE — удалить/отозвать entry
    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Long entryId,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        entryService.deleteEntry(entryId, details.getUserId());
        return ResponseEntity.noContent().build();
    }

    // 7. GET — счётчик (сколько подано / сколько можно)
    @GetMapping("/{showId}/my-entries/count")
    public ResponseEntity<EntryCountResponse> getMyEntryCount(
            @PathVariable Long showId,
            @AuthenticationPrincipal CustomUserDetails details) {
        requireAuth(details);
        return ResponseEntity.ok(entryService.getMyEntryCount(showId, details.getUserId()));
    }
}
