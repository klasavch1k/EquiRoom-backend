package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.registration.RegistrationListItemResponse;
import com.klasavchik.modelHorseProject.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows")
@CrossOrigin(origins = "http://localhost:3000")
public class RegistrationController
{
    private final RegistrationService registrationService;

    @GetMapping("/{showId}/registrations")
    public ResponseEntity<List<RegistrationListItemResponse>> getShowRegistrations(
            @PathVariable Long showId,
            Authentication authentication) {

        Long currentUserId = null;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            currentUserId = details.getUserId();
        }

        List<RegistrationListItemResponse> list = registrationService.getRegistrationsForShow(showId, currentUserId);

        return ResponseEntity.ok(list);
    }
}
