package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.entity.Theme;
import com.klasavchik.modelHorseProject.newDto.model.ThemeDto;
import com.klasavchik.modelHorseProject.security.JwtUtil;
import com.klasavchik.modelHorseProject.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me/theme")
    public ResponseEntity<ThemeDto> getMyTheme() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        Long userId = jwtUtil.extractUserId(token);
        Theme theme = settingsService.getTheme(userId);
        return ResponseEntity.ok(new ThemeDto(theme));
    }

    @PutMapping("/me/theme")
    public ResponseEntity<ThemeDto> changeMyTheme(@RequestBody ThemeDto themeDto) {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        Long userId = jwtUtil.extractUserId(token);

        // Защита: только сам пользователь или админ
        if (!jwtUtil.extractRoles(token).contains("ROLE_ADMIN") && !userId.equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        settingsService.changeTheme(userId, themeDto.getTheme());
        return ResponseEntity.ok(new ThemeDto(themeDto.getTheme()));
    }
}